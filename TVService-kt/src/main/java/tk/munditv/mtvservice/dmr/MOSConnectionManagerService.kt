package tk.munditv.mtvservice.dmr

import android.util.Log
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService
import org.fourthline.cling.support.model.ProtocolInfo
import org.seamless.util.MimeType

/**
 * @author zxt
 */
class MOSConnectionManagerService : ConnectionManagerService() {
    companion object {
        private val TAG = MOSConnectionManagerService::class.java.simpleName
    }

    init {
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("image/jpeg")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("image/png")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("image/gif")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("image/bmp")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("image/pjpeg")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("image/tiff")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("image/x-ms-bmp")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/3gpp")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/mp4")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/3gp2")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/avi")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/flv")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/mpeg")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/x-mkv")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/x-matroska")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/msvideo")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/quicktime")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/x-msvideo")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("video/x-ms-wmv")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("audio/aac")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("audio/3gpp")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("audio/amr")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("audio/ogg")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("audio/mpeg")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("audio/midi")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("audio/x-midi")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("audio/x-mid")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("audio/x-wav")))
        sinkProtocolInfo.add(ProtocolInfo(MimeType.valueOf("audio/x-ms-wma")))
        Log.d(TAG, "Supported MIME types: " + sinkProtocolInfo.size)
    }
}