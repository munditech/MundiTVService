package tk.munditv.mcontroller.dmc.callback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;

import tk.munditv.mcontroller.dmc.DMCControlMessage;

public class SetMuteCalllback extends SetMute {

	private final static String TAG = SetMuteCalllback.class.getSimpleName();

	private boolean desiredMute;
	private Handler handler;

	public SetMuteCalllback(Service paramService, boolean paramBoolean,
                            Handler paramHandler) {
		super(paramService, paramBoolean);
		Log.d(TAG, "SetMuteCalllback()");
		this.handler = paramHandler;
		this.desiredMute = paramBoolean;
	}

	public void failure(ActionInvocation paramActionInvocation,
                        UpnpResponse paramUpnpResponse, String paramString) {
		Log.d(TAG, "failure()");
	}

	public void success(ActionInvocation paramActionInvocation) {
		Log.d(TAG, "success()");
		super.success(paramActionInvocation);

		if (desiredMute) {
			desiredMute = false;
		}
		Message localMessage = new Message();
		localMessage.what = DMCControlMessage.SETMUTESUC;
		Bundle localBundle = new Bundle();
		localBundle.putBoolean("mute", desiredMute);
		localMessage.setData(localBundle);
		this.handler.sendMessage(localMessage);
	}
}
