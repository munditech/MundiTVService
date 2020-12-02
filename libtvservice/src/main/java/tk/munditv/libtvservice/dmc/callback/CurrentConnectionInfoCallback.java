
package tk.munditv.libtvservice.dmc.callback;

import android.util.Log;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.connectionmanager.callback.GetCurrentConnectionInfo;
import org.fourthline.cling.support.model.ConnectionInfo;

public class CurrentConnectionInfoCallback extends GetCurrentConnectionInfo {
    private String TAG = CurrentConnectionInfoCallback.class.getSimpleName();

    public CurrentConnectionInfoCallback(Service paramService, ControlPoint paramControlPoint,
                                         int paramInt) {
        super(paramService, paramControlPoint, paramInt);
        Log.d(TAG, "CurrentConnectionInfoCallback()");
    }

    public void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse,
                        String paramString) {
        Log.d(TAG, "failed");
    }

    public void received(ActionInvocation paramActionInvocation, ConnectionInfo paramConnectionInfo) {
        Log.d(TAG, "" + paramConnectionInfo.getConnectionID());
        Log.d(TAG, "" + paramConnectionInfo.getConnectionStatus());
    }

}
