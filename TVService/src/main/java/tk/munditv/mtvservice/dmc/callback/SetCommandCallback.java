package tk.munditv.mtvservice.dmc.callback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;

import tk.munditv.libtvservice.support.tvcontrol.callback.SetCommand;
import tk.munditv.mtvservice.dmc.DMCControlMessage;

public class SetCommandCallback extends SetCommand {

    private final static String TAG = SetCommandCallback.class.getSimpleName();

    private String commandString;
    private Handler handler;

    public SetCommandCallback(Service paramService, String paramString,
                              Handler paramHandler) {
        super(paramService, paramString);
        this.handler = paramHandler;
        this.commandString = paramString;
    }

    public void failure(ActionInvocation paramActionInvocation,
                        UpnpResponse paramUpnpResponse, String paramString) {
        Log.d(TAG, "set command failed");
    }

    public void success(ActionInvocation paramActionInvocation) {
        super.success(paramActionInvocation);

        Log.d(TAG, "set command success");
        Message localMessage = new Message();
        localMessage.what = DMCControlMessage.SETCOMMANDSUC;
        Bundle localBundle = new Bundle();
        localBundle.putString("command", commandString);
        localMessage.setData(localBundle);
        this.handler.sendMessage(localMessage);
    }
}
