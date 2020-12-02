package tk.munditv.libtvservice.util;

import android.app.Activity;
import android.util.Log;
import android.view.View;

/**
 * Class responsible for changing the view from full screen to non-full screen and vice versa.
 *
 * @author Pierfrancesco Soffritti
 */
public class FullScreenHelper {

    private final static String TAG = FullScreenHelper.class.getSimpleName();

    private Activity context;
    private View[] views;

    /**
     * @param context
     * @param views to hide/show
     */
    public FullScreenHelper(Activity context, View ... views) {
        Log.d(TAG, "FullScreenHelper()");
        this.context = context;
        this.views = views;
    }

    /**
     * call this method to enter full screen
     */
    public void enterFullScreen() {
        Log.d(TAG, "enterFullScreen()");

        View decorView = context.getWindow().getDecorView();

        hideSystemUi(decorView);

        for(View view : views) {
            view.setVisibility(View.GONE);
            view.invalidate();
        }
    }

    /**
     * call this method to exit full screen
     */
    public void exitFullScreen() {
        Log.d(TAG, "exitFullScreen()");

        View decorView = context.getWindow().getDecorView();

        showSystemUi(decorView);

        for(View view : views) {
            view.setVisibility(View.VISIBLE);
            view.invalidate();
        }
    }

    private void hideSystemUi(View mDecorView) {
        Log.d(TAG, "hideSystemUi()");

        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void showSystemUi(View mDecorView) {
        Log.d(TAG, "showSystemUi()");

        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
