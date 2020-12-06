/*
 * RenderPlayerService.java
 * Description:
 * Author: zxt
 */
package tk.munditv.mtvservice.dmr

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import tk.munditv.libtvservice.util.Action
import tk.munditv.mtvservice.dmp.GPlayer
import tk.munditv.mtvservice.dmp.ImageDisplay

class RenderPlayerService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind()")
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        onStart2(intent, startId)
        return super.onStartCommand(intent, flags, startId)
    }

    fun onStart2(intent: Intent?, startId: Int) {
        Log.d(TAG, "onStart2()")

        //xgf fix bug null point
        if (null != intent) {
            super.onStart(intent, startId)
            val type = intent.getStringExtra("type")
            val intent2: Intent
            if (type == "audio") {
                // new Thread(new RenderPlayerService.1(this,
                // intent.getStringExtra("playURI"),
                // intent.getStringExtra("name"))).start();
                intent2 = Intent(this, GPlayer::class.java)
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent2.putExtra("name", intent.getStringExtra("name"))
                intent2.putExtra("playURI", intent.getStringExtra("playURI"))
                startActivity(intent2)
            } else if (type == "video") {
                intent2 = Intent(this, GPlayer::class.java)
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent2.putExtra("name", intent.getStringExtra("name"))
                intent2.putExtra("playURI", intent.getStringExtra("playURI"))
                startActivity(intent2)
            } else if (type == "image") {
                intent2 = Intent(this, ImageDisplay::class.java)
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent2.putExtra("name", intent.getStringExtra("name"))
                intent2.putExtra("playURI", intent.getStringExtra("playURI"))
                intent2.putExtra("isRender", true)
                startActivity(intent2)
            } else {
                intent2 = Intent(Action.DMR)
                intent2.putExtra("playpath", intent.getStringExtra("playURI"))
                sendBroadcast(intent2)
            }
        }
    }

    companion object {
        private val TAG = RenderPlayerService::class.java.simpleName
    }
}