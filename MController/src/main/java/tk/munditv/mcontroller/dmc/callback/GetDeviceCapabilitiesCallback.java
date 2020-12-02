
package tk.munditv.mcontroller.dmc.callback;

import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetDeviceCapabilities;
import org.fourthline.cling.support.model.DeviceCapabilities;

public class GetDeviceCapabilitiesCallback extends GetDeviceCapabilities {

    private final static String TAG = GetDeviceCapabilitiesCallback.class.getSimpleName();

    public GetDeviceCapabilitiesCallback(Service paramService) {
        super(paramService);
        Log.d(TAG, "GetDeviceCapabilitiesCallback()");
    }

    public void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse,
                        String paramString) {
        Log.d(TAG, "failure()");
    }

    public void received(ActionInvocation paramActionInvocation,
                         DeviceCapabilities paramDeviceCapabilities) {
        Log.e(TAG, "received() = " + paramDeviceCapabilities.getPlayMediaString());
    }

}
