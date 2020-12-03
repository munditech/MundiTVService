package tk.munditv.mtvservice;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class RCKeyService extends AccessibilityService {

    private final static String TAG = RCKeyService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent()");
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt()");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.d(TAG, "onKeyEvent() keycode = " + event.getKeyCode());

        return super.onKeyEvent(event);
    }
}
