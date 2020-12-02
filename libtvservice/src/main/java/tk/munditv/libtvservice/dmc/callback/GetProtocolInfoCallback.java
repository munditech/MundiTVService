package tk.munditv.libtvservice.dmc.callback;

import android.os.Handler;
import android.util.Log;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.connectionmanager.callback.GetProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;

import tk.munditv.libtvservice.dmc.DMCControlMessage;

public class GetProtocolInfoCallback extends GetProtocolInfo {

	private String TAG = GetProtocolInfoCallback.class.getSimpleName();
	private Handler handler;
	private boolean hasType = false;
	private String requestPlayMimeType = "";

	public GetProtocolInfoCallback(Service paramService,
                                   ControlPoint paramControlPoint, String paramString,
                                   Handler paramHandler) {
		super(paramService, paramControlPoint);
		Log.d(TAG, "GetProtocolInfoCallback()");
		this.requestPlayMimeType = paramString;
		this.handler = paramHandler;
	}

	public void failure(ActionInvocation paramActionInvocation,
                        UpnpResponse paramUpnpResponse, String paramString) {
		Log.d(TAG, "GetProtocolInfo  failure");
		this.handler.sendEmptyMessage(DMCControlMessage.CONNECTIONFAILED);
	}

	public void received(ActionInvocation paramActionInvocation,
                         ProtocolInfos paramProtocolInfos1, ProtocolInfos paramProtocolInfos2) {
		Log.d(TAG, "received()");
		this.handler.sendEmptyMessage(DMCControlMessage.CONNECTIONSUCESSED);
		// TODO
	}
}
