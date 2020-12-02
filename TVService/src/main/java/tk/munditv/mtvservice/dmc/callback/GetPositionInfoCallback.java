package tk.munditv.mtvservice.dmc.callback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.model.PositionInfo;

import tk.munditv.libtvservice.util.Action;

public class GetPositionInfoCallback extends GetPositionInfo {

	private final static String TAG = GetPositionInfoCallback.class.getSimpleName();

	private Activity activity;
	private Handler handler;

	public GetPositionInfoCallback(Service paramService, Handler paramHandler,
                                   Activity paramActivity) {
		super(paramService);
		Log.d(TAG, "GetPositionInfoCallback()");
		this.handler = paramHandler;
		this.activity = paramActivity;
	}

	public void failure(ActionInvocation paramActionInvocation,
                        UpnpResponse paramUpnpResponse, String paramString) {
		Log.d(this.TAG, "failure()");
	}

	public void received(ActionInvocation paramActionInvocation,
                         PositionInfo paramPositionInfo) {
		Log.d(this.TAG, "received()");
		Bundle localBundle = new Bundle();
		localBundle.putString("TrackDuration",
				paramPositionInfo.getTrackDuration());
		localBundle.putString("RelTime", paramPositionInfo.getRelTime());
		Intent localIntent = new Intent(Action.PLAY_UPDATE);
		localIntent.putExtras(localBundle);
		activity.sendBroadcast(localIntent);
	}

	public void success(ActionInvocation paramActionInvocation) {
		super.success(paramActionInvocation);
		Log.d(this.TAG, "success()");
	}

}
