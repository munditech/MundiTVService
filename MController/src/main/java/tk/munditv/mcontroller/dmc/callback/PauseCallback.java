
package tk.munditv.mcontroller.dmc.callback;

import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.Pause;

public class PauseCallback extends Pause {

    private final static String TAG = PauseCallback.class.getSimpleName();

    public PauseCallback(Service paramService) {
        super(paramService);
        Log.d(TAG, "PauseCallback()");
    }

    public void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse,
                        String paramString) {
        Log.d(TAG, "pause failed");
    }

    public void success(ActionInvocation paramActionInvocation) {
        super.success(paramActionInvocation);
        Log.d(TAG, "pause success");
    }

}
