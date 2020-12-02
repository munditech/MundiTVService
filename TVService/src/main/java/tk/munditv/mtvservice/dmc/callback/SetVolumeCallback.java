package tk.munditv.mtvservice.dmc.callback;

import android.util.Log;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

public class SetVolumeCallback extends SetVolume {

    private final static String TAG = SetVolumeCallback.class.getSimpleName();

	public SetVolumeCallback(Service paramService, long paramLong) {
      super(paramService, paramLong);
      Log.d(TAG, "SetVolumeCallback()");
    }

    public void failure(ActionInvocation paramActionInvocation,
                        UpnpResponse paramUpnpResponse, String paramString) {
      Log.d(TAG, "set volume failed");
    }

    public void success(ActionInvocation paramActionInvocation) {
      super.success(paramActionInvocation);
      Log.d(TAG, "set volume success");
    }

}
