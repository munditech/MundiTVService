package tk.munditv.mcontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    private final static String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown()");

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference, rootKey);
        }
    }

    public static boolean getRenderOn(Context context) {
        Log.d(TAG, "getRenderOn()");

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getBoolean("dmr_status", true);
    }

    public static String getRenderName(Context context) {
        Log.d(TAG, "getRenderName()");

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getString("player_name",
                context.getString(R.string.player_name_local));
    }

    public static boolean getDmsOn(Context context) {
        Log.d(TAG, "getDmsOn()");

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getBoolean("dms_status", true);
    }

    public static String getDeviceName(Context context) {
        Log.d(TAG, "getDeviceName()");

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getString("dms_name",
                context.getString(R.string.device_local));
    }

    public static int getSlideTime(Context context) {
        Log.d(TAG, "getSlideTime()");

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return Integer.valueOf(prefs.getString("image_slide_time", "5"));
    }
}