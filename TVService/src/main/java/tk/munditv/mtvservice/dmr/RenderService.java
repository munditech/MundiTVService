/*
 * Copyright (C) 2014 zxt
 * RenderService.java
 * Description:
 * Author: zxt
 * Date:  2014-1-23 上午10:30:58
 */

package tk.munditv.mtvservice.dmr;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.fourthline.cling.android.AndroidUpnpService;

public class RenderService extends Service {

    private final static String TAG = RenderService.class.getSimpleName();

    public static final int SUPPORTED_INSTANCES = 1;
    private boolean isopen = false;
    protected MOSMediaRenderer mediaRenderer = null;
    private AndroidUpnpService upnpService = null;

    public void closeMediaRenderer() {
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

    public IBinder onBind(Intent paramIntent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();
        this.isopen = false;
        closeMediaRenderer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart2(intent, startId);
        return super.onStartCommand(intent, flags, startId);
    }

    public void onStart2(Intent paramIntent, int paramInt) {
        super.onStart(paramIntent, paramInt);
        if (!this.isopen) {
            this.isopen = true;
            // new Thread(new RenderServices.1(this)).start();
        }
    }
}
