package tk.munditv.mtvservice.popup;

import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

public class PopupWindowConstant {
    public final static int WINDOW_X = 700;
    public final static int WINDOW_Y = 100;
    public final static int WINDOW_WIDTH = 700;
    public final static int WINDOW_HEIGHT = 800;
    public final static int WINDOW_GRAVITY = Gravity.TOP;
    public final static int WINDOW_FORNAT = PixelFormat.TRANSLUCENT;
    public final static int WINDOW_TYPE = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
    public final static int WINDOW_FLAGS = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
}
