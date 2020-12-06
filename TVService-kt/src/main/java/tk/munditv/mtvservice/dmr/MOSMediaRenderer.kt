package tk.munditv.mtvservice.dmr

import android.content.Context
import android.os.Build
import android.util.Log
import org.fourthline.cling.binding.LocalServiceBinder
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.ServiceManager
import org.fourthline.cling.model.ValidationException
import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.DLNACaps
import org.fourthline.cling.model.types.DLNADoc
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser
import tk.munditv.libtvservice.support.tvcontrol.lastchange.TVControlLastChangeParser
import tk.munditv.libtvservice.util.FileUtil
import tk.munditv.libtvservice.util.NetworkData
import tk.munditv.libtvservice.util.UpnpUtil
import tk.munditv.libtvservice.util.Utils
import java.io.IOException

class MOSMediaRenderer(numberOfPlayers: Int, context: Context) {
    protected val binder: LocalServiceBinder = AnnotationLocalServiceBinder()

    // These are shared between all "logical" player instances of a single service
    protected val avTransportLastChange = LastChange(AVTransportLastChangeParser())
    protected val renderingControlLastChange = LastChange(RenderingControlLastChangeParser())
    protected val tvControlLastChange = LastChange(TVControlLastChangeParser())
    protected val mediaPlayers: Map<UnsignedIntegerFourBytes, MOSMediaPlayer>
    protected val connectionManager: ServiceManager<MOSConnectionManagerService>
    protected val avTransport: LastChangeAwareServiceManager<AVTransportService>
    protected val renderingControl: LastChangeAwareServiceManager<AudioRenderingControl>
    protected val tvControl: LastChangeAwareServiceManager<TVControl>
    protected var device: LocalDevice? = null
    protected var mContext: Context

    // The backend player instances will fill the LastChange whenever something happens with
    // whatever event messages are appropriate. This loop will periodically flush these changes
    // to subscribers of the LastChange state variable of each service.
    protected fun runLastChangePushThread() {
        Log.d(TAG, "runLastChangePushThread()")

        // TODO: We should only run this if we actually have event subscribers
        object : Thread() {
            override fun run() {
                try {
                    while (true) {
                        // These operations will NOT block and wait for network responses
                        avTransport.fireLastChange()
                        renderingControl.fireLastChange()
                        tvControl.fireLastChange()
                        sleep(LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS)
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "runLastChangePushThread ex", ex)
                }
            }
        }.start()
    }

    @JvmName("getDevice1")
    fun getDevice(): LocalDevice? {
        Log.d(TAG, "getDevice()")
        return device
    }

    @JvmName("getMediaPlayers1")
    @Synchronized
    fun getMediaPlayers(): Map<UnsignedIntegerFourBytes, MOSMediaPlayer> {
        Log.d(TAG, "getMediaPlayers()")
        return mediaPlayers
    }

    @Synchronized
    fun stopAllMediaPlayers() {
        Log.d(TAG, "stopAllMediaPlayers()")
        for (mediaPlayer in mediaPlayers.values) {
            val state = mediaPlayer.currentTransportInfo.currentTransportState
            if (state != TransportState.NO_MEDIA_PRESENT || state == TransportState.STOPPED) {
                Log.i(TAG, "Stopping player instance: " + mediaPlayer.instanceId)
                //                mediaPlayer.stop();
            }
        }
    }

    @JvmName("getConnectionManager1")
    fun getConnectionManager(): ServiceManager<MOSConnectionManagerService> {
        Log.d(TAG, "getConnectionManager()")
        return connectionManager
    }

    fun getAvTransport(): ServiceManager<AVTransportService> {
        Log.d(TAG, "getAvTransport()")
        return avTransport
    }

    fun getRenderingControl(): ServiceManager<AudioRenderingControl> {
        Log.d(TAG, "getRenderingControl()")
        return renderingControl
    }

    val tVControl: ServiceManager<TVControl>
        get() {
            Log.d(TAG, "getTVControl()")
            return tvControl
        }

    protected fun createDefaultDeviceIcon(): Icon? {
        Log.d(TAG, "createDefaultDeviceIcon()")
        return try {
            Icon("image/png", 48, 48, 32, "msi.png", mContext.resources.assets
                    .open(FileUtil.LOGO))
        } catch (e: IOException) {
            Log.w(TAG, "createDefaultDeviceIcon IOException")
            null
        }
    }

    companion object {
        private val TAG = MOSMediaRenderer::class.java.simpleName
        const val LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS: Long = 500
    }

    init {
        Log.d(TAG, "MOSMediaRenderer()")
        mContext = context
        // This is the backend which manages the actual player instances
        mediaPlayers = object : MOSMediaPlayers(
                numberOfPlayers,
                context,
                avTransportLastChange,
                renderingControlLastChange,
                tvControlLastChange
        ) {
            // These overrides connect the player instances to the output/display
            override fun onPlay(player: MOSMediaPlayer) {
//                getDisplayHandler().onPlay(player);
            }

            override fun onStop(player: MOSMediaPlayer) {
//                getDisplayHandler().onStop(player);
            }
        } as Map<UnsignedIntegerFourBytes, MOSMediaPlayer>

        // The connection manager doesn't have to do much, HTTP is stateless
        val connectionManagerService = binder.read(MOSConnectionManagerService::class.java) as LocalService<MOSConnectionManagerService>
        connectionManager = object : DefaultServiceManager<MOSConnectionManagerService>(connectionManagerService) {
            @Throws(Exception::class)
            override fun createServiceInstance(): MOSConnectionManagerService {
                return MOSConnectionManagerService()
            }
        }
        connectionManagerService.setManager(connectionManager)

        // The AVTransport just passes the calls on to the backend players
        val avTransportService: LocalService<AVTransportService> = binder.read(AVTransportService::class.java) as LocalService<AVTransportService>
        avTransport = object : LastChangeAwareServiceManager<AVTransportService>(
                avTransportService,
                AVTransportLastChangeParser()
        ) {
            @Throws(Exception::class)
            override fun createServiceInstance(): AVTransportService {
                return AVTransportService(avTransportLastChange, mediaPlayers)
            }
        }
        avTransportService.setManager(avTransport)

        // The Rendering Control just passes the calls on to the backend players
        val renderingControlService: LocalService<AudioRenderingControl> = binder.read(AudioRenderingControl::class.java) as LocalService<AudioRenderingControl>
        renderingControl = object : LastChangeAwareServiceManager<AudioRenderingControl>(
                renderingControlService,
                RenderingControlLastChangeParser()
        ) {
            @Throws(Exception::class)
            override fun createServiceInstance(): AudioRenderingControl {
                return AudioRenderingControl(renderingControlLastChange, mediaPlayers)
            }
        }
        renderingControlService.setManager(renderingControl)
        Log.d(TAG, "add tvcontrol service start!")
        val tvControlService: LocalService<TVControl> = binder.read(TVControl::class.java) as LocalService<TVControl>
        Log.d(TAG, "add tvcontrol service phrase 1")
        tvControl = object : LastChangeAwareServiceManager<TVControl>(
                tvControlService,
                TVControlLastChangeParser()
        ) {
            @Throws(Exception::class)
            override fun createServiceInstance(): TVControl {
                return TVControl(tvControlLastChange, mediaPlayers)
            }
        }
        Log.d(TAG, "add tvcontrol service phrase 2")
        tvControlService.setManager(tvControl)
        Log.d(TAG, "add tvcontrol service finished!")
        try {
            val udn = UpnpUtil.uniqueSystemIdentifier("msidmr")
            device = LocalDevice( //TODO zxt
                    DeviceIdentity(udn),
                    UDADeviceType("MediaRenderer", 1),
                    DeviceDetails(
                            NetworkData.getRenderName() + " (" + Build.MODEL + ")",
                            ManufacturerDetails(Utils.MANUFACTURER),
                            ModelDetails(Utils.DMR_NAME, Utils.DMR_DESC, "1", Utils.DMR_MODEL_URL), arrayOf(
                            DLNADoc("DMR", DLNADoc.Version.V1_5)
                    ), DLNACaps(arrayOf(
                            "av-upload", "image-upload", "audio-upload"
                    ))
                    ), arrayOf(createDefaultDeviceIcon()), arrayOf(
                    avTransportService,
                    renderingControlService,
                    connectionManagerService,
                    tvControlService
            ))
            Log.i(TAG, "getType: " + device!!.getType().toString())
        } catch (ex: ValidationException) {
            throw RuntimeException(ex)
        }
        runLastChangePushThread()
    }
}