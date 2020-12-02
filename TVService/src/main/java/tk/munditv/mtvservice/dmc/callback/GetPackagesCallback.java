package tk.munditv.mtvservice.dmc.callback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;

import tk.munditv.libtvservice.support.tvcontrol.callback.GetPackages;
import tk.munditv.mtvservice.dmc.DMCControlMessage;

public class GetPackagesCallback extends GetPackages {

    private final static String TAG = GetPackagesCallback.class.getSimpleName();
    private Handler handler;

    public GetPackagesCallback(Service paramService, Handler paramHandler) {
        super(paramService);
        Log.d(TAG, "GetPackagesCallback()");

        this.handler = paramHandler;
    }

    public void failure(ActionInvocation paramActionInvocation,
                        UpnpResponse paramUpnpResponse, String paramString) {
        Log.d(TAG, "failure()");
    }

    public void received(ActionInvocation paramActionInvocation,
                         String paramString) {
        Log.d(TAG, "received()");

        Message localMessage = new Message();
        localMessage.what = DMCControlMessage.GETPACKAGES;
        Bundle localBundle = new Bundle();
        localBundle.putString("packages", paramString);
        localMessage.setData(localBundle);
        handler.sendMessage(localMessage);
    }
}
