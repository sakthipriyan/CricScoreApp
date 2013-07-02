package com.sakthipriyan.cricscore;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.os.Bundle;

public class Settings extends SherlockPreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
