package tk.munditv.mtvservice.popup

import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager

object PopupWindowConstant {
    const val WINDOW_X = 700
    const val WINDOW_Y = 100
    const val WINDOW_WIDTH = 700
    const val WINDOW_HEIGHT = 800
    const val WINDOW_GRAVITY = Gravity.TOP
    const val WINDOW_FORNAT = PixelFormat.TRANSLUCENT
    const val WINDOW_TYPE = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
    const val WINDOW_FLAGS = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
}