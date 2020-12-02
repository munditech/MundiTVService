
package tk.munditv.mcontroller.dmc.callback;

import android.os.Handler;
import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.Stop;

import tk.munditv.mcontroller.dmc.DMCControlMessage;

public class StopCallback extends Stop {

    private final static String TAG = StopCallback.class.getSimpleName();

    private Handler handler;
    private Boolean isRePlay = false;
    private int type;

    public StopCallback(Service paramService, Handler paramHandler, Boolean paramBoolean,
                        int paramInt) {
        super(paramService);
        Log.d(TAG, "StopCallback()");
        this.handler = paramHandler;
        this.isRePlay = paramBoolean;
        this.type = paramInt;
    }

    public void failure(ActionInvocation paramActionInvocation, UpnpResponse paramUpnpResponse,
                        String paramString) {
        Log.d(TAG, "failure()");
        if (this.type == 1)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYIMAGEFAILED);
        if (this.type == 2)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYAUDIOFAILED);
        if (this.type == 3)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYVIDEOFAILED);
    }

    public void success(ActionInvocation paramActionInvocation) {
        super.success(paramActionInvocation);
        Log.d(TAG, "success()");
        if (!isRePlay.booleanValue()) {
            this.handler.sendEmptyMessage(DMCControlMessage.SETURL);
        } else {
            this.handler.sendEmptyMessage(DMCControlMessage.GETTRANSPORTINFO);
        }
    }

}
