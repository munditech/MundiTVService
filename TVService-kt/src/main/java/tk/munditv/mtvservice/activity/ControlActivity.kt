package tk.munditv.mtvservice.activity

import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.fourthline.cling.android.AndroidUpnpService
import tk.munditv.libtvservice.dmc.DMCControl
import tk.munditv.libtvservice.dmp.ContentItem
import tk.munditv.libtvservice.dmp.DeviceItem
import tk.munditv.libtvservice.util.Action
import tk.munditv.libtvservice.util.NetworkData
import tk.munditv.libtvservice.util.Utils
import tk.munditv.mtvservice.R
import java.util.*

class ControlActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "VideoControl"
    private var mSeekBar: SeekBar? = null
    private var mTotalTime: TextView? = null
    private var mCurrentTime: TextView? = null
    private var mNameTitle: TextView? = null
    private var mAuthorName: TextView? = null
    private var mPlayBtn: ImageView? = null
    private var mVoicePlus: ImageView? = null
    private var mVoiceCut: ImageView? = null
    private var mVoiceMute: ImageView? = null
    private var currentContentFormatMimeType: String? = ""
    private var dmcControl: DMCControl? = null
    private var dmrDeviceItem: DeviceItem? = null
    private var isToMute = true
    private val isUpdatePlaySeek = true
    var listcontent: ArrayList<ContentItem>? = null
    private var metaData: String? = null
    var name: String? = null
    private var path: String? = null
    private val position = 0
    private val timer: Timer? = null
    private var upnpService: AndroidUpnpService? = null
    private var progDialog: ProgressDialog? = null
    private val updatePlayTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(paramContext: Context, paramIntent: Intent) {
            if (paramIntent.action == Action.PLAY_UPDATE) {
                if (isUpdatePlaySeek) {
                    val localBundle = paramIntent.extras
                    val str1 = localBundle!!.getString("TrackDuration")
                    val str2 = localBundle.getString("RelTime")
                    val i = Utils.getRealTime(str1)
                    val j = Utils.getRealTime(str2)
                    mSeekBar!!.max = i
                    mSeekBar!!.progress = j
                    mTotalTime!!.text = str1
                    mCurrentTime!!.text = str2
                }
                stopProgressDialog()
            }
            if (paramIntent.action == "com.transport.info") {
                initData(paramIntent)
            }
            if (paramIntent.action == Action.PLAY_ERR_VIDEO || paramIntent.action == Action.PLAY_ERR_AUDIO) {
                Toast.makeText(this@ControlActivity, R.string.media_play_err,
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_lay)
        initView()
        val localIntentFilter = IntentFilter()
        localIntentFilter.addAction(Action.PLAY_UPDATE)
        localIntentFilter.addAction("com.video.play.error")
        localIntentFilter.addAction("com.connection.failed")
        localIntentFilter.addAction("com.transport.info")
        localIntentFilter.addAction(Action.PLAY_ERR_VIDEO)
        localIntentFilter.addAction(Action.PLAY_ERR_AUDIO)
        registerReceiver(updatePlayTime, localIntentFilter)
    }

    override fun onResume() {
        DMCControl.isExit = false
        super.onResume()
    }

    override fun onDestroy() {
        unregisterReceiver(updatePlayTime)
        DMCControl.isExit = true
        super.onDestroy()
    }

    private fun initView() {
        mNameTitle = findViewById<View>(R.id.media_tv_title) as TextView
        mAuthorName = findViewById<View>(R.id.media_tv_author) as TextView
        mPlayBtn = findViewById<View>(R.id.media_iv_play) as ImageView
        mVoicePlus = findViewById<View>(R.id.media_iv_voc_plus) as ImageView
        mVoiceCut = findViewById<View>(R.id.media_iv_voc_cut) as ImageView
        mVoiceMute = findViewById<View>(R.id.media_iv_voc_mute) as ImageView
        mPlayBtn!!.setOnClickListener(this)
        mVoicePlus!!.setOnClickListener(this)
        mVoiceCut!!.setOnClickListener(this)
        mVoiceMute!!.setOnClickListener(this)
        mPlayBtn!!.setBackgroundResource(R.drawable.icon_media_pause)
        mVoiceMute!!.setBackgroundResource(R.drawable.icon_voc_mute)
        mCurrentTime = findViewById<View>(R.id.media_tv_time) as TextView
        mTotalTime = findViewById<View>(R.id.media_tv_total_time) as TextView
        mSeekBar = findViewById<View>(R.id.media_seekBar) as SeekBar
        mSeekBar!!.setOnSeekBarChangeListener(PlaySeekBarListener())
    }

    private fun initData(localIntent: Intent?) {
        if (null == localIntent) {
            Toast.makeText(this, getString(R.string.not_select_dev),
                    Toast.LENGTH_SHORT).show()
            return
        }
        path = localIntent.getStringExtra("playURI")
        name = localIntent.getStringExtra("name")
        currentContentFormatMimeType = localIntent
                .getStringExtra("currentContentFormatMimeType")
        metaData = localIntent.getStringExtra("metaData")
        mNameTitle!!.text = name
        mAuthorName!!.text = name
        if (null != path && null != currentContentFormatMimeType && null != metaData) {
            isplay = true
            startProgressDialog()
            // TODO get
            // mVideoThumb.setImageBitmap(Utils.getThumbnailForVideo(path));
            dmrDeviceItem = NetworkData.getDmrDeviceItem()
            upnpService = NetworkData.getUpnpService()
            dmcControl = DMCControl(this, 3, dmrDeviceItem,
                    upnpService, path, metaData, null)
            dmcControl!!.getProtocolInfos(currentContentFormatMimeType)
        } else {
            Toast.makeText(this, getString(R.string.get_data_err),
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun startProgressDialog() {
        progDialog = ProgressDialog(this)
        progDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progDialog!!.isIndeterminate = false
        progDialog!!.setCancelable(true)
        progDialog!!.setMessage(getString(R.string.dialog_wait_msg))
    }

    private fun stopProgressDialog() {
        if (null != progDialog) {
            progDialog!!.dismiss()
            progDialog = null
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.media_iv_play -> {
                playPause()
            }
            R.id.media_iv_voc_plus -> {
                soundUp()
            }
            R.id.media_iv_voc_cut -> {
                soundDown()
            }
            R.id.media_iv_voc_mute -> {
                soundMute()
            }
            else -> {
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else super.onKeyDown(keyCode, event)
    }

    private fun playPause() {
        if (null != dmcControl) {
            if (isplay) {
                isplay = false
                dmcControl!!.pause()
                mPlayBtn!!.setBackgroundResource(R.drawable.icon_media_play)
            } else {
                isplay = true
                mPlayBtn!!.setBackgroundResource(R.drawable.icon_media_pause)
                dmcControl!!.play()
            }
        }
    }

    private fun soundDown() {
        if (null != dmcControl) {
            dmcControl!!.getVolume(DMCControl.CUT_VOC)
        }
    }

    private fun soundUp() {
        if (null != dmcControl) {
            dmcControl!!.getVolume(DMCControl.ADD_VOC)
        }
    }

    // if paramBoolean is True MUTE
    private fun soundMute() {
        if (null != dmcControl) {
            dmcControl!!.getMute()
        }
    }

    fun setVideoRemoteMuteState(paramBoolean: Boolean) {
        Log.i("mute state", java.lang.Boolean.toString(paramBoolean))
        isToMute = paramBoolean
        if (!paramBoolean) {
            mVoiceMute!!.setBackgroundResource(R.drawable.icon_voc_mute)
        } else {
            mVoiceMute!!.setBackgroundResource(R.drawable.icon_voc_mute_click)
        }
    }

    internal inner class PlaySeekBarListener : OnSeekBarChangeListener {
        override fun onProgressChanged(paramSeekBar: SeekBar, paramInt: Int,
                                       paramBoolean: Boolean) {
        }

        override fun onStartTrackingTouch(paramSeekBar: SeekBar) {
            // isUpdatePlaySeek = false;
        }

        override fun onStopTrackingTouch(paramSeekBar: SeekBar) {
            if (null != dmcControl) {
                val str = Utils.secToTime(paramSeekBar.progress.toLong())
                Log.i("DMC", "SeekBar time:$str")
                dmcControl!!.seekBarPosition(str)
            }
        }
    } // private void getCurrentPosition() {

    // final long beginTime = System.currentTimeMillis();
    // timer = new Timer();
    // timer.schedule(new TimerTask() {
    // @Override
    // public void run() {
    // dmcControl.getPositionInfo();
    // }
    // }, 100, 500);
    // }
    companion object {
        var isplay = false
    }
}