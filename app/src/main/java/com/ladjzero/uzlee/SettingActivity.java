package com.ladjzero.uzlee;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.r0adkll.slidr.Slidr;

public class SettingActivity extends BaseActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActionbar.setDisplayHomeAsUpEnabled(true);
		LayoutInflater mInflater = LayoutInflater.from(this);
		View customView =  mInflater.inflate(R.layout.toolbar_title, null);

		mActionbar.setTitle(null);
		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowCustomEnabled(true);
		mActionbar.setCustomView(customView);
		mTitleView = (android.widget.TextView) customView.findViewById(R.id.title);

		setTitle("设置");

		Slidr.attach(this);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
	}

	public static class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(getResources().getColor(android.R.color.white));
			return view;
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Preference preference = findPreference(key);

			if (key.equals("sort_thread")) {
				preference.setSummary("按照" + ((ListPreference) preference).getEntry() + "排序");
			}

			if (key.equals("enable_image_only_wifi")) {
				((BaseActivity) getActivity()).setImageNetwork();
			}
		}

		@Override
		public void onResume() {
			super.onResume();
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}
	}
}
