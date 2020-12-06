package tk.munditv.mtvservice.dmp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.FailReason.FailType
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import org.fourthline.cling.android.AndroidUpnpService
import tk.munditv.libtvservice.dmc.DMCControl
import tk.munditv.libtvservice.dmc.GenerateXml
import tk.munditv.libtvservice.dmp.ContentItem
import tk.munditv.libtvservice.dmp.DeviceItem
import tk.munditv.libtvservice.util.*
import tk.munditv.mtvservice.R
import java.io.File
import java.util.*

class ImageDisplay : AppCompatActivity(), View.OnClickListener, OnTouchListener {
    private var mode = NONE
    private var oldDist = 0f
    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private val start = PointF()
    private val mid = PointF()
    private var mImageView: SuperImageView? = null
    private var mPreBtn: Button? = null
    private var mNextBtn: Button? = null
    private var mDownloadBtn: Button? = null
    private var mSharedBtn: Button? = null
    private var mSlideBtn: Button? = null
    private var mRotateBtn: Button? = null
    private var mButtonLayout: LinearLayout? = null
    private var mPlayUri: String? = null
    private var currentContentFormatMimeType: String? = ""
    private var metaData: String? = ""
    private var dmrDeviceItem: DeviceItem? = null
    private var isLocalDmr = true
    private var dmcControl: DMCControl? = null
    private var upnpService: AndroidUpnpService? = null
    private var mListPhotos = ArrayList<ContentItem>()
    private var mSpinner: ProgressBar? = null
    var options: DisplayImageOptions? = null
    private var mCurrentPosition = 0
    private var isSlidePlaying = false

    @Volatile
    private var mCurrentBitmap: Bitmap? = null
    private var mContext: Context? = null
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_SLIDE_START -> {
                    if (!nextImage()) {
                        var time = NetworkData.getSlideTime()
                        if (time < 5) {
                            time = 5
                        }
                        sendEmptyMessageDelayed(MSG_SLIDE_START, (
                                time * 1000).toLong())
                    }
                }
                else -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_display)
        mContext = this
        options = DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error).resetViewBeforeLoading()
                .cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(FadeInBitmapDisplayer(300)).build()
        initView()
        initData()
        showImage(mPlayUri)
        addShake()
    }

    private fun initView() {
        mImageView = findViewById<View>(R.id.imageView) as SuperImageView
        mPreBtn = findViewById<View>(R.id.preButton) as Button
        mNextBtn = findViewById<View>(R.id.nextButton) as Button
        mButtonLayout = findViewById<View>(R.id.buttonLayout) as LinearLayout
        mPreBtn!!.setOnClickListener(this)
        mNextBtn!!.setOnClickListener(this)
        mImageView!!.setOnTouchListener(this)
        mSpinner = findViewById<View>(R.id.loading) as ProgressBar
        mDownloadBtn = findViewById<View>(R.id.downloadButton) as Button
        mDownloadBtn!!.setOnClickListener(this)
        mSharedBtn = findViewById<View>(R.id.sharedButton) as Button
        mSharedBtn!!.setOnClickListener(this)
        mSlideBtn = findViewById<View>(R.id.slideButton) as Button
        mSlideBtn!!.setOnClickListener(this)
        mRotateBtn = findViewById<View>(R.id.rotateButton) as Button
        mRotateBtn!!.setOnClickListener(this)
    }

    private fun initData() {
        val localIntent = intent
        mPlayUri = localIntent.getStringExtra("playURI")
        mCurrentPosition = ConfigData.photoPosition
        mListPhotos = ConfigData.listPhotos
        dmrDeviceItem = NetworkData.getDmrDeviceItem()
        upnpService = NetworkData.getUpnpService()
        isLocalDmr = NetworkData.getIsLocalDmr()
        if (!isLocalDmr) {
            currentContentFormatMimeType = localIntent
                    .getStringExtra("currentContentFormatMimeType")
            metaData = localIntent.getStringExtra("metaData")
            dmcControl = DMCControl(this, 1, dmrDeviceItem, upnpService,
                    mPlayUri, metaData, null)
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.preButton) {
            prevImage()
        } else if (id == R.id.nextButton) {
            nextImage()
        } else if (id == R.id.slideButton) {
            if (!isSlidePlaying) {
                isSlidePlaying = true
                mSlideBtn!!.setBackgroundResource(R.drawable.ic_slide_pause)
                mHandler.sendEmptyMessageDelayed(MSG_SLIDE_START, 5000)
                Toast.makeText(mContext, R.string.info_image_slide_start,
                        Toast.LENGTH_SHORT).show()
            } else {
                isSlidePlaying = false
                mSlideBtn!!.setBackgroundResource(R.drawable.ic_slide_start)
                mHandler.removeMessages(MSG_SLIDE_START)
                Toast.makeText(mContext, R.string.info_image_slide_pause,
                        Toast.LENGTH_SHORT).show()
            }
        } else if (id == R.id.downloadButton) {
            val path = saveCurrentBitmap()
            if (!TextUtils.isEmpty(path)) {
                Toast.makeText(
                        mContext,
                        mContext!!.getString(R.string.info_download_image) + path,
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext, R.string.info_download_image_error,
                        Toast.LENGTH_SHORT).show()
            }
        } else if (id == R.id.sharedButton) {
            val path = saveCurrentBitmap()
            if (!TextUtils.isEmpty(path)) {
                share(Uri.parse(path))
            }
        }
    }

    private fun nextImage(): Boolean {
        val isLast: Boolean
        if (mCurrentPosition >= mListPhotos.size - 1) {
            isLast = true
            Toast.makeText(this@ImageDisplay, R.string.info_last_image,
                    Toast.LENGTH_SHORT).show()
        } else {
            isLast = false
            mCurrentPosition = mCurrentPosition + 1
            val uri = mListPhotos[mCurrentPosition]
                    .item.firstResource.value
            if (!TextUtils.isEmpty(uri)) {
                mPlayUri = uri
                showImage(mPlayUri)
                if (!isLocalDmr) {
                    dmcControl!!.stop(true)
                    try {
                        dmcControl!!.setCurrentPlayPath(mPlayUri,
                                GenerateXml()
                                        .generate(mListPhotos[mCurrentPosition]))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    dmcControl!!.getProtocolInfos(currentContentFormatMimeType)
                }
            }
        }
        return isLast
    }

    private fun prevImage(): Boolean {
        val isFirst: Boolean
        if (mCurrentPosition == 0) {
            isFirst = true
            Toast.makeText(this@ImageDisplay, R.string.info_first_image,
                    Toast.LENGTH_SHORT).show()
        } else {
            isFirst = false
            mCurrentPosition = mCurrentPosition - 1
            val uri = mListPhotos[mCurrentPosition]
                    .item.firstResource.value
            if (!TextUtils.isEmpty(uri)) {
                mPlayUri = uri
                showImage(mPlayUri)
                if (!isLocalDmr) {
                    dmcControl!!.stop(true)
                    try {
                        dmcControl!!.setCurrentPlayPath(mPlayUri,
                                GenerateXml()
                                        .generate(mListPhotos[mCurrentPosition]))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    dmcControl!!.getProtocolInfos(currentContentFormatMimeType)
                }
            }
        }
        return isFirst
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isLocalDmr) {
            dmcControl!!.stop(true)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isLocalDmr) {
            dmcControl!!.stop(true)
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val view = v as ImageView
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                if (mButtonLayout!!.visibility == View.VISIBLE) {
                    mButtonLayout!!.visibility = View.GONE
                } else {
                    mButtonLayout!!.visibility = View.VISIBLE
                }
                start[event.x] = event.y
                mode = DRAG
            }
            MotionEvent.ACTION_UP -> if (mode == DRAG) {
                if (event.x - start.x > 100) {
                    // go to prev pic
                    prevImage()
                } else if (event.x - start.x < -100) {
                    // go to next pic
                    nextImage()
                }
            }
            MotionEvent.ACTION_POINTER_UP -> mode = NONE
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    midPoint(mid, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_MOVE -> {
            }
        }

        // view.setImageMatrix(matrix);
        return false
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    private fun showImage(url: String?) {
        fetchBitmap2(url)
        if (!isLocalDmr) {
            try {
                dmcControl!!.setCurrentPlayPath(mPlayUri, GenerateXml()
                        .generate(mListPhotos[mCurrentPosition]))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dmcControl!!.getProtocolInfos(currentContentFormatMimeType)
        }
    }

    private fun fetchBitmap2(url: String?) {
        ImageLoader.getInstance().displayImage(url, mImageView, options,
                object : SimpleImageLoadingListener() {
                    override fun onLoadingStarted(imageUri: String, view: View) {
                        mSpinner!!.visibility = View.VISIBLE
                    }

                    override fun onLoadingFailed(imageUri: String, view: View,
                                                 failReason: FailReason) {
                        var message = R.string.network_denied
                        message = when (failReason.type) {
                            FailType.IO_ERROR -> R.string.io_error
                            FailType.DECODING_ERROR -> R.string.decoding_error
                            FailType.NETWORK_DENIED -> R.string.network_denied
                            FailType.OUT_OF_MEMORY -> R.string.oom_error
                            FailType.UNKNOWN -> R.string.unknown_error
                        }
                        Toast.makeText(this@ImageDisplay, message,
                                Toast.LENGTH_SHORT).show()
                        mSpinner!!.visibility = View.GONE
                    }

                    override fun onLoadingComplete(imageUri: String, view: View,
                                                   loadedImage: Bitmap) {
                        mSpinner!!.visibility = View.GONE
                        mCurrentBitmap = loadedImage
                    }
                })
    }

    private fun saveCurrentBitmap(): String {
        var path = ""
        if (null != mCurrentBitmap && !mCurrentBitmap!!.isRecycled) {
            if (null != FileUtil.getSDPath()) {
                var filename = mPlayUri!!.substring(mPlayUri!!.lastIndexOf("/"))
                if (FileUtil.getFileSuffix(filename) == "") {
                    filename = "$filename.jpg"
                }
                path = FileUtil.getSDPath() + FileUtil.IMAGE_DOWNLOAD_PATH
                val path1 = File(path)
                if (!path1.exists()) {
                    path1.mkdirs()
                }
                path = path + filename
                try {
                    ImageUtil
                            .saveBitmapWithFilePathSuffix(mCurrentBitmap, path)
                } catch (e: Exception) {
                    path = ""
                    Log.w(TAG, "saveCurrentBitmap", e)
                }
            }
        }
        return path
    }

    private fun share(uri: Uri?) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        if (uri != null) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/*"
            startActivity(Intent.createChooser(shareIntent,
                    getText(R.string.info_share_image)))
        }
    }

    private fun addShake() {
        val shakeListener = ShakeListener(this)
        shakeListener.setOnShakeListener { nextImage() }
    }

    companion object {
        private val TAG = ImageDisplay::class.java.simpleName
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
        protected const val MSG_SLIDE_START = 1000
        fun playImage(context: Context, url: String?) {
            val intent = Intent()
            intent.setClass(context, ImageDisplay::class.java)
            intent.putExtra("playURI", url)
            context.startActivity(intent)
            Log.d(TAG, url!!)
        }
    }
}