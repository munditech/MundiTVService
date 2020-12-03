package tk.munditv.mcontroller.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import tk.munditv.libtvservice.dmp.DeviceItem;
import tk.munditv.libtvservice.util.ConfigData;
import tk.munditv.libtvservice.util.NetworkData;
import tk.munditv.libtvservice.util.PInfo;
import tk.munditv.libtvservice.util.Packages;
import tk.munditv.mcontroller.SettingsActivity;
import tk.munditv.mcontroller.dmr.MOSMediaRenderer;
import tk.munditv.mcontroller.dms.MediaServer;
import tk.munditv.mcontroller.ui.applist.DMRListCallback;

public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();

    private static AndroidUpnpService upnpService;
    public static Context mContext;
    private static boolean serverPrepared = false;
    private DeviceListRegistryListener deviceListRegistryListener = new DeviceListRegistryListener();
    private static DeviceItem deviceItem;
    private static DeviceItem dmrDeviceItem;
    public static boolean isLocalDmr = true;
    private MediaServer mediaServer;
    private static MainApplication mInstance;
    private DMRListCallback callback;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "serviceConnection -- onServiceConnected()");

            if (NetworkData.mDevList == null) NetworkData.mDevList = new ArrayList<DeviceItem>();
            if (NetworkData.mDmrList == null) NetworkData.mDmrList = new ArrayList<DeviceItem>();
            NetworkData.mDevList.clear();
            NetworkData.mDmrList.clear();

            NetworkData.setUpnpService((AndroidUpnpService) service);
            upnpService = (AndroidUpnpService) service;

            Log.d(TAG, "Connected to UPnP Service");

            if (mediaServer == null
                    && SettingsActivity.getDmsOn(mContext)) {
                Log.d(TAG, "Start Media Server!");

                try {
                    mediaServer = new MediaServer(mContext);
                    upnpService.getRegistry()
                            .addDevice(mediaServer.getDevice());
                    DeviceItem localDevItem = new DeviceItem(
                            mediaServer.getDevice());

                    deviceListRegistryListener.deviceAdded(localDevItem);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            //prepareMediaServer();
                        }
                    }).start();

                } catch (Exception ex) {
                    // TODO: handle exception
                    Log.v(TAG, "Creating demo device failed");
                    return;
                }
            }

            if (SettingsActivity.getRenderOn(mContext)) {
                Log.d(TAG, "Start Media Renderer!");

                MOSMediaRenderer mediaRenderer = new MOSMediaRenderer(1,
                        mContext);
                upnpService.getRegistry().addDevice(mediaRenderer.getDevice());

                deviceListRegistryListener.dmrAdded(new DeviceItem(
                        mediaRenderer.getDevice()));
            }

            // xgf
            Log.d(TAG, "Start Search Devicesr!");

            for (Device device : upnpService.getRegistry().getDevices()) {
                if (device.getType().getNamespace().equals("schemas-upnp-org")
                        && device.getType().getType().equals("MediaServer")) {
                    final DeviceItem display = new DeviceItem(device, device
                            .getDetails().getFriendlyName(),
                            device.getDisplayString(), "(REMOTE) "
                            + device.getType().getDisplayString());
                    deviceListRegistryListener.deviceAdded(display);
                }
            }

            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(deviceListRegistryListener);
            // Refresh device list
            upnpService.getControlPoint().search();

            // select first device by default
            if (null != NetworkData.mDevList && NetworkData.mDevList.size() > 0
                    && null == NetworkData.getDmsDeviceItem()) {
                NetworkData.setDmsDeviceItem(NetworkData.mDevList.get(0));
            }
            if (null != NetworkData.mDmrList && NetworkData.mDmrList.size() > 0
                    && null == NetworkData.getDmrDeviceItem()) {
                NetworkData.setdmrDeviceItem(NetworkData.mDmrList.get(0));
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    public class DeviceListRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry,
                                                 RemoteDevice device) {
            Log.d(TAG, "remoteDeviceDiscoveryStarted()");

        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry,
                                                final RemoteDevice device, final Exception ex) {
            Log.d(TAG, "remoteDeviceDiscoveryFailed()");

        }

        /*
         * End of optimization, you can remove the whole block if your Android
         * handset is fast (>= 600 Mhz)
         */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            Log.d(TAG,"remoteDeviceAdded:" + device.toString()
                    + device.getType().getType());

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaServer")) {
                final DeviceItem display = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                deviceAdded(display);
            }

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaRenderer")) {
                final DeviceItem dmrDisplay = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                dmrAdded(dmrDisplay);
            }
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            Log.d(TAG,"remoteDeviceRemoved:" + device.toString()
                    + device.getType().getType());

            final DeviceItem display = new DeviceItem(device,
                    device.getDisplayString());
            deviceRemoved(display);

            if (device.getType().getNamespace().equals("schemas-upnp-org")
                    && device.getType().getType().equals("MediaRenderer")) {
                final DeviceItem dmrDisplay = new DeviceItem(device, device
                        .getDetails().getFriendlyName(),
                        device.getDisplayString(), "(REMOTE) "
                        + device.getType().getDisplayString());
                dmrRemoved(dmrDisplay);
            }
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            Log.d(TAG,"localDeviceAdded:" + device.toString()
                    + device.getType().getType());

            final DeviceItem display = new DeviceItem(device, device
                    .getDetails().getFriendlyName(), device.getDisplayString(),
                    "(REMOTE) " + device.getType().getDisplayString());
            deviceAdded(display);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            Log.d(TAG,"localDeviceRemoved:" + device.toString()
                    + device.getType().getType());

            final DeviceItem display = new DeviceItem(device,
                    device.getDisplayString());
            deviceRemoved(display);
        }

        public void deviceAdded(final DeviceItem di) {
            Log.d(TAG, "deviceAdded()");

            if (!NetworkData.mDevList.contains(di)) {
                NetworkData.mDevList.add(di);
            }
        }

        public void deviceRemoved(final DeviceItem di) {
            Log.d(TAG, "deviceRemoved()");

            NetworkData.mDevList.remove(di);
        }

        public void dmrAdded(final DeviceItem di) {
            Log.d(TAG, "dmrAdded()");

            if (!NetworkData.mDmrList.contains(di)) {
                NetworkData.mDmrList.add(di);
            }
            if (callback != null) {
                callback.refresh(NetworkData.mDmrList);
            }
        }

        public void dmrRemoved(final DeviceItem di) {
            Log.d(TAG, "dmrRemoved()");

            NetworkData.mDmrList.remove(di);
            if (callback != null) {
                callback.refresh(NetworkData.mDmrList);
            }
        }
    }

    public static MainApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        mContext = getApplicationContext();
        initImageLoader(mContext);
        getIp();
        initApps(this);
        String dmr = SettingsActivity.getRenderName(this);
        String dms = SettingsActivity.getDeviceName(this);
        NetworkData.setServerName(dms);
        NetworkData.setRenderName(dmr);
        //if (inetAddress != null) {
            bindService(
                    new Intent(this, AndroidUpnpServiceImpl.class),
                    serviceConnection, Context.BIND_AUTO_CREATE);
        //}
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onDestroy()");

        if (upnpService != null) {
            upnpService.getRegistry()
                    .removeListener(deviceListRegistryListener);
        }
        unbindService(serviceConnection);
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        Log.d(TAG, "initImageLoader()");

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    public static InetAddress getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress mInetAddress = enumIpAddr.nextElement();
                    if (!mInetAddress.isLoopbackAddress() && mInetAddress instanceof Inet4Address) {
                        return mInetAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void getIp() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "getIp() Thread inside : run()");
                InetAddress inetAddress = getLocalIpAddress();;
                if (inetAddress != null) {
                    NetworkData.setHostName(inetAddress.getHostName());
                    NetworkData.setHostAddress(inetAddress.getHostAddress());
                }
                return;
            }
        }).start();
    }

    public static void initApps(Context context) {
        Log.d(TAG, "initApps()");

        Packages pkg = new Packages(context);
        ConfigData.apps = pkg.getPackages();


    }

    public static Context getContext() {
        return mContext;
    }

    public void searchNetwork() {
        Log.d(TAG, "searchNetwork()");

        if (upnpService == null)
            return;
        //Toast.makeText(this, R.string.searching_lan, Toast.LENGTH_SHORT).show();
        upnpService.getRegistry().removeAllRemoteDevices();
        upnpService.getControlPoint().search();
    }

    public void setCallback(DMRListCallback callback) {
        this.callback = callback;
    }
}
