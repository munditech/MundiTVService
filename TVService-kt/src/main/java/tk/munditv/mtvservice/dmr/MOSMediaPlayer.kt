package tk.munditv.mtvservice.dmr

import android.app.Instrumentation
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import android.view.KeyEvent
import com.google.gson.Gson
import org.fourthline.cling.model.ModelUtil
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable.*
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.model.*
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelMute
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable.Volume
import tk.munditv.libtvservice.support.tvcontrol.lastchange.ChannelPackages
import tk.munditv.libtvservice.support.tvcontrol.lastchange.TVControlVariable
import tk.munditv.libtvservice.util.*
import tk.munditv.mtvservice.dmp.GPlayer
import tk.munditv.mtvservice.dmp.GPlayer.Companion.setMediaListener
import java.net.URI

/**
 * @author offbye
 */
open class MOSMediaPlayer(instanceId: UnsignedIntegerFourBytes, context: Context,
                          avTransportLastChange: LastChange,
                          renderingControlLastChange: LastChange,
                          tvControlLastChange: LastChange) {
    val instanceId: UnsignedIntegerFourBytes
    private val avTransportLastChange: LastChange
    private val renderingControlLastChange: LastChange
    private val tvControlLastChange: LastChange

    //    final private VideoComponent videoComponent = new VideoComponent();
    // We'll synchronize read/writes to these fields
    //@Volatile
    var currentTransportInfo = TransportInfo()
    var currentPositionInfo = PositionInfo()
    var currentMediaInfo = MediaInfo()
    private var storedVolume = 0.0
    private var commandstr: String? = null
    private val mContext: Context

    @JvmName("getInstanceId1")
    fun getInstanceId(): UnsignedIntegerFourBytes {
        Log.d(TAG, "getInstanceId()")
        return instanceId
    }

    fun getAvTransportLastChange(): LastChange {
        Log.d(TAG, "getAvTransportLastChange()")
        return avTransportLastChange
    }

    fun getRenderingControlLastChange(): LastChange {
        Log.d(TAG, "getRenderingControlLastChange()")
        return renderingControlLastChange
    }

    val tVControlLastChange: LastChange
        get() {
            Log.d(TAG, "getTVControlLastChange()")
            return tvControlLastChange
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

    @JvmName("getCurrentTransportInfo1")
    @Synchronized
    fun getCurrentTransportInfo(): TransportInfo {
        Log.d(TAG, "getCurrentTransportInfo()")
        return currentTransportInfo
    }

    @JvmName("getCurrentPositionInfo1")
    @Synchronized
    fun getCurrentPositionInfo(): PositionInfo {
        Log.d(TAG, "getCurrentPositionInfo()")
        return currentPositionInfo
    }

    @JvmName("getCurrentMediaInfo1")
    @Synchronized
    fun getCurrentMediaInfo(): MediaInfo {
        Log.d(TAG, "getCurrentMediaInfo()")
        return currentMediaInfo
    }

    // @Override
    @Synchronized
    fun setURI(uri: URI, type: String?, name: String?, currentURIMetaData: String?) {
        Log.d(TAG, "setURI() = $uri")
        currentMediaInfo = MediaInfo(uri.toString(), currentURIMetaData)
        currentPositionInfo = PositionInfo(1, "", uri.toString())
        getAvTransportLastChange().setEventedValue(getInstanceId(),
                AVTransportURI(uri),
                CurrentTrackURI(uri))
        transportStateChanged(TransportState.STOPPED)
        setMediaListener(MOSMediaListener())
        val intent = Intent()
        intent.setClass(mContext, RenderPlayerService::class.java)
        intent.putExtra("type", type)
        intent.putExtra("name", name)
        intent.putExtra("playURI", uri.toString())
        mContext.startService(intent)
    }

    @Synchronized
    fun setMute(desiredMute: Boolean) {
        Log.d(TAG, "setMute() = $desiredMute")
        if (desiredMute && volume > 0) {
            Log.d(TAG, "Switching mute ON")
            volume = 0.0
        } else if (!desiredMute && volume == 0.0) {
            Log.d(TAG, "Switching mute OFF, restoring: $storedVolume")
            volume = storedVolume
        }
    }

    // Because we don't have an automated state machine, we need to calculate the possible transitions here
    @get:Synchronized
    val currentTransportActions: Array<TransportAction>?
        get() {
            Log.d(TAG, "getCurrentTransportActions()")
            val state = currentTransportInfo.currentTransportState
            val actions: Array<TransportAction>?
            actions = when (state) {
                TransportState.STOPPED -> arrayOf(
                        TransportAction.Play
                )
                TransportState.PLAYING -> arrayOf(
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek
                )
                TransportState.PAUSED_PLAYBACK -> arrayOf(
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek,
                        TransportAction.Play
                )
                else -> null
            }
            return actions
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
    @Synchronized
    protected open fun transportStateChanged(newState: TransportState) {
        Log.d(TAG, "transportStateChanged()")
        val currentTransportState = currentTransportInfo.currentTransportState
        Log.d(TAG, "Current state is: $currentTransportState, changing to new state: $newState")
        currentTransportInfo = TransportInfo(newState)
        getAvTransportLastChange().setEventedValue(
                getInstanceId(),
                AVTransportVariable.TransportState(newState),
                CurrentTransportActions(currentTransportActions)
        )
    }

    protected inner class MOSMediaListener : GPlayer.MediaListener {
        override fun pause() {
            transportStateChanged(TransportState.PAUSED_PLAYBACK)
        }

        override fun start() {
            transportStateChanged(TransportState.PLAYING)
        }

        override fun stop() {
            transportStateChanged(TransportState.STOPPED)
        }

        override fun endOfMedia() {
            Log.d(TAG, "End Of Media event received, stopping media player backend")
            transportStateChanged(TransportState.NO_MEDIA_PRESENT)
            //GstMediaPlayer.this.stop();
        }

        override fun positionChanged(position: Int) {
            Log.d(TAG, "Position Changed event received: $position")
            synchronized(this@MOSMediaPlayer) {
                currentPositionInfo = PositionInfo(1, currentMediaInfo.mediaDuration,
                        currentMediaInfo.currentURI, ModelUtil.toTimeString((position / 1000).toLong()),
                        ModelUtil.toTimeString((position / 1000).toLong()))
            }
        }

        override fun durationChanged(duration: Int) {
            Log.d(TAG, "Duration Changed event received: $duration")
            synchronized(this@MOSMediaPlayer) {
                val newValue = ModelUtil.toTimeString((duration / 1000).toLong())
                currentMediaInfo = MediaInfo(currentMediaInfo.currentURI, "",
                        UnsignedIntegerFourBytes(1), newValue, StorageMedium.NETWORK)
                getAvTransportLastChange().setEventedValue(getInstanceId(),
                        CurrentTrackDuration(newValue),
                        CurrentMediaDuration(newValue))
            }
        }
    }

    //    @Override
    @set:Synchronized
    var volume: Double
        get() {
            Log.d(TAG, "getVolume()")
            val audioManager = mContext.getSystemService(Service.AUDIO_SERVICE) as AudioManager
            val v = (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toDouble()
                    / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
            Log.d(TAG, "getVolume $v")
            return v
        }
        set(volume) {
            Log.i(TAG, "setVolume() = $volume")
            storedVolume = volume
            val intent = Intent()
            intent.action = Action.DMR
            intent.putExtra("helpAction", Action.SET_VOLUME)
            intent.putExtra("volume", volume)
            mContext.sendBroadcast(intent)
            val switchedMute = if (storedVolume == 0.0 && volume > 0 || storedVolume > 0 && volume == 0.0) ChannelMute(Channel.Master, storedVolume > 0 && volume == 0.0) else null
            getRenderingControlLastChange().setEventedValue(
                    getInstanceId(),
                    Volume(
                            ChannelVolume(Channel.Master, (volume * 100).toInt())
                    ),
                    if (switchedMute != null) RenderingControlVariable.Mute(switchedMute) else null
            )
        }

    @get:Synchronized
    val packages: String
        get() {
            Log.d(TAG, "getPackages()")
            val max = ConfigData.apps.size
            for (i in 0 until max) {
                val p = ConfigData.apps[i]
                Log.d(TAG, "get package name = " + p.appname)
            }
            val jstring = Gson().toJson(ConfigData.apps)
            Log.d(TAG, "jstring = $jstring")
            tVControlLastChange.setEventedValue(
                    getInstanceId(),
                    TVControlVariable.Packages(
                            ChannelPackages(Channel.Master, jstring)
                    )
            )
            return jstring
        }

    @get:Synchronized
    var command: String?
        get() {
            Log.d(TAG, "getCommand() = $command")
            return commandstr;
        }
        set(command) {
            Log.d(TAG, "setCommand() = $command")
            commandstr = command
            if (command!!.toLowerCase().contains("[ytvideo]=")) {
                val videoid = command.substring(10)
                val intent = Intent(Intent.ACTION_SEND)
                intent.setClassName("tk.munditv.ottservice",
                        "tk.munditv.ottservice.activity.YTPlayerActivity")
                intent.putExtra(Intent.EXTRA_TEXT, videoid)
                intent.type = "text/plain"
                Log.d(TAG, "videoid = $videoid")
                mContext.startActivity(intent)
            } else if (command!!.toUpperCase().contains("[KEYCODE]")) {
                val keyCodeString = command.substring(9)
                simulateKey(parseKeyCode(keyCodeString))
            } else if (command!!.toUpperCase().contains("[COMMAND]")) {
                checkPackage(command)
            }
            return
        }

/*
    fun getCommand(): String? {
        Log.d(TAG, "getCommand() = $command")
        return command
    }

    fun setCommand(command: String) {
        Log.d(TAG, "setCommand() = $command")
        this.command = command
        if (command.toLowerCase().contains("[ytvideo]=")) {
            val videoid = command.substring(10)
            val intent = Intent(Intent.ACTION_SEND)
            intent.setClassName("tk.munditv.ottservice",
                    "tk.munditv.ottservice.activity.YTPlayerActivity")
            intent.putExtra(Intent.EXTRA_TEXT, videoid)
            intent.type = "text/plain"
            Log.d(TAG, "videoid = $videoid")
            mContext.startActivity(intent)
        } else if (command.toUpperCase().contains("[KEYCODE]")) {
            val keyCodeString = command.substring(9)
            simulateKey(parseKeyCode(keyCodeString))
        } else if (command.toUpperCase().contains("[COMMAND]")) {
            checkPackage(command)
        }
        return
    }
*/

    private fun parseKeyCode(keystr: String): Int {
        var code = 0
        if (keystr.toUpperCase() == "KEY_0") {
            code = KeyEvent.KEYCODE_0
        } else if (keystr.toUpperCase() == "KEY_1") {
            code = KeyEvent.KEYCODE_1
        } else if (keystr.toUpperCase() == "KEY_2") {
            code = KeyEvent.KEYCODE_2
        } else if (keystr.toUpperCase() == "KEY_3") {
            code = KeyEvent.KEYCODE_3
        } else if (keystr.toUpperCase() == "KEY_4") {
            code = KeyEvent.KEYCODE_4
        } else if (keystr.toUpperCase() == "KEY_5") {
            code = KeyEvent.KEYCODE_5
        } else if (keystr.toUpperCase() == "KEY_6") {
            code = KeyEvent.KEYCODE_6
        } else if (keystr.toUpperCase() == "KEY_7") {
            code = KeyEvent.KEYCODE_7
        } else if (keystr.toUpperCase() == "KEY_8") {
            code = KeyEvent.KEYCODE_8
        } else if (keystr.toUpperCase() == "KEY_9") {
            code = KeyEvent.KEYCODE_9
        } else if (keystr.toUpperCase() == "KEY_DELETE") {
            code = KeyEvent.KEYCODE_DEL
        } else if (keystr.toUpperCase() == "KEY_BACK") {
            code = KeyEvent.KEYCODE_BACK
        } else if (keystr.toUpperCase() == "KEY_ENTER") {
            code = KeyEvent.KEYCODE_ENTER
        } else if (keystr.toUpperCase() == "KEY_NEBY") {
            code = KeyEvent.KEYCODE_MENU
        } else if (keystr.toUpperCase() == "KEY_GUIDE") {
            code = KeyEvent.KEYCODE_GUIDE
        } else if (keystr.toUpperCase() == "KEY_HELP") {
            code = KeyEvent.KEYCODE_HELP
        } else if (keystr.toUpperCase() == "KEY_INFO") {
            code = KeyEvent.KEYCODE_INFO
        } else if (keystr.toUpperCase() == "KEY_DPAD_UP") {
            code = KeyEvent.KEYCODE_DPAD_UP
        } else if (keystr.toUpperCase() == "KEY_DPAD_DOWN") {
            code = KeyEvent.KEYCODE_DPAD_DOWN
        } else if (keystr.toUpperCase() == "KEY_DPAD_LEFT") {
            code = KeyEvent.KEYCODE_DPAD_LEFT
        } else if (keystr.toUpperCase() == "KEY_DPAD_RIGHT") {
            code = KeyEvent.KEYCODE_DPAD_RIGHT
        } else if (keystr.toUpperCase() == "KEY_DPAD_CENTER") {
            code = KeyEvent.KEYCODE_DPAD_CENTER
        } else if (keystr.toUpperCase() == "KEY_CHANNEL_UP") {
            code = KeyEvent.KEYCODE_CHANNEL_UP
        } else if (keystr.toUpperCase() == "KEY_CHANNEL_DOWN") {
            code = KeyEvent.KEYCODE_CHANNEL_DOWN
        } else if (keystr.toUpperCase() == "KEY_VOLUME_UP") {
            code = KeyEvent.KEYCODE_VOLUME_UP
        } else if (keystr.toUpperCase() == "KEY_VOLUME_DOWN") {
            code = KeyEvent.KEYCODE_VOLUME_DOWN
        } else if (keystr.toUpperCase() == "KEY_VOLUME_MUTE") {
            code = KeyEvent.KEYCODE_VOLUME_MUTE
        } else if (keystr.toUpperCase() == "KEY_MEDIA_PLAY") {
            code = KeyEvent.KEYCODE_MEDIA_PLAY
        } else if (keystr.toUpperCase() == "KEY_MEDIA_PLAY_PAUSE") {
            code = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        } else if (keystr.toUpperCase() == "KEY_MEDIA_PAUSE") {
            code = KeyEvent.KEYCODE_MEDIA_PAUSE
        } else if (keystr.toUpperCase() == "KEY_MEDIA_STOP") {
            code = KeyEvent.KEYCODE_MEDIA_STOP
        } else if (keystr.toUpperCase() == "KEY_MEDIA_PREVIOUS") {
            code = KeyEvent.KEYCODE_MEDIA_PREVIOUS
        } else if (keystr.toUpperCase() == "KEY_MEDIA_NEXT") {
            code = KeyEvent.KEYCODE_MEDIA_NEXT
        } else if (keystr.toUpperCase() == "KEY_MEDIA_EJECT") {
            code = KeyEvent.KEYCODE_MEDIA_EJECT
        } else if (keystr.toUpperCase() == "KEY_MEDIA_FAST_FORWARD") {
            code = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
        } else if (keystr.toUpperCase() == "KEY_MEDIA_REWIND") {
            code = KeyEvent.KEYCODE_MEDIA_REWIND
        } else if (keystr.toUpperCase() == "KEY_MEDIA_RECORD") {
            code = KeyEvent.KEYCODE_MEDIA_RECORD
        } else if (keystr.toUpperCase() == "KEY_MEDIA_AUDIO_TRACK") {
            code = KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK
        } else if (keystr.toUpperCase() == "KEY_MEDIA_SKIP_BACKWARD") {
            code = KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD
        } else if (keystr.toUpperCase() == "KEY_MEDIA_SKIP_FORWARD") {
            code = KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD
        } else if (keystr.toUpperCase() == "KEY_MEDIA_CLOSE") {
            code = KeyEvent.KEYCODE_MEDIA_CLOSE
        } else if (keystr.toUpperCase() == "KEY_MEDIA_STEP_BACKWARD") {
            code = KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD
        } else if (keystr.toUpperCase() == "KEY_MEDIA_STEP_FORWARD") {
            code = KeyEvent.KEYCODE_MEDIA_STEP_FORWARD
        } else if (keystr.toUpperCase() == "KEY_MEDIA_TOP_MENU") {
            code = KeyEvent.KEYCODE_MEDIA_TOP_MENU
        }
        return code
    }

    private fun checkPackage(msg: String) {
        Log.d(TAG, "checkPackage() = $msg")
        val max = ConfigData.apps.size
        for (i in 0 until max) {
            val p = ConfigData.apps[i]
            Log.d(TAG, "compare package name = " + p.appname + " message = " + msg)
            if (msg.toLowerCase().contains(p.appname.toLowerCase())) {
                execute(p)
                break
            }
        }
        return
    }

    private fun execute(p: PInfo) {
        Log.d(TAG, "execute =" + p.appname)
        val packagename = p.pName
        val intent = mContext.packageManager.getLaunchIntentForPackage(packagename)
        if (intent != null) {
            // We found the activity now start the activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext.startActivity(intent)
        }
        return
    }

    fun play() {
        Log.d(TAG, "play()")
        sendBroadcastAction(Action.PLAY)
    }

    fun pause() {
        Log.d(TAG, "pause()")
        sendBroadcastAction(Action.PAUSE)
    }

    fun stop() {
        Log.d(TAG, "stop()")
        sendBroadcastAction(Action.STOP)
    }

    fun seek(position: Int) {
        Log.d(TAG, "seek() = $position")
        val intent = Intent()
        intent.action = Action.DMR
        intent.putExtra("helpAction", Action.SEEK)
        intent.putExtra("position", position)
        mContext.sendBroadcast(intent)
    }

    fun sendBroadcastAction(action: String) {
        Log.d(TAG, "sendBroadcastAction() = $action")
        val intent = Intent()
        intent.action = Action.DMR
        intent.putExtra("helpAction", action)
        mContext.sendBroadcast(intent)
    }

    companion object {
        private val TAG = MOSMediaPlayer::class.java.simpleName
        private fun simulateKey(KeyCode: Int) {
            object : Thread() {
                override fun run() {
                    try {
                        val inst = Instrumentation()
                        inst.sendKeyDownUpSync(KeyCode)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return
                }
            }.start()
            return
        }
    }

    init {
        Log.d(TAG, "MOSMediaPlayer()")
        this.instanceId = instanceId
        mContext = context
        this.avTransportLastChange = avTransportLastChange
        this.renderingControlLastChange = renderingControlLastChange
        this.tvControlLastChange = tvControlLastChange
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
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

//        addMediaListener(new GstMediaListener());

//        setVideoSink(videoComponent.getElement());
    }
}