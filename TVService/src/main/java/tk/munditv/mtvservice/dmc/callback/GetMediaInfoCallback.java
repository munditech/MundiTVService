
package tk.munditv.mtvservice.dmc.callback;

import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.model.MediaInfo;

public class GetMediaInfoCallback extends GetMediaInfo {

    private final static String TAG = GetMediaInfoCallback.class.getSimpleName();

    public GetMediaInfoCallback(Service service) {
        super(service);
        Log.d(TAG, "GetMediaInfoCallback()");
    }

    @Override
    public void received(ActionInvocation paramActionInvocation, MediaInfo paramMediaInfo) {
        Log.d(TAG, "received()");

    }

    @Override
    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        // TODO Auto-generated method stub
        Log.d(TAG, "failure()");
    }

    public void success(ActionInvocation paramActionInvocation) {
        super.success(paramActionInvocation);
        Log.d(TAG, "success()");
    }
}
