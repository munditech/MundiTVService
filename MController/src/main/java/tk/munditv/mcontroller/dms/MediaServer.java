
package tk.munditv.mcontroller.dms;

import android.content.Context;
import android.util.Log;

import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import java.io.IOException;

import tk.munditv.libtvservice.util.FileUtil;
import tk.munditv.libtvservice.util.UpnpUtil;
import tk.munditv.libtvservice.util.Utils;
import tk.munditv.mcontroller.SettingsActivity;
import tk.munditv.mcontroller.app.MainApplication;

public class MediaServer {

    private UDN udn ;

    private LocalDevice localDevice;

    private final static String deviceType = "MediaServer";

    private final static int version = 1;

    private final static String TAG = MediaServer.class.getSimpleName();

    public final static int PORT = 8192;
    private Context mContext;

    public MediaServer(Context context ) throws ValidationException {
        Log.d(TAG, "MediaServer()");

        mContext = context;
        DeviceType type = new UDADeviceType(deviceType, version);

        DeviceDetails details = new DeviceDetails(SettingsActivity.getDeviceName(context) + " ("
                + android.os.Build.MODEL + ")", new ManufacturerDetails(
                android.os.Build.MANUFACTURER), new ModelDetails(android.os.Build.MODEL,
                Utils.DMS_DESC, "v1"));

        LocalService service = new AnnotationLocalServiceBinder()
                .read(ContentDirectoryService.class);

        service.setManager(new DefaultServiceManager<ContentDirectoryService>(service,
                ContentDirectoryService.class));

        udn = UpnpUtil.uniqueSystemIdentifier("msidms");

        localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, createDefaultDeviceIcon(), service);

        Log.v(TAG, "MediaServer device created: ");
        Log.v(TAG, "friendly name: " + details.getFriendlyName());
        Log.v(TAG, "manufacturer: " + details.getManufacturerDetails().getManufacturer());
        Log.v(TAG, "model: " + details.getModelDetails().getModelName());

        // start http server
        try {
            new HttpServer(PORT);
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
            System.exit(-1);
        }

        Log.v(TAG, "Started Http Server on port " + PORT);
    }

    public LocalDevice getDevice() {
        Log.d(TAG, "getDevice()");
        return localDevice;
    }

    public String getAddress() {
        Log.d(TAG, "getAddress()");

        return MainApplication.getHostAddress() + ":" + PORT;
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
