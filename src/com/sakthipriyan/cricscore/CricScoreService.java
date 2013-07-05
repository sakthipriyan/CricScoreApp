package com.sakthipriyan.cricscore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

@SuppressLint("UseSparseArrays")
public class CricScoreService extends Service {

	private static final String TAG = CricScoreService.class.getSimpleName();
	private Date lastModified;
	private Map<Integer, Score> liveScores;
	private List<Score> listMatches;
	private Timer timer;
	private boolean checkboxNotify;
	private int updateInterval;
	private IBinder binder;
	private BackEnd backEnd;
	private CricScoreAPI api;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		this.timer = new Timer("scoreUpdateTimer");
		this.binder = new LocalBinder();
		this.backEnd = BackEnd.getInstance();
		this.liveScores = new HashMap<Integer, Score>(10);
		this.api = new CricScoreAPI();
		readPreferences();
	}

	private void readPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		this.checkboxNotify = prefs.getBoolean("checkboxNotify", true);
		this.updateInterval = Integer.parseInt(prefs.getString("refreshTime", "20000"));
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
		return Service.START_STICKY;
	}

	private void updateMatchList() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "updating match list");
				Response response = backEnd.fetchData(Request.NULL);
				listMatches = Score.getScores(response.getJson());
				sendBroadcast(new Intent(MainActivity.UPDATE_MATCHES));
				Log.d(TAG, "updated match list");
			}
		}).start();
	}

	private void background() {
		Log.d(TAG, "running background()");
		this.timer.cancel();
		if (this.liveScores.size() != 0) {
			this.timer = new Timer(true);
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					updateScores();
				}
			};
			this.timer.scheduleAtFixedRate(task, 0, this.updateInterval);
		}
	}

	private void updateScores() {
		sendBroadcast(new Intent(MainActivity.UPDATE_STARTED));
		Log.d(TAG, "Running background scores - started");
		List<Integer> matchIds = new ArrayList<Integer>(
				this.liveScores.keySet());
		Request request = new Request(matchIds, this.lastModified);
		Response response = backEnd.fetchData(request);
		List<Score> scoresChanged = Score.getScores(response.getJson());

		for (Score score : scoresChanged) {
			liveScores.put(score.getId(), score);
		}
		
		if (response.getLastModified() != null) {
			this.lastModified = response.getLastModified();
		}
		
		Log.d(TAG, "Running background scores - ended");
		sendBroadcast(new Intent(MainActivity.UPDATE_COMPLETED));
	}

	public class LocalBinder extends Binder {
		CricScoreAPI getService() {
			return api;
		}
	}

	public class CricScoreAPI {

		public List<Score> listMatches() {
			return listMatches;
		}

		public List<Score> getLiveScores() {
			Log.d(TAG, "getLiveScores(): " + liveScores);
			return new ArrayList<Score>(liveScores.values());
		}

		public void addMatch(Integer id) {
			if (!liveScores.containsKey(id)) {
				liveScores.put(id, null);
				background();
				lastModified = null;
			}
		}

		public void removeMatch(Integer id) {
			if (liveScores.containsKey(id)) {
				liveScores.remove(id);
				background();
			}
		}
	}
}