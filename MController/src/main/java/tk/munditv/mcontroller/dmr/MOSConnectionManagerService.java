
package tk.munditv.mcontroller.dmr;

import android.util.Log;

import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.seamless.util.MimeType;

/**
 * @author zxt
 */
public class MOSConnectionManagerService extends ConnectionManagerService {

    private final static String TAG = MOSConnectionManagerService.class.getSimpleName();

    public MOSConnectionManagerService() {

        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("image/jpeg")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("image/png")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("image/gif")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("image/bmp")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("image/pjpeg")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("image/tiff")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("image/x-ms-bmp")));
        
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/3gpp")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/mp4")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/3gp2")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/avi")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/flv")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/mpeg")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/x-mkv")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/x-matroska")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/msvideo")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/quicktime")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/x-msvideo")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("video/x-ms-wmv")));

        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/aac")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/3gpp")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/amr")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/ogg")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/mpeg")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/midi")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/x-midi")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/x-mid")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/x-wav")));
        sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/x-ms-wma")));

        Log.d(TAG, "Supported MIME types: " + sinkProtocolInfo.size());
    }

}
