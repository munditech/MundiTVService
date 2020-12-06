package tk.munditv.mtvservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive()")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "tk.munditv.mtvservice.action.START") {
            Log.d(TAG, "intent.getAction() is equals to " + intent.action)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, MainService::class.java))
            } else {
                context.startService(Intent(context, MainService::class.java))
            }
        }
        return
    } // end of onReceive()

    companion object {
        private val TAG = BootUpReceiver::class.java.simpleName
    }
}