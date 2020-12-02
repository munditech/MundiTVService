package tk.munditv.libtvservice.dmc.callback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.renderingcontrol.callback.GetMute;

import tk.munditv.libtvservice.dmc.DMCControlMessage;

public class GetMuteCallback extends GetMute {

	private final static String TAG = GetMuteCallback.class.getSimpleName();
	private Handler handler;

	public GetMuteCallback(Service paramService, Handler paramHandler) {
		super(paramService);
		Log.d(TAG, "GetMuteCallback()");

		this.handler = paramHandler;
	}

	public void failure(ActionInvocation paramActionInvocation,
                        UpnpResponse paramUpnpResponse, String paramString) {
		Log.d(TAG, "get mute failed");
	}

	public void received(ActionInvocation paramActionInvocation,
                         boolean paramBoolean) {
		Log.d(TAG, "received() : " + Boolean.toString(paramBoolean));

		Message localMessage = new Message();
		localMessage.what = DMCControlMessage.SETMUTE;
		Bundle localBundle = new Bundle();
		localBundle.putBoolean("mute", paramBoolean);
		localMessage.setData(localBundle);
		handler.sendMessage(localMessage);
	}

}
