package tk.munditv.mtvservice.dmc.callback;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.Seek;

import tk.munditv.mtvservice.dmc.DMCControlMessage;

public class SeekCallback extends Seek {

	private String TAG = SeekCallback.class.getSimpleName();
	private Activity activity;
	private Handler mHandler;

	public SeekCallback(Activity paramActivity, Service paramService,
			String paramString, Handler paramHandler) {
		super(paramService, paramString);
		Log.d(this.TAG, "SeekCallback()");

		activity = paramActivity;
		mHandler = paramHandler;
	}

	public void failure(ActionInvocation paramActionInvocation,
                        UpnpResponse paramUpnpResponse, String paramString) {
		Log.d(this.TAG, "failed()");
	}

	public void sendBroadcast() {
		Log.d(this.TAG, "sendBroadcast()");
		Intent localIntent = new Intent("com.continue.display");
		this.activity.sendBroadcast(localIntent);
	}

	public void success(ActionInvocation paramActionInvocation) {
		super.success(paramActionInvocation);
		Log.d(this.TAG, "success()");
		mHandler.sendEmptyMessage(DMCControlMessage.GETPOTITION);
	}

}
