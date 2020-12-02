package tk.munditv.mtvservice.popup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;

import tk.munditv.mtvservice.activity.ControlActivity;

public class PopupWindow {
    private final static String TAG = PopupWindow.class.getSimpleName();

    private Context mContext;
    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams  mParams = null;
    private LinearLayout mADVISTORFrameLayout = null;
    private LayoutInflater mLayoutinflater = null;

    private int left = PopupWindowConstant.WINDOW_X;
    private int top = PopupWindowConstant.WINDOW_Y;
    private int width = PopupWindowConstant.WINDOW_WIDTH;
    private int height = PopupWindowConstant.WINDOW_HEIGHT;
    private int gravity = PopupWindowConstant.WINDOW_GRAVITY;
    private int format = PopupWindowConstant.WINDOW_FORNAT;
    private int type = PopupWindowConstant.WINDOW_TYPE;
    private int flags = PopupWindowConstant.WINDOW_FLAGS;

    public PopupWindow(Context context) {
        initialize(context);
        return;
    }

    public PopupWindow(Context context, int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        initialize(context);
    }

    public void initialize(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutinflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mParams = new WindowManager.LayoutParams();
        return;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public int getGravity() {
        return gravity;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getFormat() {
        return format;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getFlags() {
        return flags;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
