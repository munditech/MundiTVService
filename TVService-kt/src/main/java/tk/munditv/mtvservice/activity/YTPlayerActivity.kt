package tk.munditv.mtvservice.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import tk.munditv.libtvservice.util.FullScreenHelper
import tk.munditv.mtvservice.R

class YTPlayerActivity : AppCompatActivity() {
    private var youTubePlayerView: YouTubePlayerView? = null
    private val fullScreenHelper = FullScreenHelper(this)
    private var videoID: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ytplayer)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        fullScreenHelper.enterFullScreen()
        youTubePlayerView = findViewById(R.id.youtube_player_view)
        initYouTubePlayerView()
    }

    override fun onConfigurationChanged(newConfiguration: Configuration) {
        super.onConfigurationChanged(newConfiguration)
        youTubePlayerView!!.getPlayerUiController().getMenu()!!.dismiss()
    }

    private fun initYouTubePlayerView() {
        //initPlayerMenu();
        Log.d("MundiYTPlayer", "initYouTubePlayerView()")
        initURL()
        if (videoID == null) finish()

        // The player will automatically release itself when the activity is destroyed.
        // The player will automatically pause when the activity is stopped
        // If you don't add YouTubePlayerView as a lifecycle observer, you will have to release it manually.
        lifecycle.addObserver(youTubePlayerView!!)
        youTubePlayerView!!.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                Log.d(TAG, "onReady()")
                youTubePlayer.loadOrCueVideo(lifecycle, videoID!!, 0f)
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
                Log.d(TAG, "state = $state")
                if (state == PlayerConstants.PlayerState.ENDED) {
                    finish()
                }
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerError) {
                super.onError(youTubePlayer, error)
                Log.d(TAG, "onError() error = $error")
                if (error == PlayerError.VIDEO_NOT_FOUND) {
                    finish()
                }
            }
        })
    }

    private fun initURL() {
        Log.d("MundiYTPlayer", "initURL()")
        val intent = intent
        if (intent.action == Intent.ACTION_SEND) {
            Log.d("MundiYTPlayer", " String = " + intent.type)
            try {
                if (intent.type == "text/plain") {
                    val str = intent.getStringExtra(Intent.EXTRA_TEXT)
                    Log.d("MundiYTPlayer", "SEND data  String = $str")
                    val array = str!!.split("/").toTypedArray()
                    videoID = array[array.size - 1]
                    Log.d("MundiYTPlayer", " videoID = $videoID")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            val str = uri!!.encodedQuery
            Log.d("MundiYTPlayer", "VIEW data String = $str")
            val array = str!!.split("v=").toTypedArray()
            videoID = array[array.size - 1]
            videoID = videoID!!.split("&").toTypedArray()[0]
            Log.d("MundiYTPlayer", " videoID = $videoID")
        }
    }

    companion object {
        private const val TAG = "YTPlayer"
    }
}