/*
 * Copyright (C) 2014 zxt
 * RenderService.java
 * Description:
 * Author: zxt
 * Date:  2014-1-23 上午10:30:58
 */
package tk.munditv.mtvservice.dmr

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.fourthline.cling.android.AndroidUpnpService

class RenderService : Service() {
    private var isopen = false
    protected var mediaRenderer: MOSMediaRenderer? = null
    private val upnpService: AndroidUpnpService? = null
    fun closeMediaRenderer() {
//        try {
//            if (this.upnpService != null) {
//                this.upnpService.getRegistry().getProtocolFactory()
//                        .createSendingNotificationByebye(this.mediaRenderer.getDevice());
//                PlayListener.setMediaPlayer(null);
//                this.upnpService.getRegistry().removeDevice(this.mediaRenderer.getDevice());
//                this.mediaRenderer.setMainState(Boolean.valueOf(false));
//                this.mediaRenderer.closeDevices();
//                this.mediaRenderer = null;
//            }
//            return;
//        } catch (Exception localException) {
//            while (true)
//                localException.printStackTrace();
//        }
    }

    override fun onBind(paramIntent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        isopen = false
        closeMediaRenderer()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        onStart2(intent, startId)
        return super.onStartCommand(intent, flags, startId)
    }

    fun onStart2(paramIntent: Intent?, paramInt: Int) {
        super.onStart(paramIntent, paramInt)
        if (!isopen) {
            isopen = true
            // new Thread(new RenderServices.1(this)).start();
        }
    }

    companion object {
        private val TAG = RenderService::class.java.simpleName
        const val SUPPORTED_INSTANCES = 1
    }
}