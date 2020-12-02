package tk.munditv.mtvservice.util;

import android.content.Context;
import android.preference.Preference;
import android.util.Log;
import android.view.View;

import tk.munditv.mtvservice.R;

public class PreferenceHead extends Preference {

	private final static String TAG = PreferenceHead.class.getSimpleName();

	public PreferenceHead(Context context) {
		super(context);
		Log.d(TAG, "PreferenceHead()");
		setLayoutResource(R.layout.preference_head);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		Log.d(TAG, "onBindView()");

	}
}
