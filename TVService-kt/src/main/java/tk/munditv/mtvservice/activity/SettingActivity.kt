package tk.munditv.mtvservice.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.*
import android.view.KeyEvent
import tk.munditv.mtvservice.R

class SettingActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preference)
        val intent = Intent("tk.munditv.mtvservice.action.START")
        sendBroadcast(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else super.onKeyDown(keyCode, event)
    }

    private fun setLayoutResource(preference: Preference) {
        if (preference is PreferenceScreen) {
            val ps = preference
            ps.layoutResource = R.layout.preference_screen
            val cnt = ps.preferenceCount
            for (i in 0 until cnt) {
                val p = ps.getPreference(i)
                setLayoutResource(p)
            }
        } else if (preference is PreferenceCategory) {
            val pc = preference
            pc.layoutResource = R.layout.preference_category
            val cnt = pc.preferenceCount
            for (i in 0 until cnt) {
                val p = pc.getPreference(i)
                setLayoutResource(p)
            }
        } else {
            preference.layoutResource = R.layout.preference
        }
    }

    companion object {
        /*
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference.getKey().equals("about")) {
			Intent intent = new Intent(tk.munditv.mtvservice.activity.SettingActivity.this,
					AboutActivity.class);
			startActivity(intent);
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	public static boolean getRenderOn(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("dmr_status", true);
	}
*/
        fun getRenderName(context: Context): String? {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(context)
            return prefs.getString("player_name",
                    context.getString(R.string.player_name_local))
        }

        /*
	public static boolean getDmsOn(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("dms_status", true);
	}

	public static String getDeviceName(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getString("dms_name",
				context.getString(R.string.device_local));
	}
*/
        fun getSlideTime(context: Context?): Int {
            val prefs = PreferenceManager
                    .getDefaultSharedPreferences(context)
            return Integer.valueOf(prefs.getString("image_slide_time", "5"))
        }
    }
}