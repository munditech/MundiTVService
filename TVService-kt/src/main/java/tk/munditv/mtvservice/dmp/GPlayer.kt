package tk.munditv.mtvservice.dmp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.MediaController.MediaPlayerControl
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import tk.munditv.libtvservice.util.Action
import tk.munditv.libtvservice.util.Utils
import tk.munditv.mtvservice.R
import java.io.IOException

class GPlayer : AppCompatActivity(), OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, OnPreparedListener, OnSeekCompleteListener, OnVideoSizeChangedListener, SurfaceHolder.Callback, MediaPlayerControl, View.OnClickListener {
    var currentDisplay: Display? = null
    var surfaceView: SurfaceView? = null
    lateinit var surfaceHolder: SurfaceHolder
    var mMediaPlayer: MediaPlayer? = null
    var mediaController: MediaController? = null
    var videoWidth = 0
    var videoHeight = 0
    var readyToPlay = false
    var playURI: String? = null
    private var mAudioManager: AudioManager? = null
    private var mTextViewTime: TextView? = null
    private var mSeekBarProgress: SeekBar? = null
    private var mTextViewLength: TextView? = null
    private var mPauseButton: ImageButton? = null
    private var mProgressBarPreparing: ProgressBar? = null
    private var mTextProgress: TextView? = null
    private var mTextInfo: TextView? = null
    private var mBufferLayout: RelativeLayout? = null
    private var mLayoutBottom: LinearLayout? = null
    private var mLayoutTop: RelativeLayout? = null
    private var mVideoTitle: TextView? = null
    private var mLeftButton: Button? = null
    private var mRightButton: Button? = null
    private var mSound: ImageView? = null
    private var mSeekBarSound: SeekBar? = null

    //@Volatile
    private val mCanSeek = true
    private var isMute = false
    private var mBackCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gplayer)
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        surfaceView = findViewById<View>(R.id.gplayer_surfaceview) as SurfaceView
        surfaceHolder = surfaceView!!.holder
        surfaceHolder.addCallback(this)
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        mMediaPlayer = MediaPlayer()
        mMediaPlayer!!.setOnCompletionListener(this)
        mMediaPlayer!!.setOnErrorListener(this)
        mMediaPlayer!!.setOnInfoListener(this)
        mMediaPlayer!!.setOnPreparedListener(this)
        mMediaPlayer!!.setOnSeekCompleteListener(this)
        mMediaPlayer!!.setOnVideoSizeChangedListener(this)
        initControl()
        val intent = intent
        playURI = intent.getStringExtra("playURI")
        if (!TextUtils.isEmpty(playURI)) {
            setUri(playURI)
        }
        setTitle(intent)
        currentDisplay = windowManager.defaultDisplay
        registerBrocast()
    }

    private fun setTitle(intent: Intent) {
        val name = intent.getStringExtra("name")
        if (!TextUtils.isEmpty(name)) {
            mVideoTitle!!.text = name
        }
    }

    private fun initControl() {
        mediaController = MediaController(this)
        mBufferLayout = findViewById<View>(R.id.buffer_info) as RelativeLayout
        mProgressBarPreparing = findViewById<View>(R.id.player_prepairing) as ProgressBar
        mTextProgress = findViewById<View>(R.id.prepare_progress) as TextView
        mTextInfo = findViewById<View>(R.id.info) as TextView
        mLayoutTop = findViewById<View>(R.id.layout_top) as RelativeLayout
        mVideoTitle = findViewById<View>(R.id.video_title) as TextView
        mLeftButton = findViewById<View>(R.id.topBar_back) as Button
        mRightButton = findViewById<View>(R.id.topBar_list_switch) as Button
        mLeftButton!!.setOnClickListener(this)
        mRightButton!!.setOnClickListener(this)
        mTextViewTime = findViewById<View>(R.id.current_time) as TextView
        mTextViewLength = findViewById<View>(R.id.totle_time) as TextView
        mPauseButton = findViewById<View>(R.id.play) as ImageButton
        mPauseButton!!.setOnClickListener(this)
        mLayoutBottom = findViewById<View>(R.id.layout_control) as LinearLayout
        mTextProgress = findViewById<View>(R.id.prepare_progress) as TextView
        mTextInfo = findViewById<View>(R.id.info) as TextView
        mSeekBarProgress = findViewById<View>(R.id.seekBar_progress) as SeekBar
        mSeekBarProgress!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val id = seekBar.id
                if (id == R.id.seekBar_progress) {
                    if (mCanSeek) {
                        val position = seekBar.progress
                        if (mMediaPlayer != null) {
                            mMediaPlayer!!.seekTo(position)
                        }
                    }
                }
            }
        })
        mSound = findViewById<View>(R.id.sound) as ImageView
        mSound!!.setOnClickListener(this)
        mSeekBarSound = findViewById<View>(R.id.seekBar_sound) as SeekBar
        mSeekBarSound!!.max = mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        mSeekBarSound!!.progress = mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        mSeekBarSound!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    override fun onNewIntent(intent: Intent) {
        playURI = intent.getStringExtra("playURI")
        if (!TextUtils.isEmpty(playURI)) {
            setUri(playURI)
        }
        setTitle(intent)
        super.onNewIntent(intent)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.pause()
        }
        super.onStop()
    }

    override fun onDestroy() {
        exit()
        unregisterBrocast()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mBackCount > 0) {
                exit()
            } else {
                mBackCount++
                Toast.makeText(this, R.string.player_exit, Toast.LENGTH_SHORT).show()
            }
            return true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100)
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100)
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun exit() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
        mMediaListener = null
        finish()
    }

    override fun onClick(v: View) {
        // if (!mMediaPlayerLoaded)
        // return;
        val id = v.id
        if (id == R.id.topBar_back) {
            exit()
        } else if (id == R.id.sound) {
            isMute = !isMute
            mAudioManager!!.setStreamMute(AudioManager.STREAM_MUSIC, isMute)
            if (isMute) {
                mSound!!.setImageResource(R.drawable.phone_480_sound_mute)
            } else {
                mSound!!.setImageResource(R.drawable.phone_480_sound_on)
            }
        } else if (id == R.id.play) {
            doPauseResume()
        }
    }

    private fun updatePausePlay() {
        if (mMediaPlayer == null || mPauseButton == null) {
            return
        }
        val resource = if (mMediaPlayer!!.isPlaying) R.drawable.button_pause else R.drawable.button_play
        mPauseButton!!.setBackgroundResource(resource)
    }

    private fun doPauseResume() {
        if (mMediaPlayer == null) {
            return
        }
        if (mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.pause()
            if (null != mMediaListener) {
                mMediaListener!!.pause()
            }
        } else {
            mMediaPlayer!!.start()
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 200)
            if (null != mMediaListener) {
                mMediaListener!!.start()
            }
        }
        updatePausePlay()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            val visibility = mLayoutTop!!.visibility
            if (visibility != View.VISIBLE) {
                mLayoutTop!!.visibility = View.VISIBLE
                mLayoutBottom!!.visibility = View.VISIBLE
            } else {
                mLayoutTop!!.visibility = View.GONE
                mLayoutBottom!!.visibility = View.GONE
            }
        }

        // if (mediaController.isShowing()) {
        // mediaController.hide();
        // } else {
        // mediaController.show(10000);
        // }
        return false
    }

    override fun getAudioSessionId(): Int {
        return 1
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.v(TAG, "surfaceChanged Called")
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.v(TAG, "surfaceCreated Called")
        mMediaPlayer!!.setDisplay(holder)
        try {
            mMediaPlayer!!.prepare()
        } catch (e: IllegalStateException) {
            Log.v(TAG, "IllegalStateException", e)
        } catch (e: IOException) {
            Log.v(TAG, "IOException", e)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.v(TAG, "surfaceDestroyed Called")
    }

    override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {
        Log.v(TAG, "onVideoSizeChanged Called")
    }

    override fun onSeekComplete(mp: MediaPlayer) {
        Log.v(TAG, "onSeekComplete Called")
        if (null != mMediaListener) {
            mMediaListener!!.endOfMedia()
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        Log.v(TAG, "onPrepared Called")
        videoWidth = mp.videoWidth
        videoHeight = mp.videoHeight
        if (videoWidth > currentDisplay!!.width || videoHeight > currentDisplay!!.height) {
            val heightRatio = videoHeight.toFloat() / currentDisplay!!.height.toFloat()
            val widthRatio = videoWidth.toFloat() / currentDisplay!!.width.toFloat()
            if (heightRatio > 1 || widthRatio > 1) {
                if (heightRatio > widthRatio) {
                    videoHeight = Math.ceil((videoHeight.toFloat() / heightRatio).toDouble()).toInt()
                    videoWidth = Math.ceil((videoWidth.toFloat() / heightRatio).toDouble()).toInt()
                } else {
                    videoHeight = Math.ceil((videoHeight.toFloat() / widthRatio).toDouble()).toInt()
                    videoWidth = Math.ceil((videoWidth.toFloat() / widthRatio).toDouble()).toInt()
                }
            }
        }
        // surfaceView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth,
        // videoHeight));
        mp.start()
        if (null != mMediaListener) {
            mMediaListener!!.start()
        }

        // mediaController.setMediaPlayer(this);
        // mediaController.setAnchorView(this.findViewById(R.id.gplayer_surfaceview));
        // mediaController.setEnabled(true);
        // mediaController.show(5000);
        mHandler.sendEmptyMessage(MEDIA_PLAYER_PREPARED)
        mHandler.sendEmptyMessage(MEDIA_PLAYER_PROGRESS_UPDATE)
        mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_HIDDEN_CONTROL, 10000)
    }

    override fun onInfo(mp: MediaPlayer, whatInfo: Int, extra: Int): Boolean {
        if (whatInfo == MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING) {
            Log.v(TAG, "Media Info, Media Info Bad Interleaving $extra")
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
            Log.v(TAG, "Media Info, Media Info Not Seekable $extra")
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_UNKNOWN) {
            Log.v(TAG, "Media Info, Media Info Unknown $extra")
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
            Log.v(TAG, "MediaInfo, Media Info Video Track Lagging $extra")
        } else if (whatInfo == MediaPlayer.MEDIA_INFO_METADATA_UPDATE) {
            Log.v(TAG, "MediaInfo, Media Info Metadata Update $extra")
        }
        return false
    }

    override fun onCompletion(mp: MediaPlayer) {
        Log.v(TAG, "onCompletion Called")
        if (null != mMediaListener) {
            mMediaListener!!.endOfMedia()
        }
        exit()
    }

    override fun onError(mp: MediaPlayer, whatError: Int, extra: Int): Boolean {
        Log.d(TAG, "onError Called$whatError  $extra")
        if (whatError == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            Log.v(TAG, "Media Error, Server Died $extra")
        } else if (whatError == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            Log.v(TAG, "Media Error, Error Unknown $extra")
        }
        return false
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun getCurrentPosition(): Int {
        return mMediaPlayer!!.currentPosition
    }

    override fun getDuration(): Int {
        return mMediaPlayer!!.duration
    }

    override fun isPlaying(): Boolean {
        return mMediaPlayer!!.isPlaying
    }

    fun setUri(uri: String?) {
        try {
            mMediaPlayer!!.reset()
            playURI = uri
            mMediaPlayer!!.setDataSource(playURI)
        } catch (e: IllegalArgumentException) {
            Log.v(TAG, e.message!!)
        } catch (e: IllegalStateException) {
            Log.v(TAG, e.message!!)
        } catch (e: IOException) {
            Log.v(TAG, e.message!!)
        }
    }

    override fun pause() {
        if (mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.pause()
            if (null != mMediaListener) {
                mMediaListener!!.pause()
            }
        }
    }

    override fun seekTo(pos: Int) {
        mMediaPlayer!!.seekTo(pos)
        if (null != mMediaListener) {
            mMediaListener!!.positionChanged(pos)
        }
    }

    override fun start() {
        try {
            mMediaPlayer!!.start()
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 200)
            if (null != mMediaListener) {
                mMediaListener!!.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "start()", e)
        }
    }

    fun stop() {
        try {
            mMediaPlayer!!.stop()
            if (null != mMediaListener) {
                mMediaListener!!.stop()
            }
        } catch (e: Exception) {
            Log.e(TAG, "stop()", e)
        }
    }

    private val mHandler: Handler = object : Handler() {
        @SuppressLint("HandlerLeak")
        override fun handleMessage(msg: Message) {
            Log.d(TAG, "msg=" + msg.what)
            when (msg.what) {
                MEDIA_PLAYER_BUFFERING_UPDATE -> {
                }
                MEDIA_PLAYER_COMPLETION -> {
                }
                MEDIA_PLAYER_ERROR -> {
                }
                MEDIA_PLAYER_INFO -> {
                }
                MEDIA_PLAYER_PREPARED -> {
                    mBufferLayout!!.visibility = View.GONE
                }
                MEDIA_PLAYER_PROGRESS_UPDATE -> {
                    if (null == mMediaPlayer || !mMediaPlayer!!.isPlaying) {
                        return
                    }
                    val position = mMediaPlayer!!.currentPosition
                    val duration = mMediaPlayer!!.duration
                    if (null != mMediaListener) {
                        mMediaListener!!.positionChanged(position)
                        mMediaListener!!.durationChanged(duration)
                    }
                    mTextViewLength!!.text = Utils.secToTime((duration / 1000).toLong())
                    mSeekBarProgress!!.max = duration
                    mTextViewTime!!.text = Utils.secToTime((position / 1000).toLong())
                    mSeekBarProgress!!.progress = position
                    sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 500)
                }
                MEDIA_PLAYER_VIDEO_SIZE_CHANGED -> {
                }
                MEDIA_PLAYER_VOLUME_CHANGED -> {
                    mSeekBarSound!!.progress = mAudioManager
                            ?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                }
                MEDIA_PLAYER_HIDDEN_CONTROL -> {
                    mLayoutTop!!.visibility = View.GONE
                    mLayoutBottom!!.visibility = View.GONE
                }
                else -> {
                }
            }
        }
    }
    private val playRecevieBrocast: PlayBrocastReceiver = PlayBrocastReceiver()
    fun registerBrocast() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Action.DMR)
        intentFilter.addAction(Action.VIDEO_PLAY)
        registerReceiver(playRecevieBrocast, intentFilter)
    }

    fun unregisterBrocast() {
        unregisterReceiver(playRecevieBrocast)
    }

    internal inner class PlayBrocastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val str1 = intent.getStringExtra("helpAction")
            if (str1 == Action.PLAY) {
                start()
                updatePausePlay()
            } else if (str1 == Action.PAUSE) {
                pause()
                updatePausePlay()
            } else if (str1 == Action.SEEK) {
                var isPaused = false
                if (!mMediaPlayer!!.isPlaying) {
                    isPaused = true
                }
                val position = intent.getIntExtra("position", 0)
                mMediaPlayer!!.seekTo(position)
                if (isPaused) {
                    pause()
                } else {
                    start()
                }
            } else if (str1 == Action.SET_VOLUME) {
                val volume = (intent.getDoubleExtra("volume", 0.0) * mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)).toInt()
                mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
                mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100)
            } else if (str1 == Action.STOP) {
                stop()
            }
        }
    }

    interface MediaListener {
        fun pause()
        fun start()
        fun stop()
        fun endOfMedia()
        fun positionChanged(position: Int)
        fun durationChanged(duration: Int)
    }

    companion object {
        private val TAG = GPlayer::class.java.simpleName
        private const val MEDIA_PLAYER_BUFFERING_UPDATE = 4001
        private const val MEDIA_PLAYER_COMPLETION = 4002
        private const val MEDIA_PLAYER_ERROR = 4003
        private const val MEDIA_PLAYER_INFO = 4004
        private const val MEDIA_PLAYER_PREPARED = 4005
        private const val MEDIA_PLAYER_PROGRESS_UPDATE = 4006
        private const val MEDIA_PLAYER_VIDEO_SIZE_CHANGED = 4007
        private const val MEDIA_PLAYER_VOLUME_CHANGED = 4008
        private const val MEDIA_PLAYER_HIDDEN_CONTROL = 4009
        var mMediaListener: MediaListener? = null
        @JvmStatic
        fun setMediaListener(mediaListener: MediaListener?) {
            mMediaListener = mediaListener
        }
    }
}