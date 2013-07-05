package com.sakthipriyan.cricscore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.sakthipriyan.cricscore.CricScoreService.CricScoreAPI;
import com.sakthipriyan.cricscore.CricScoreService.LocalBinder;

public class MainActivity extends SherlockActivity {

	public static final String UPDATE_MATCHES = "com.sakthipriyan.cricscore.UPDATE_MATCHES";
	public static final String UPDATE_STARTED = "com.sakthipriyan.cricscore.UPDATE_STARTED";
	public static final String UPDATE_COMPLETED = "com.sakthipriyan.cricscore.UPDATE_COMPLETED";
	private static final String TAG = MainActivity.class.getSimpleName();
	
	// For local reference
	private Context cxt;
	private CricScoreAPI cricScoreAPI;
	private boolean bound = false;
	private CricScoreReceiver receiver;
	private IntentFilter filter;
	private List<Score> matches;
	private List<Score> liveScores;
	private MatchAdapter matchAdapter;
	private Set<Integer> liveMatches;
	private Menu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		cxt = this;
		this.receiver = new CricScoreReceiver();
		this.filter = new IntentFilter(UPDATE_MATCHES);
		this.filter.addAction(UPDATE_STARTED);
		this.filter.addAction(UPDATE_COMPLETED);
		this.matches = new ArrayList<Score>();
		this.liveMatches = new HashSet<Integer>();
		this.matchAdapter = new MatchAdapter(cxt);
		//getSherlock().setProgressBarIndeterminateVisibility(true);
		Log.d(TAG, "onCreate");
	}

	@Override
	protected void onStart() {
		super.onStart();
		startService(new Intent(cxt, CricScoreService.class));
		Intent intent = new Intent(cxt, CricScoreService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "onStart");
	}
	
	@Override
	protected void onResume() {
		registerReceiver(receiver, filter);
		super.onResume();
	}
	
	@Override
	public void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(cxt, Settings.class));
			return true;
		case R.id.refresh:
			Toast.makeText(cxt, "Refreshing..",	 Toast.LENGTH_SHORT).show();
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

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
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
	
	private void addMatch(int id){
		Toast.makeText(cxt, id + "", Toast.LENGTH_LONG).show();
		cricScoreAPI.addMatch(id);
	}
	
	private void showAllMatches(){
		this.matches = cricScoreAPI.listMatches();
		final ListView list = (ListView) findViewById(R.id.matchList);
		list.setAdapter(matchAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				Integer matchId = ((Score)list.getItemAtPosition(position)).getId();
				addMatch(matchId);
			}
		});
		final LinearLayout progress =  (LinearLayout) findViewById(R.id.loadingInProgress);
		progress.setVisibility(View.GONE);
		final LinearLayout content =  (LinearLayout) findViewById(R.id.content);
		content.setVisibility(View.VISIBLE);
		getSherlock().setProgressBarIndeterminateVisibility(false);
	}
	
	private class CricScoreReceiver extends BroadcastReceiver {		
		@Override
		public void onReceive(Context context, Intent intent) {
			long start = System.currentTimeMillis();
			if(UPDATE_MATCHES.equals(intent.getAction())){
				showAllMatches();
			} else if(UPDATE_STARTED.equals(intent.getAction())){
				menu.findItem(R.id.refresh).setVisible(false);
				getSherlock().setProgressBarIndeterminateVisibility(true);
			} else if(UPDATE_COMPLETED.equals(intent.getAction())){
				getSherlock().setProgressBarIndeterminateVisibility(false);
				menu.findItem(R.id.refresh).setVisible(true);
			}
			long time = System.currentTimeMillis() - start;
			Log.i(TAG, "On Receive Time Taken : " + time + " ms");
		} 
	}
	
	private class MatchAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MatchAdapter(Context context) {
			mInflater = LayoutInflater.from(context);

		}

		public int getCount() {
			return matches.size();
		}

		public Object getItem(int position) {
			return matches.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.matchview, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.TeamOne);
				holder.text2 = (TextView) convertView
						.findViewById(R.id.TeamTwo);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			try {
				Score score = matches.get(position);
				holder.text.setText(score.getTeam1());
				holder.text2.setText(score.getTeam2());
			} catch (ArrayIndexOutOfBoundsException e) {

			}

			return convertView;
		}

		class ViewHolder {
			TextView text;
			TextView text2;
		}
	}
}