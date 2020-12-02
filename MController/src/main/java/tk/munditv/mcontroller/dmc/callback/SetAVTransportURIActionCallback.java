
package tk.munditv.mcontroller.dmc.callback;

import android.os.Handler;
import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;

import tk.munditv.mcontroller.dmc.DMCControlMessage;

public class SetAVTransportURIActionCallback extends SetAVTransportURI {

    private final static String TAG = SetAVTransportURIActionCallback.class.getSimpleName();

    private Handler handler;
    private int type;

    public SetAVTransportURIActionCallback(Service paramService, String paramString1,
                                           String paramString2, Handler paramHandler, int paramInt) {
        super(paramService, paramString1, paramString2);
        Log.d(TAG, "SetAVTransportURIActionCallback()");
        this.handler = paramHandler;
    }

    @Override
    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        Log.d(TAG, "failure()");
        if (this.type == 1)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYIMAGEFAILED);
        if (this.type == 2)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYAUDIOFAILED);
        if (this.type == 3)
            this.handler.sendEmptyMessage(DMCControlMessage.PLAYVIDEOFAILED);
    }

    @Override
    public void success(ActionInvocation paramActionInvocation) {
        super.success(paramActionInvocation);
        Log.d(TAG, "success()");

        try {
            Thread.sleep(2000L);
            this.handler.sendEmptyMessage(DMCControlMessage.PLAY);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

}
