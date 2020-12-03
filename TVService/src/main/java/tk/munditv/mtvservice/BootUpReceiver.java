package tk.munditv.mtvservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver {

    private final static String TAG = BootUpReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
        intent.getAction().equals("tk.munditv.mtvservice.action.START")) {
            Log.d(TAG, "intent.getAction() is equals to " + intent.getAction());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, MainService.class));
            } else {
                context.startService(new Intent(context, MainService.class));
            }
        }
        return;
    } // end of onReceive()
}
