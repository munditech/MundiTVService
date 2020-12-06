package tk.munditv.mtvservice.popup

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.LinearLayout

class PopupWindow {
    private var mContext: Context? = null
    private var mWindowManager: WindowManager? = null
    private var mParams: WindowManager.LayoutParams? = null
    private val mADVISTORFrameLayout: LinearLayout? = null
    private var mLayoutinflater: LayoutInflater? = null
    var left = PopupWindowConstant.WINDOW_X
        private set
    var top = PopupWindowConstant.WINDOW_Y
        private set
    var width = PopupWindowConstant.WINDOW_WIDTH
        private set
    var height = PopupWindowConstant.WINDOW_HEIGHT
        private set
    var gravity = PopupWindowConstant.WINDOW_GRAVITY
    var format = PopupWindowConstant.WINDOW_FORNAT
    var type = PopupWindowConstant.WINDOW_TYPE
    var flags = PopupWindowConstant.WINDOW_FLAGS

    constructor(context: Context?) {
        initialize(context)
        return
    }

    constructor(context: Context?, left: Int, top: Int, width: Int, height: Int) {
        this.left = left
        this.top = top
        this.width = width
        this.height = height
        initialize(context)
    }

    fun initialize(context: Context?) {
        mContext = context
        mWindowManager = mContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mLayoutinflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mParams = WindowManager.LayoutParams()
        return
    }

    companion object {
        private val TAG = PopupWindow::class.java.simpleName
    }
}