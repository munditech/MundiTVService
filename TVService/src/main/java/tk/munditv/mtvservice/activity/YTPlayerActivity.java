package tk.munditv.mtvservice.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import tk.munditv.libtvservice.util.FullScreenHelper;
import tk.munditv.mtvservice.R;

public class YTPlayerActivity extends AppCompatActivity {
    private static final String TAG = "YTPlayer";

    private YouTubePlayerView youTubePlayerView;
    private FullScreenHelper fullScreenHelper = new FullScreenHelper(this);
    private String videoID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ytplayer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        fullScreenHelper.enterFullScreen();
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        initYouTubePlayerView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfiguration) {
        super.onConfigurationChanged(newConfiguration);
        youTubePlayerView.getPlayerUiController().getMenu().dismiss();
    }

    private void initYouTubePlayerView() {
        //initPlayerMenu();
        Log.d("MundiYTPlayer", "initYouTubePlayerView()");

        initURL();
        if (videoID == null) finish();

        // The player will automatically release itself when the activity is destroyed.
        // The player will automatically pause when the activity is stopped
        // If you don't add YouTubePlayerView as a lifecycle observer, you will have to release it manually.
        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                Log.d(TAG, "onReady()");

                YouTubePlayerUtils.loadOrCueVideo(
                        youTubePlayer,
                        getLifecycle(),
                        videoID,
                        0f
                );
            }

            @Override
            public void onStateChange(YouTubePlayer youTubePlayer, PlayerConstants.PlayerState state) {
                super.onStateChange(youTubePlayer, state);
                Log.d(TAG, "state = " + state);
                if (state == PlayerConstants.PlayerState.ENDED) {
                    finish();
                }
            }

            @Override
            public void onError(YouTubePlayer youTubePlayer, PlayerConstants.PlayerError error) {
                super.onError(youTubePlayer, error);
                Log.d(TAG, "onError() error = " + error.toString());
                if (error == PlayerConstants.PlayerError.VIDEO_NOT_FOUND) {
                    finish();
                }
            }

        });

    }

    private void initURL() {
        Log.d("MundiYTPlayer", "initURL()");

        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_SEND)) {
            Log.d("MundiYTPlayer", " String = " + intent.getType());
            try {
                if (intent.getType().equals("text/plain")) {
                    String str = intent.getStringExtra(Intent.EXTRA_TEXT);
                    Log.d("MundiYTPlayer", "SEND data  String = " + str);
                    String[] array = str.split("/");
                    videoID = array[array.length - 1];
                    Log.d("MundiYTPlayer", " videoID = " + videoID);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = intent.getData();
            String str = uri.getEncodedQuery();
            Log.d("MundiYTPlayer", "VIEW data String = " + str);
            String[] array = str.split("v=");
            videoID = array[array.length-1];
            videoID = videoID.split("&")[0];
            Log.d("MundiYTPlayer", " videoID = " + videoID);
        }
    }
}
