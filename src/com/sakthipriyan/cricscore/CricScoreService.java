package com.sakthipriyan.cricscore;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class CricScoreService extends Service {

	private static final String TAG = CricScoreService.class.toString();
	private Date lastModified;
	private List<Integer> matches;
	private List<Score> scoresChanged;
	private List<Score> listMatches;
	private Timer timer;
	private boolean checkboxNotify;
	private int updateInterval;
	private IBinder binder;
	private BackEnd backEnd;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		this.timer = new Timer("scoreUpdateTimer");
		this.binder = new LocalBinder();
		this.backEnd = BackEnd.getInstance();
		readPreferences();
	}

	private void readPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		this.checkboxNotify = prefs.getBoolean("checkboxNotify", true);
		this.updateInterval = prefs.getInt("refreshTime", 20000);
		Log.d(TAG, "Read Preferences. checkboxNotify:" + checkboxNotify
				+ " updateInterval:" + updateInterval);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return this.binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		updateMatchList();
		background();
		return Service.START_STICKY;
	}

	private void updateMatchList() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "updating match list");
				Response response = backEnd.fetchData(Request.NULL);
				listMatches = Score.getScores(response.getJson());
				Log.d(TAG, "updated match list");
			}
		}).start();
	}

	private void background() {
		Log.d(TAG, "running background()");
		this.timer.cancel();
		this.timer = new Timer(true);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				updateScores();
			}
		};
		this.timer.scheduleAtFixedRate(task, 0, this.updateInterval);
	}

	private void updateScores() {
		if (this.matches != null && this.matches.size() != 0) {
			Log.d(TAG, "Running background scores - started");
			Request request = new Request(this.matches, this.lastModified);
			Response response = backEnd.fetchData(request);
			this.scoresChanged = Score.getScores(response.getJson());
			this.lastModified = response.getLastModified();
			Log.d(TAG, "Running background scores - ended");
		}
	}

	public class LocalBinder extends Binder {
		CricScoreAPI getService() {
			return new CricScoreAPI();
		}
	}

	public class CricScoreAPI {
		public List<Score> listMatches() {
			return CricScoreService.this.listMatches;
		}

		public List<Score> getScoresChanged() {
			Log.d(TAG, "getScores(): " + scoresChanged);
			return scoresChanged;
		}

		public void setMatches(List<Integer> newMatches) {
			Log.d(TAG, "setMatches(): " + matches);
			matches = newMatches;
			lastModified = null;
			background();
		}
	}
}
