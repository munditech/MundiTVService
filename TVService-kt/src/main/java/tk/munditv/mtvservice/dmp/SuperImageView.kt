package tk.munditv.mtvservice.dmp

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SuperImageView : AppCompatImageView {
    var imageW = 0f
    var imageH = 0f
    var rotatedImageW = 0f
    var rotatedImageH = 0f
    var viewW = 0f
    var viewH = 0f
    //var matrix = Matrix()
    var savedMatrix = Matrix()
    var mode = NONE
    var pA = PointF()
    var pB = PointF()
    var mid = PointF()
    var lastClickPos = PointF()
    var lastClickTime: Long = 0
    var rotation = 0.0
    var dist = 1f

    constructor(context: Context?) : super(context!!) {
        Log.d(TAG, "SuperImageView()")
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        Log.d(TAG, "SuperImageView()")
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) {
        Log.d(TAG, "SuperImageView()")
        init()
    }

    private fun init() {
        Log.d(TAG, "init()")
        scaleType = ScaleType.MATRIX
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        Log.d(TAG, "setImageBitmap()")
        setImageWidthHeight()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        Log.d(TAG, "setImageDrawable()")
        setImageWidthHeight()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        Log.d(TAG, "setImageResource()")
        setImageWidthHeight()
    }

    private fun setImageWidthHeight() {
        Log.d(TAG, "setImageWidthHeight()")
        val d = drawable ?: return
        rotatedImageW = d.intrinsicWidth.toFloat()
        imageW = rotatedImageW
        rotatedImageH = d.intrinsicHeight.toFloat()
        imageH = rotatedImageH
        initImage()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "onSizeChanged()")
        viewW = w.toFloat()
        viewH = h.toFloat()
        if (oldw == 0) {
            initImage()
        } else {
            fixScale()
            fixTranslation()
            imageMatrix = matrix
        }
    }

    private fun initImage() {
        Log.d(TAG, "initImage()")
        if (viewW <= 0 || viewH <= 0 || imageW <= 0 || imageH <= 0) {
            return
        }
        mode = NONE
        matrix.setScale(0f, 0f)
        fixScale()
        fixTranslation()
        imageMatrix = matrix
    }

    private fun fixScale() {
        Log.d(TAG, "fixScale()")
        val p = FloatArray(9)
        matrix.getValues(p)
        val curScale = Math.abs(p[0]) + Math.abs(p[1])
        val minScale = Math.min(viewW / rotatedImageW,
                viewH / rotatedImageH)
        if (curScale < minScale) {
            if (curScale > 0) {
                val scale = (minScale / curScale).toDouble()
                p[0] = (p[0] * scale).toFloat()
                p[1] = (p[1] * scale).toFloat()
                p[3] = (p[3] * scale).toFloat()
                p[4] = (p[4] * scale).toFloat()
                matrix.setValues(p)
            } else {
                matrix.setScale(minScale, minScale)
            }
        }
    }

    private fun maxPostScale(): Float {
        Log.d(TAG, "maxPostScale()")
        val p = FloatArray(9)
        matrix.getValues(p)
        val curScale = Math.abs(p[0]) + Math.abs(p[1])
        val minScale = Math.min(viewW / rotatedImageW,
                viewH / rotatedImageH)
        val maxScale = Math.max(minScale, MAX_SCALE)
        return maxScale / curScale
    }

    private fun fixTranslation() {
        Log.d(TAG, "fixTranslation()")
        val rect = RectF(0f, 0f, imageW, imageH)
        matrix.mapRect(rect)
        val height = rect.height()
        val width = rect.width()
        var deltaX = 0f
        var deltaY = 0f
        if (width < viewW) {
            deltaX = (viewW - width) / 2 - rect.left
        } else if (rect.left > 0) {
            deltaX = -rect.left
        } else if (rect.right < viewW) {
            deltaX = viewW - rect.right
        }
        if (height < viewH) {
            deltaY = (viewH - height) / 2 - rect.top
        } else if (rect.top > 0) {
            deltaY = -rect.top
        } else if (rect.bottom < viewH) {
            deltaY = viewH - rect.bottom
        }
        matrix.postTranslate(deltaX, deltaY)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onTouchEvent()")
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                pA[event.x] = event.y
                pB[event.x] = event.y
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.actionIndex > 1) return true
                dist = spacing(event.getX(0), event.getY(0), event.getX(1),
                        event.getY(1))
                // 如果连续两点距离大于10，则判定为多点模式
                if (dist > 10f) {
                    savedMatrix.set(matrix)
                    pA[event.getX(0)] = event.getY(0)
                    pB[event.getX(1)] = event.getY(1)
                    mid[(event.getX(0) + event.getX(1)) / 2] = (event.getY(0) + event.getY(1)) / 2
                    mode = ZOOM_OR_ROTATE
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (mode == DRAG) {
                    if (spacing(pA.x, pA.y, pB.x, pB.y) < 50) {
                        var now = System.currentTimeMillis()
                        if (now - lastClickTime < 500
                                && spacing(pA.x, pA.y, lastClickPos.x,
                                        lastClickPos.y) < 50) {
                            doubleClick(pA.x, pA.y)
                            now = 0
                        }
                        lastClickPos.set(pA)
                        lastClickTime = now
                    }
                } else if (mode == ROTATE) {
                    var level = Math.floor((rotation + Math.PI / 4)
                            / (Math.PI / 2)).toInt()
                    if (level == 4) level = 0
                    matrix.set(savedMatrix)
                    matrix.postRotate((90 * level).toFloat(), mid.x, mid.y)
                    if (level == 1 || level == 3) {
                        val tmp = rotatedImageW
                        rotatedImageW = rotatedImageH
                        rotatedImageH = tmp
                        fixScale()
                    }
                    fixTranslation()
                    imageMatrix = matrix
                }
                mode = NONE
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == ZOOM_OR_ROTATE) {
                    val pC = PointF(event.getX(1) - event.getX(0) + pA.x,
                            event.getY(1) - event.getY(0) + pA.y)
                    val a = spacing(pB.x, pB.y, pC.x, pC.y).toDouble()
                    val b = spacing(pA.x, pA.y, pC.x, pC.y).toDouble()
                    val c = spacing(pA.x, pA.y, pB.x, pB.y).toDouble()
                    if (a >= 10) {
                        val cosB = (a * a + c * c - b * b) / (2 * a * c)
                        val angleB = Math.acos(cosB)
                        val PID4 = Math.PI / 4
                        if (angleB > PID4 && angleB < 3 * PID4) {
                            mode = ROTATE
                            rotation = 0.0
                        } else {
                            mode = ZOOM
                        }
                    }
                }
                if (mode == DRAG) {
                    matrix.set(savedMatrix)
                    pB[event.x] = event.y
                    matrix.postTranslate(event.x - pA.x, event.y - pA.y)
                    fixTranslation()
                    imageMatrix = matrix
                } else if (mode == ZOOM) {
                    val newDist = spacing(event.getX(0), event.getY(0),
                            event.getX(1), event.getY(1))
                    if (newDist > 10f) {
                        matrix.set(savedMatrix)
                        val tScale = Math.min(newDist / dist, maxPostScale())
                        matrix.postScale(tScale, tScale, mid.x, mid.y)
                        fixScale()
                        fixTranslation()
                        imageMatrix = matrix
                    }
                } else if (mode == ROTATE) {
                    val pC = PointF(event.getX(1) - event.getX(0) + pA.x,
                            event.getY(1) - event.getY(0) + pA.y)
                    val a = spacing(pB.x, pB.y, pC.x, pC.y).toDouble()
                    val b = spacing(pA.x, pA.y, pC.x, pC.y).toDouble()
                    val c = spacing(pA.x, pA.y, pB.x, pB.y).toDouble()
                    if (b > 10) {
                        val cosA = (b * b + c * c - a * a) / (2 * b * c)
                        var angleA = Math.acos(cosA)
                        val ta = (pB.y - pA.y).toDouble()
                        val tb = (pA.x - pB.x).toDouble()
                        val tc = (pB.x * pA.y - pA.x * pB.y).toDouble()
                        val td = ta * pC.x + tb * pC.y + tc
                        if (td > 0) {
                            angleA = 2 * Math.PI - angleA
                        }
                        rotation = angleA
                        matrix.set(savedMatrix)
                        matrix.postRotate((rotation * 180 / Math.PI).toFloat(),
                                mid.x, mid.y)
                        imageMatrix = matrix
                    }
                }
            }
        }
        return true
    }

    /**
     * 两点的距离
     */
    private fun spacing(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        Log.d(TAG, "spacing()")
        val x = x1 - x2
        val y = y1 - y2
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun doubleClick(x: Float, y: Float) {
        Log.d(TAG, "doubleClick()")
        val p = FloatArray(9)
        matrix.getValues(p)
        val curScale = Math.abs(p[0]) + Math.abs(p[1])
        val minScale = Math.min(viewW / rotatedImageW,
                viewH / rotatedImageH)
        if (curScale <= minScale + 0.01) { // 放大
            val toScale = Math.max(minScale, MAX_SCALE) / curScale
            matrix.postScale(toScale, toScale, x, y)
        } else { // 缩小
            val toScale = minScale / curScale
            matrix.postScale(toScale, toScale, x, y)
            fixTranslation()
        }
        imageMatrix = matrix
    }

    fun createImage(path: String?) {
        Log.d(TAG, "createImage()")
        val bitmap = Bitmap.createBitmap(rotatedImageW.toInt(), rotatedImageH.toInt(),
                Bitmap.Config.ARGB_8888) // 背景图片
        val canvas = Canvas(bitmap) // 新建画布
        canvas.drawBitmap(this.drawingCache, matrix, null) // 画图片
        canvas.save() // 保存画布
        canvas.restore()
        val saveFile = File(path)
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile()
                val fileOutputStream = FileOutputStream(saveFile)
                if (fileOutputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream)
                }
                fileOutputStream.flush()
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private val TAG = SuperImageView::class.java.simpleName
        const val MAX_SCALE = 2.0f
        const val NONE = 0 // 初始状态
        const val DRAG = 1 // 拖动
        const val ZOOM = 2 // 缩放
        const val ROTATE = 3 // 旋转
        const val ZOOM_OR_ROTATE = 4 // 缩放或旋转
    }
}