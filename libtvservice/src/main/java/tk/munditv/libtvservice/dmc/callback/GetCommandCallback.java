package tk.munditv.libtvservice.dmc.callback;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;

import tk.munditv.libtvservice.support.tvcontrol.callback.GetCommand;
import tk.munditv.libtvservice.dmc.DMCControlMessage;

public class GetCommandCallback extends GetCommand {

    private final static String TAG = GetCommandCallback.class.getSimpleName();
    private Handler handler;

    public GetCommandCallback(Service paramService, Handler paramHandler) {
        super(paramService);
        Log.d(TAG, "GetCommandCallback()");

        this.handler = paramHandler;
    }

    public void failure(ActionInvocation paramActionInvocation,
                        UpnpResponse paramUpnpResponse, String paramString) {
        Log.d(TAG, "failure()");
    }

    public void received(ActionInvocation paramActionInvocation,
                         String paramString) {
        Log.d(TAG, "received() : " + paramString);

        Message localMessage = new Message();
        localMessage.what = DMCControlMessage.SETCOMMAND;
        Bundle localBundle = new Bundle();
        localBundle.putString("command", paramString);
        localMessage.setData(localBundle);
        handler.sendMessage(localMessage);
    }

}
