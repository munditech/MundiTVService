
package tk.munditv.mcontroller.dmr;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

import com.google.gson.Gson;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelMute;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import java.net.URI;

import tk.munditv.libtvservice.support.tvcontrol.lastchange.ChannelPackages;
import tk.munditv.libtvservice.support.tvcontrol.lastchange.TVControlVariable;
import tk.munditv.mcontroller.dmp.GPlayer;
import tk.munditv.mcontroller.dmp.GPlayer.MediaListener;
import tk.munditv.mcontroller.util.Action;
import tk.munditv.mcontroller.util.ConfigData;
import tk.munditv.mcontroller.util.PInfo;

/**
 * @author offbye
 */
public class MOSMediaPlayer {

    private static final String TAG = MOSMediaPlayer.class.getSimpleName();

    final private UnsignedIntegerFourBytes instanceId;
    final private LastChange avTransportLastChange;
    final private LastChange renderingControlLastChange;
    final private LastChange tvControlLastChange;

//    final private VideoComponent videoComponent = new VideoComponent();

    // We'll synchronize read/writes to these fields
    private volatile TransportInfo currentTransportInfo = new TransportInfo();
    private PositionInfo currentPositionInfo = new PositionInfo();
    private MediaInfo currentMediaInfo = new MediaInfo();
    private double storedVolume;
    private String command = null;
    private Context mContext;

    public MOSMediaPlayer(UnsignedIntegerFourBytes instanceId, Context context,
                          LastChange avTransportLastChange,
                          LastChange renderingControlLastChange,
                          LastChange tvControlLastChange) {
        super();
        Log.d(TAG, "MOSMediaPlayer()");
        this.instanceId = instanceId;
        this.mContext = context;
        this.avTransportLastChange = avTransportLastChange;
        this.renderingControlLastChange = renderingControlLastChange;
        this.tvControlLastChange = tvControlLastChange;

        try {
            // Disconnect the old bus listener
            /* TODO: That doesn't work for some reason...
            getPipeline().getBus().disconnect(
                    (Bus.STATE_CHANGED) Reflections.getField(getClass(), "stateChanged").get(this)
            );
            */

            // Connect a fixed bus state listener
//            getPipeline().getBus().connect(busStateChanged);

            // Connect a bus tag listener
//            getPipeline().getBus().connect(busTag);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

//        addMediaListener(new GstMediaListener());

//        setVideoSink(videoComponent.getElement());
    }

    public UnsignedIntegerFourBytes getInstanceId() {
        Log.d(TAG, "getInstanceId()");

        return instanceId;
    }

    public LastChange getAvTransportLastChange() {
        Log.d(TAG, "getAvTransportLastChange()");

        return avTransportLastChange;
    }

    public LastChange getRenderingControlLastChange() {
        Log.d(TAG, "getRenderingControlLastChange()");

        return renderingControlLastChange;
    }

    public LastChange getTVControlLastChange() {
        Log.d(TAG, "getTVControlLastChange()");

        return tvControlLastChange;
    }

//    public VideoComponent getVideoComponent() {
//        return videoComponent;
//    }

    // TODO: gstreamer-java has a broken implementation of getStreamInfo(), so we need to
    // do our best fishing for the stream type inside the playbin pipeline

    /*
    synchronized public boolean isDecodingStreamType(String prefix) {
        for (Element element : getPipeline().getElements()) {
            if (element.getName().matches("decodebin[0-9]+")) {
                for (Pad pad : element.getPads()) {
                    if (pad.getName().matches("src[0-9]+")) {
                        Caps caps = pad.getNegotiatedCaps();
                        Structure struct = caps.getStructure(0);
                        if (struct.getName().startsWith(prefix + "/"))
                            return true;
                    }
                }
            }
        }
        return false;
    } */

    synchronized public TransportInfo getCurrentTransportInfo() {
        Log.d(TAG, "getCurrentTransportInfo()");

        return currentTransportInfo;
    }

    synchronized public PositionInfo getCurrentPositionInfo() {
        Log.d(TAG, "getCurrentPositionInfo()");

        return currentPositionInfo;
    }

    synchronized public MediaInfo getCurrentMediaInfo() {
        Log.d(TAG, "getCurrentMediaInfo()");

        return currentMediaInfo;
    }

   // @Override
    synchronized public void setURI(URI uri, String type, String name, String currentURIMetaData) {
        Log.d(TAG, "setURI() = " + uri);

        currentMediaInfo = new MediaInfo(uri.toString(),currentURIMetaData);
        currentPositionInfo = new PositionInfo(1, "", uri.toString());

        getAvTransportLastChange().setEventedValue(getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri));

        transportStateChanged(TransportState.STOPPED);
        
        GPlayer.setMediaListener(new MOSMediaListener());
        
        Intent intent = new Intent();
        intent.setClass(mContext, RenderPlayerService.class);
        intent.putExtra("type", type);
        intent.putExtra("name", name);
        intent.putExtra("playURI", uri.toString());
        mContext.startService(intent);
    }

//    @Override
    synchronized public void setVolume(double volume) {
        Log.i(TAG,"setVolume() = " + volume);
        storedVolume = getVolume();
        
        Intent intent = new Intent();
        intent.setAction(Action.DMR);
        intent.putExtra("helpAction", Action.SET_VOLUME);
        intent.putExtra("volume", volume);

        mContext.sendBroadcast(intent);        
        
        ChannelMute switchedMute =
                (storedVolume == 0 && volume > 0) || (storedVolume > 0 && volume == 0)
                        ? new ChannelMute(Channel.Master, storedVolume > 0 && volume == 0)
                        : null;

        getRenderingControlLastChange().setEventedValue(
                getInstanceId(),
                new RenderingControlVariable.Volume(
                        new ChannelVolume(Channel.Master, (int) (volume * 100))
                ),
                switchedMute != null
                        ? new RenderingControlVariable.Mute(switchedMute)
                        : null
        );
    }

    synchronized public void setMute(boolean desiredMute) {
        Log.d(TAG, "setMute() = " + desiredMute);
        
        if (desiredMute && getVolume() > 0) {
            Log.d(TAG, "Switching mute ON");
            setVolume(0);
        } else if (!desiredMute && getVolume() == 0) {
            Log.d(TAG, "Switching mute OFF, restoring: " + storedVolume);
            setVolume(storedVolume);
        }
    }

    // Because we don't have an automated state machine, we need to calculate the possible transitions here

    synchronized public TransportAction[] getCurrentTransportActions() {
        Log.d(TAG, "getCurrentTransportActions()");

        TransportState state = currentTransportInfo.getCurrentTransportState();
        TransportAction[] actions;

        switch (state) {
            case STOPPED:
                actions = new TransportAction[]{
                        TransportAction.Play
                };
                break;
            case PLAYING:
                actions = new TransportAction[]{
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek
                };
                break;
            case PAUSED_PLAYBACK:
                actions = new TransportAction[]{
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek,
                        TransportAction.Play
                };
                break;
            default:
                actions = null;
        }
        return actions;
    }

    // Can't disconnect the broken bus listener, these funny methods disable it
/*
    protected void fireStartEvent(StartEvent ev) {
    }

    protected void fireStartEventFix(StartEvent ev) {
        for (MediaListener l : getMediaListeners()) {
            l.start(ev);
        }
    }

    protected void fireStopEvent(StopEvent ev) {

    }

    protected void fireStopEventFix(StopEvent ev) {
        for (MediaListener l : getMediaListeners()) {
            l.stop(ev);
        }
    }

    protected void firePauseEvent(PauseEvent ev) {

    }

    protected void firePauseEventFix(PauseEvent ev) {
        for (MediaListener l : getMediaListeners()) {
            l.pause(ev);
        }
    }

    // TODO: The gstreamer-java folks don't seem to understand their code very well, nobody knew what
    // I was talking about when I mentioned "transitioning" as a new callback for the listener... so yes,
    // this hack is still necessary.
    private final Bus.STATE_CHANGED busStateChanged = new Bus.STATE_CHANGED() {
        public void stateChanged(GstObject source, State old, State newState, State pending) {
            if (!source.equals(getPipeline())) return;
            log.fine("GST pipeline changed state from " + old + " to " + newState + ", pending: " + pending);
            final ClockTime position = getPipeline().queryPosition();
            if (newState.equals(State.PLAYING) && old.equals(State.PAUSED)) {
                fireStartEventFix(new StartEvent(GstMediaPlayer.this, old, newState, State.VOID_PENDING, position));
            } else if (newState.equals(State.PAUSED) && pending.equals(State.VOID_PENDING)) {
                firePauseEventFix(new PauseEvent(GstMediaPlayer.this, old, newState, State.VOID_PENDING, position));
            } else if (newState.equals(State.READY) && pending.equals(State.NULL)) {
                fireStopEventFix(new StopEvent(GstMediaPlayer.this, old, newState, pending, position));
            }

            // Anything else means we are transitioning!
            if (!pending.equals(State.VOID_PENDING) && !pending.equals(State.NULL))
                transportStateChanged(TransportState.TRANSITIONING);
        }
    };
*/
    synchronized protected void transportStateChanged(TransportState newState) {
        Log.d(TAG, "transportStateChanged()");

        TransportState currentTransportState = currentTransportInfo.getCurrentTransportState();
        Log.d(TAG, "Current state is: " + currentTransportState + ", changing to new state: " + newState);
        currentTransportInfo = new TransportInfo(newState);

        getAvTransportLastChange().setEventedValue(
                getInstanceId(),
                new AVTransportVariable.TransportState(newState),
                new AVTransportVariable.CurrentTransportActions(getCurrentTransportActions())
        );
    }

    protected class MOSMediaListener implements MediaListener {
        public void pause() {
            transportStateChanged(TransportState.PAUSED_PLAYBACK);
        }

        public void start() {
            transportStateChanged(TransportState.PLAYING);
        }

        public void stop() {
            transportStateChanged(TransportState.STOPPED);
        }

        public void endOfMedia() {
            Log.d(TAG, "End Of Media event received, stopping media player backend");

            transportStateChanged(TransportState.NO_MEDIA_PRESENT);
            //GstMediaPlayer.this.stop();
        }

        public void positionChanged(int position) {
            Log.d(TAG, "Position Changed event received: " + position);

            synchronized (MOSMediaPlayer.this) {
                currentPositionInfo = new PositionInfo(1, currentMediaInfo.getMediaDuration(),
                        currentMediaInfo.getCurrentURI(), ModelUtil.toTimeString(position/1000),
                        ModelUtil.toTimeString(position/1000));
            }
        }

        public void durationChanged(int duration) {
            Log.d(TAG, "Duration Changed event received: " + duration);

            synchronized (MOSMediaPlayer.this) {
                String newValue = ModelUtil.toTimeString(duration/1000);
                currentMediaInfo = new MediaInfo(currentMediaInfo.getCurrentURI(), "",
                        new UnsignedIntegerFourBytes(1), newValue, StorageMedium.NETWORK);

                getAvTransportLastChange().setEventedValue(getInstanceId(),
                        new AVTransportVariable.CurrentTrackDuration(newValue),
                        new AVTransportVariable.CurrentMediaDuration(newValue));
            }
        }
    } 
    
    public double getVolume() {
        Log.d(TAG, "getVolume()");

        AudioManager audioManager = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
        double v =  (double)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "getVolume " + v);
        return v;
    }

    synchronized public String getPackages() {
        Log.d(TAG, "getPackages()");

        final int max = ConfigData.apps.size();
        for (int i = 0; i < max; i++) {
            PInfo p = ConfigData.apps.get(i);
            Log.d(TAG, "get package name = " + p.getAppname());
        }
        String jstring = new Gson().toJson(ConfigData.apps);
        Log.d(TAG, "jstring = " + jstring);

        getTVControlLastChange().setEventedValue(
                getInstanceId(),
                new TVControlVariable.Packages(
                        new ChannelPackages(Channel.Master, jstring)
                )
        );

        return jstring;
    }


    public String getCommand() {
        Log.d(TAG, "getCommand() = " + command);

        return command;
    }

    public void setCommand(String command) {
        Log.d(TAG, "setCommand() = " + command);

        this.command = command;
        if (command.toLowerCase().contains("[ytvideo]=")) {
            String videoid = command.substring(10);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setClassName("tk.munditv.ottservice",
            "tk.munditv.ottservice.activity.YTPlayerActivity");
            intent.putExtra(Intent.EXTRA_TEXT, videoid);
            intent.setType("text/plain");
            Log.d(TAG, "videoid = " + videoid);
            mContext.startActivity(intent);
        } else if (command.toUpperCase().contains("[KEYCODE]")) {
            String keyCodeString  = command.substring(9);
            simulateKey(parseKeyCode(keyCodeString));
        } else if (command.toUpperCase().contains("[COMMAND]")){
            checkPackage(command);
        }
        return;
    }

    private int parseKeyCode(String keystr) {
        int code = 0;
        if (keystr.toUpperCase().equals("KEY_0")) {
            code = KeyEvent.KEYCODE_0;
        } else if (keystr.toUpperCase().equals("KEY_1")) {
            code = KeyEvent.KEYCODE_1;
        } else if (keystr.toUpperCase().equals("KEY_2")) {
            code = KeyEvent.KEYCODE_2;
        } else if (keystr.toUpperCase().equals("KEY_3")) {
            code = KeyEvent.KEYCODE_3;
        } else if (keystr.toUpperCase().equals("KEY_4")) {
            code = KeyEvent.KEYCODE_4;
        } else if (keystr.toUpperCase().equals("KEY_5")) {
            code = KeyEvent.KEYCODE_5;
        } else if (keystr.toUpperCase().equals("KEY_6")) {
            code = KeyEvent.KEYCODE_6;
        } else if (keystr.toUpperCase().equals("KEY_7")) {
            code = KeyEvent.KEYCODE_7;
        } else if (keystr.toUpperCase().equals("KEY_8")) {
            code = KeyEvent.KEYCODE_8;
        } else if (keystr.toUpperCase().equals("KEY_9")) {
            code = KeyEvent.KEYCODE_9;
        } else if (keystr.toUpperCase().equals("KEY_DELETE")) {
            code = KeyEvent.KEYCODE_DEL;
        } else if (keystr.toUpperCase().equals("KEY_BACK")) {
            code = KeyEvent.KEYCODE_BACK;
        } else if (keystr.toUpperCase().equals("KEY_ENTER")) {
            code = KeyEvent.KEYCODE_ENTER;
        } else if (keystr.toUpperCase().equals("KEY_NEBY")) {
            code = KeyEvent.KEYCODE_MENU;
        } else if (keystr.toUpperCase().equals("KEY_GUIDE")) {
            code = KeyEvent.KEYCODE_GUIDE;
        } else if (keystr.toUpperCase().equals("KEY_HELP")) {
            code = KeyEvent.KEYCODE_HELP;
        } else if (keystr.toUpperCase().equals("KEY_INFO")) {
            code = KeyEvent.KEYCODE_INFO;
        } else if (keystr.toUpperCase().equals("KEY_DPAD_UP")) {
            code = KeyEvent.KEYCODE_DPAD_UP;
        } else if (keystr.toUpperCase().equals("KEY_DPAD_DOWN")) {
            code = KeyEvent.KEYCODE_DPAD_DOWN;
        } else if (keystr.toUpperCase().equals("KEY_DPAD_LEFT")) {
            code = KeyEvent.KEYCODE_DPAD_LEFT;
        } else if (keystr.toUpperCase().equals("KEY_DPAD_RIGHT")) {
            code = KeyEvent.KEYCODE_DPAD_RIGHT;
        } else if (keystr.toUpperCase().equals("KEY_DPAD_CENTER")) {
            code = KeyEvent.KEYCODE_DPAD_CENTER;
        } else if (keystr.toUpperCase().equals("KEY_CHANNEL_UP")) {
            code = KeyEvent.KEYCODE_CHANNEL_UP;
        } else if (keystr.toUpperCase().equals("KEY_CHANNEL_DOWN")) {
            code = KeyEvent.KEYCODE_CHANNEL_DOWN;
        } else if (keystr.toUpperCase().equals("KEY_VOLUME_UP")) {
            code = KeyEvent.KEYCODE_VOLUME_UP;
        } else if (keystr.toUpperCase().equals("KEY_VOLUME_DOWN")) {
            code = KeyEvent.KEYCODE_VOLUME_DOWN;
        } else if (keystr.toUpperCase().equals("KEY_VOLUME_MUTE")) {
            code = KeyEvent.KEYCODE_VOLUME_MUTE;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_PLAY")) {
            code = KeyEvent.KEYCODE_MEDIA_PLAY;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_PLAY_PAUSE")) {
            code = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_PAUSE")) {
            code = KeyEvent.KEYCODE_MEDIA_PAUSE;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_STOP")) {
            code = KeyEvent.KEYCODE_MEDIA_STOP;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_PREVIOUS")) {
            code = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_NEXT")) {
            code = KeyEvent.KEYCODE_MEDIA_NEXT;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_EJECT")) {
            code = KeyEvent.KEYCODE_MEDIA_EJECT;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_FAST_FORWARD")) {
            code = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_REWIND")) {
            code = KeyEvent.KEYCODE_MEDIA_REWIND;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_RECORD")) {
            code = KeyEvent.KEYCODE_MEDIA_RECORD;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_AUDIO_TRACK")) {
            code = KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_SKIP_BACKWARD")) {
            code = KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_SKIP_FORWARD")) {
            code = KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_CLOSE")) {
            code = KeyEvent.KEYCODE_MEDIA_CLOSE;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_STEP_BACKWARD")) {
            code = KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_STEP_FORWARD")) {
            code = KeyEvent.KEYCODE_MEDIA_STEP_FORWARD;
        } else if (keystr.toUpperCase().equals("KEY_MEDIA_TOP_MENU")) {
            code = KeyEvent.KEYCODE_MEDIA_TOP_MENU;
        }
        return code;
    }

    private static void simulateKey(final int KeyCode) {

        new Thread() {
            @Override
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

        }.start();
        return;
    }

    private void checkPackage(String msg) {
        Log.d(TAG, "checkPackage() = " + msg);

        final int max = ConfigData.apps.size();
        for (int i = 0; i < max; i++) {
            PInfo p = ConfigData.apps.get(i);
            Log.d(TAG, "compare package name = " + p.getAppname() + " message = " + msg);
            if(msg.toLowerCase().contains(p.getAppname().toLowerCase())) {
                execute(p);
                break;
            }
        }
        return;
    }

    private void execute(PInfo p) {
        Log.d(TAG, "execute =" + p.getAppname());

        String packagename = p.getPName();
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packagename);
        if (intent != null) {
            // We found the activity now start the activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
        return;
    }


    public void play() {
        Log.d(TAG,"play()");
        sendBroadcastAction(Action.PLAY);
    }

    public void pause() {
        Log.d(TAG,"pause()");

        sendBroadcastAction(Action.PAUSE);
    }

    public void stop() {
        Log.d(TAG,"stop()");

        sendBroadcastAction(Action.STOP);
    }
    
    public void seek(int position) {
        Log.d(TAG,"seek() = " +  position);

        Intent intent = new Intent();
        intent.setAction(Action.DMR);
        intent.putExtra("helpAction", Action.SEEK);
        intent.putExtra("position", position);
        mContext.sendBroadcast(intent);
    }
    
    public void sendBroadcastAction(String action) {
        Log.d(TAG,"sendBroadcastAction() = " +  action);

        Intent intent = new Intent();
        intent.setAction(Action.DMR);
        intent.putExtra("helpAction", action);
        mContext.sendBroadcast(intent);
    }
}

