package tk.munditv.mtvservice.dmr

import android.util.Log
import org.fourthline.cling.model.types.ErrorCode
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.avtransport.AVTransportErrorCode
import org.fourthline.cling.support.avtransport.AVTransportException
import org.fourthline.cling.support.avtransport.AbstractAVTransportService
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.model.*
import org.seamless.http.HttpFetch
import org.seamless.util.URIUtil
import tk.munditv.libtvservice.util.Utils
import java.net.URI

/**
 * @author offbye
 */
class AVTransportService(lastChange: LastChange?, protected val players: Map<UnsignedIntegerFourBytes, MOSMediaPlayer>) : AbstractAVTransportService(lastChange) {
    @Throws(AVTransportException::class)
    protected fun getInstance(instanceId: UnsignedIntegerFourBytes): MOSMediaPlayer {
        return players[instanceId]
                ?: throw AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID)
    }

    @Throws(AVTransportException::class)
    override fun setAVTransportURI(instanceId: UnsignedIntegerFourBytes,
                                   currentURI: String,
                                   currentURIMetaData: String) {
        Log.d(TAG, "$currentURI---$currentURIMetaData")
        val uri: URI
        uri = try {
            URI(currentURI)
        } catch (ex: Exception) {
            throw AVTransportException(
                    ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed"
            )
        }
        if (currentURI.startsWith("http:")) {
            try {
                HttpFetch.validate(URIUtil.toURL(uri))
            } catch (ex: Exception) {
                throw AVTransportException(
                        AVTransportErrorCode.RESOURCE_NOT_FOUND, ex.message
                )
            }
        } else if (!currentURI.startsWith("file:")) {
            throw AVTransportException(
                    ErrorCode.INVALID_ARGS, "Only HTTP and file: resource identifiers are supported"
            )
        }

        // TODO: Check mime type of resource against supported types
        // TODO: DIDL fragment parsing and handling of currentURIMetaData
        var type = "image"
        if (currentURIMetaData.contains("object.item.videoItem")) {
            type = "video"
        } else if (currentURIMetaData.contains("object.item.imageItem")) {
            type = "image"
        } else if (currentURIMetaData.contains("object.item.audioItem")) {
            type = "audio"
        }
        val name = currentURIMetaData.substring(currentURIMetaData.indexOf("<dc:title>") + 10,
                currentURIMetaData.indexOf("</dc:title>"))
        Log.d(TAG, name)
        getInstance(instanceId).setURI(uri, type, name, currentURIMetaData)
    }

    @Throws(AVTransportException::class)
    override fun getMediaInfo(instanceId: UnsignedIntegerFourBytes): MediaInfo {
        Log.d(TAG, "getMediaInfo()")
        return getInstance(instanceId).currentMediaInfo
    }

    @Throws(AVTransportException::class)
    override fun getTransportInfo(instanceId: UnsignedIntegerFourBytes): TransportInfo {
        Log.d(TAG, "getTransportInfo()")
        return getInstance(instanceId).currentTransportInfo
    }

    @Throws(AVTransportException::class)
    override fun getPositionInfo(instanceId: UnsignedIntegerFourBytes): PositionInfo {
        Log.d(TAG, "getPositionInfo()")
        return getInstance(instanceId).currentPositionInfo
    }

    @Throws(AVTransportException::class)
    override fun getDeviceCapabilities(instanceId: UnsignedIntegerFourBytes): DeviceCapabilities {
        Log.d(TAG, "getDeviceCapabilities()")
        getInstance(instanceId)
        return DeviceCapabilities(arrayOf(StorageMedium.NETWORK))
    }

    @Throws(AVTransportException::class)
    override fun getTransportSettings(instanceId: UnsignedIntegerFourBytes): TransportSettings {
        Log.d(TAG, "getTransportSettings()")
        getInstance(instanceId)
        return TransportSettings(PlayMode.NORMAL)
    }

    @Throws(AVTransportException::class)
    override fun stop(instanceId: UnsignedIntegerFourBytes) {
        Log.d(TAG, "stop()")
        getInstance(instanceId).stop()
    }

    @Throws(AVTransportException::class)
    override fun play(instanceId: UnsignedIntegerFourBytes, speed: String) {
        Log.d(TAG, "play()")
        getInstance(instanceId).play()
    }

    @Throws(AVTransportException::class)
    override fun pause(instanceId: UnsignedIntegerFourBytes) {
        Log.d(TAG, "pause()")
        getInstance(instanceId).pause()
    }

    @Throws(AVTransportException::class)
    override fun record(instanceId: UnsignedIntegerFourBytes) {
        // Not implemented
        Log.d(TAG, "### TODO: Not implemented: Record()")
    }

    @Throws(AVTransportException::class)
    override fun seek(instanceId: UnsignedIntegerFourBytes, unit: String, target: String) {
        Log.d(TAG, "seek()")
        val player = getInstance(instanceId)
        val seekMode: SeekMode
        try {
            seekMode = SeekMode.valueOrExceptionOf(unit)
            require(seekMode == SeekMode.REL_TIME)

//            final ClockTime ct = ClockTime.fromSeconds(ModelUtil.fromTimeString(target));
            val pos = (Utils.getRealTime(target) * 1000)
            Log.i(TAG, "### $unit target: $target  pos: $pos")

//            if (getInstance(instanceId).getCurrentTransportInfo().getCurrentTransportState()
//                    .equals(TransportState.PLAYING)) {
//                getInstance(instanceId).pause();
//                getInstance(instanceId).seek(pos);
//                getInstance(instanceId).play();
//            } else if (getInstance(instanceId).getCurrentTransportInfo().getCurrentTransportState()
//                    .equals(TransportState.PAUSED_PLAYBACK)) {
            getInstance(instanceId).seek(pos)
            //            }
        } catch (ex: IllegalArgumentException) {
            throw AVTransportException(
                    AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: $unit"
            )
        }
    }

    @Throws(AVTransportException::class)
    override fun next(instanceId: UnsignedIntegerFourBytes) {
        // Not implemented
        Log.d(TAG, "### TODO: Not implemented: Next()")
    }

    @Throws(AVTransportException::class)
    override fun previous(instanceId: UnsignedIntegerFourBytes) {
        // Not implemented
        Log.d(TAG, "### TODO: Not implemented: Previous()")
    }

    @Throws(AVTransportException::class)
    override fun setNextAVTransportURI(instanceId: UnsignedIntegerFourBytes,
                                       nextURI: String,
                                       nextURIMetaData: String) {
        Log.d(TAG, "### TODO: Not implemented: SetNextAVTransportURI()")
        // Not implemented
    }

    @Throws(AVTransportException::class)
    override fun setPlayMode(instanceId: UnsignedIntegerFourBytes, newPlayMode: String) {
        // Not implemented
        Log.d(TAG, "### TODO: Not implemented: SetPlayMode()")
    }

    @Throws(AVTransportException::class)
    override fun setRecordQualityMode(instanceId: UnsignedIntegerFourBytes, newRecordQualityMode: String) {
        // Not implemented
        Log.d(TAG, "### TODO: Not implemented: SetRecordQualityMode()")
    }

    @Throws(Exception::class)
    override fun getCurrentTransportActions(instanceId: UnsignedIntegerFourBytes): Array<TransportAction>? {
        Log.d(TAG, "getCurrentTransportActions()")
        return getInstance(instanceId).currentTransportActions
    }

    override fun getCurrentInstanceIds(): Array<UnsignedIntegerFourBytes?> {
        Log.d(TAG, "getCurrentInstanceIds()")
        val ids = arrayOfNulls<UnsignedIntegerFourBytes>(players.size)
        var i = 0
        for (id in players.keys) {
            ids[i] = id
            i++
        }
        return ids
    }

    companion object {
        private val TAG = AVTransportService::class.java.simpleName
    }
}