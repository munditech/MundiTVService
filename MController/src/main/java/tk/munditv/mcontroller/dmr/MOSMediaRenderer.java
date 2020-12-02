
package tk.munditv.mcontroller.dmr;

import android.content.Context;
import android.util.Log;

import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.io.IOException;
import java.util.Map;

import tk.munditv.libtvservice.support.tvcontrol.lastchange.TVControlLastChangeParser;
import tk.munditv.libtvservice.util.FileUtil;
import tk.munditv.libtvservice.util.NetworkData;
import tk.munditv.libtvservice.util.UpnpUtil;
import tk.munditv.libtvservice.util.Utils;

public class MOSMediaRenderer {

    private static final String TAG = MOSMediaRenderer.class.getSimpleName();

    public static final long LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS = 500;
    final protected LocalServiceBinder binder = new AnnotationLocalServiceBinder();
    // These are shared between all "logical" player instances of a single service
    final protected LastChange avTransportLastChange = new LastChange(new AVTransportLastChangeParser());
    final protected LastChange renderingControlLastChange = new LastChange(new RenderingControlLastChangeParser());
    final protected LastChange tvControlLastChange = new LastChange(new TVControlLastChangeParser());
    final protected Map<UnsignedIntegerFourBytes, MOSMediaPlayer> mediaPlayers;
    final protected ServiceManager<MOSConnectionManagerService> connectionManager;
    final protected LastChangeAwareServiceManager<AVTransportService> avTransport;
    final protected LastChangeAwareServiceManager<AudioRenderingControl> renderingControl;
    final protected LastChangeAwareServiceManager<TVControl> tvControl;
    final protected LocalDevice device;
    protected  Context mContext;

    public MOSMediaRenderer(int numberOfPlayers, Context context) {
        Log.d(TAG, "MOSMediaRenderer()");
        mContext = context;
        // This is the backend which manages the actual player instances
        mediaPlayers = new MOSMediaPlayers(
                numberOfPlayers,
                context,
                avTransportLastChange,
                renderingControlLastChange,
                tvControlLastChange
        ) {
            // These overrides connect the player instances to the output/display
            @Override
            protected void onPlay(MOSMediaPlayer player) {
//                getDisplayHandler().onPlay(player);
            }

            @Override
            protected void onStop(MOSMediaPlayer player) {
//                getDisplayHandler().onStop(player);
            }
        };

        // The connection manager doesn't have to do much, HTTP is stateless
        LocalService connectionManagerService = binder.read(MOSConnectionManagerService.class);
        connectionManager =
                new DefaultServiceManager(connectionManagerService) {
                    @Override
                    protected Object createServiceInstance() throws Exception {
                        return new MOSConnectionManagerService();
                    }
                };
        connectionManagerService.setManager(connectionManager);

        // The AVTransport just passes the calls on to the backend players
        LocalService<AVTransportService> avTransportService = binder.read(AVTransportService.class);
        avTransport =
                new LastChangeAwareServiceManager<AVTransportService>(
                        avTransportService,
                        new AVTransportLastChangeParser()
                ) {
                    @Override
                    protected AVTransportService createServiceInstance() throws Exception {
                        return new AVTransportService(avTransportLastChange, mediaPlayers);
                    }
                };
        avTransportService.setManager(avTransport);

        // The Rendering Control just passes the calls on to the backend players
        LocalService<AudioRenderingControl> renderingControlService = binder.read(AudioRenderingControl.class);
        renderingControl =
                new LastChangeAwareServiceManager<AudioRenderingControl>(
                        renderingControlService,
                        new RenderingControlLastChangeParser()
                ) {
                    @Override
                    protected AudioRenderingControl createServiceInstance() throws Exception {
                        return new AudioRenderingControl(renderingControlLastChange, mediaPlayers);
                    }
                };
        renderingControlService.setManager(renderingControl);

        Log.d(TAG, "add tvcontrol service start!");
        LocalService<TVControl> tvControlService = binder.read(TVControl.class);
        Log.d(TAG, "add tvcontrol service phrase 1");
        tvControl =
                new LastChangeAwareServiceManager<TVControl>(
                        tvControlService,
                        new TVControlLastChangeParser()
                ) {
                    @Override
                    protected TVControl createServiceInstance() throws Exception {
                        return new TVControl(tvControlLastChange, mediaPlayers);
                    }
                };
        Log.d(TAG, "add tvcontrol service phrase 2");
        tvControlService.setManager(tvControl);
        Log.d(TAG, "add tvcontrol service finished!");

        try {
            UDN udn = UpnpUtil.uniqueSystemIdentifier("msidmr");

            device = new LocalDevice(
                    //TODO zxt

                    new DeviceIdentity(udn),
                    new UDADeviceType("MediaRenderer", 1),
                    new DeviceDetails(
                            NetworkData.getRenderName() + " (" + android.os.Build.MODEL + ")",
                            new ManufacturerDetails(Utils.MANUFACTURER),
                            new ModelDetails(Utils.DMR_NAME, Utils.DMR_DESC, "1", Utils.DMR_MODEL_URL),
                            new DLNADoc[] {
                                new DLNADoc("DMR", DLNADoc.Version.V1_5)
                            }, new DLNACaps(new String[] {
                                 "av-upload", "image-upload", "audio-upload"
                            })
                    ),
                    new Icon[]{createDefaultDeviceIcon()},
                    new LocalService[]{
                            avTransportService,
                            renderingControlService,
                            connectionManagerService,
                            tvControlService
                    }
            );
            Log.i(TAG,  "getType: " +  device.getType().toString());
        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        }

        runLastChangePushThread();
    }

    // The backend player instances will fill the LastChange whenever something happens with
    // whatever event messages are appropriate. This loop will periodically flush these changes
    // to subscribers of the LastChange state variable of each service.
    protected void runLastChangePushThread() {
        Log.d(TAG, "runLastChangePushThread()");

        // TODO: We should only run this if we actually have event subscribers
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // These operations will NOT block and wait for network responses
                        avTransport.fireLastChange();
                        renderingControl.fireLastChange();
                        tvControl.fireLastChange();
                        Thread.sleep(LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "runLastChangePushThread ex", ex);
                }
            }
        }.start();
    }

    public LocalDevice getDevice() {
        Log.d(TAG, "getDevice()");

        return device;
    }


    synchronized public Map<UnsignedIntegerFourBytes, MOSMediaPlayer> getMediaPlayers() {
        Log.d(TAG, "getMediaPlayers()");

        return mediaPlayers;
    }

    synchronized public void stopAllMediaPlayers() {
        Log.d(TAG, "stopAllMediaPlayers()");

        for (MOSMediaPlayer mediaPlayer : mediaPlayers.values()) {
            TransportState state =
                mediaPlayer.getCurrentTransportInfo().getCurrentTransportState();
            if (!state.equals(TransportState.NO_MEDIA_PRESENT) ||
                    state.equals(TransportState.STOPPED)) {
                Log.i(TAG, "Stopping player instance: " + mediaPlayer.getInstanceId());
//                mediaPlayer.stop();
            }
        }
    }

    public ServiceManager<MOSConnectionManagerService> getConnectionManager() {
        Log.d(TAG, "getConnectionManager()");

        return connectionManager;
    }

    public ServiceManager<AVTransportService> getAvTransport() {
        Log.d(TAG, "getAvTransport()");

        return avTransport;
    }

    public ServiceManager<AudioRenderingControl> getRenderingControl() {
        Log.d(TAG, "getRenderingControl()");

        return renderingControl;
    }

    public ServiceManager<TVControl> getTVControl() {
        Log.d(TAG, "getTVControl()");

        return tvControl;
    }


    protected Icon createDefaultDeviceIcon() {
        Log.d(TAG, "createDefaultDeviceIcon()");

        try {
            return new Icon("image/png", 48, 48, 32, "msi.png", mContext.getResources().getAssets()
                    .open(FileUtil.LOGO));
        } catch (IOException e) {
            Log.w(TAG, "createDefaultDeviceIcon IOException");
            return null;
        }
    }

}
