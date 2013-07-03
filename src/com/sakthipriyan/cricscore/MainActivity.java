package com.sakthipriyan.cricscore;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.sakthipriyan.cricscore.CricScoreService.CricScoreAPI;
import com.sakthipriyan.cricscore.CricScoreService.LocalBinder;

public class MainActivity extends SherlockActivity {

	private static final String TAG = MainActivity.class.toString(); 
	// For local reference
	private Context cxt;
	private CricScoreAPI cricScoreAPI;
	private boolean bound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		cxt = this;
		Log.d(TAG, "onCreate");
	}

	@Override
	protected void onStart() {
		super.onStart();
		startService(new Intent(cxt, CricScoreService.class));
		Intent intent = new Intent(cxt, CricScoreService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "onStart");
		if(bound){
			Log.d(TAG, "cricScoreService.getScores():" + cricScoreAPI.getScoresChanged());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(cxt, Settings.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		if (bound) {
			unbindService(connection);
			bound = false;
		}
	}

	/**
	 * Called when a button is clicked (the button in the layout file attaches
	 * to this method with the android:onClick attribute)
	 */
	/*
	 * public void onButtonClick(View v) { if (mBound) { // Call a method from
	 * the LocalService. // However, if this call were something that might
	 * hang, then this request should // occur in a separate thread to avoid
	 * slowing down the activity performance. int num =
	 * mService.getRandomNumber(); Toast.makeText(this, "number: " + num,
	 * Toast.LENGTH_SHORT).show(); } }
	 */

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			cricScoreAPI = binder.getService();
			bound = true;
			Log.d(TAG, "connecting to service");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
			Log.d(TAG, "disconnecting from service");
		}
	};

}