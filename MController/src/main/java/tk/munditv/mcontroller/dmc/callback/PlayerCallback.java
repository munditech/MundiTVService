package tk.munditv.mcontroller.dmc.callback;

import android.os.Handler;
import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.Play;

import tk.munditv.mcontroller.dmc.DMCControlMessage;

public class PlayerCallback extends Play {

	private final static String TAG = PlayerCallback.class.getSimpleName();

	private Handler handler;

	public PlayerCallback(Service paramService, Handler paramHandler) {
		super(paramService);
		Log.d(TAG, "PlayerCallback()");
		this.handler = paramHandler;
	}

	public void failure(ActionInvocation paramActionInvocation,
                        UpnpResponse paramUpnpResponse, String paramString) {
		Log.d(TAG, "failure()");
		handler.sendEmptyMessage(DMCControlMessage.PLAYVIDEOFAILED);
	}

	public void run() {
		super.run();
	}

	public void success(ActionInvocation paramActionInvocation) {
		super.success(paramActionInvocation);
		Log.d(TAG, "success()");
		handler.sendEmptyMessage(DMCControlMessage.GETMEDIA);
	}

}
