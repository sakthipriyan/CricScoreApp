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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.sakthipriyan.cricscore.CricScoreService.CricScoreAPI;
import com.sakthipriyan.cricscore.CricScoreService.LocalBinder;
import com.sakthipriyan.cricscore.models.Score;

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
	private MatchAdapter matchAdapter;
	private Set<Integer> liveMatches;
	private MenuItem refreshItem;
	private ImageView iv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		cxt = this;
		this.receiver = new CricScoreReceiver();
		this.filter = new IntentFilter(UPDATE_MATCHES);
		this.filter.addAction(UPDATE_STARTED);
		this.filter.addAction(UPDATE_COMPLETED);
		this.matches = new ArrayList<Score>();
		this.liveMatches = new HashSet<Integer>();
		this.matchAdapter = new MatchAdapter(cxt);
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
		this.refreshItem = menu.findItem(R.id.refresh);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(cxt, Settings.class));
			return true;
		case R.id.refresh:
			cricScoreAPI.refresh();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (bound) {
			unbindService(connection);
			bound = false;
		}
	}

	private void matchClicked(Score score, View view) {
		Integer id = score.getId();
		if (liveMatches.contains(id)) {
			view.setBackgroundResource(0);
			liveMatches.remove(id);
			cricScoreAPI.removeMatch(id);
			//Toast.makeText(cxt, score.getTeam1() + " vs "  + score.getTeam2() + " removed", Toast.LENGTH_LONG).show();
		} else {
			view.setBackgroundColor(getResources().getColor(R.color.light_red));
			liveMatches.add(id);
			cricScoreAPI.addMatch(id);
			//Toast.makeText(cxt, score.getTeam1() + " vs "  + score.getTeam2() + " added", Toast.LENGTH_LONG).show();
		}
	}

	private void showAllMatches() {
		this.matches = cricScoreAPI.listMatches();
		final ListView list = (ListView) findViewById(R.id.matchList);
		list.setAdapter(matchAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				Score score = ((Score) list.getItemAtPosition(position));
				matchClicked(score, view);
			}
		});
		final LinearLayout progress = (LinearLayout) findViewById(R.id.loadingInProgress);
		progress.setVisibility(View.GONE);
		final LinearLayout content = (LinearLayout) findViewById(R.id.content);
		content.setVisibility(View.VISIBLE);
	}

	private void refreshStart() {
		if (this.iv == null) {
			LayoutInflater inflater = (LayoutInflater) getApplication()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.iv = (ImageView) inflater.inflate(R.layout.refresh_view, null);
		}

		Animation rotation = AnimationUtils.loadAnimation(getApplication(),
				R.anim.rotation_refresh);
		rotation.setRepeatCount(Animation.INFINITE);
		this.iv.startAnimation(rotation);

		refreshItem.setActionView(this.iv);
	}

	private void refreshEnd() {
		if (this.refreshItem != null
				&& this.refreshItem.getActionView() != null) {
			this.refreshItem.getActionView().clearAnimation();
			this.refreshItem.setActionView(null);
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

	private class CricScoreReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (UPDATE_MATCHES.equals(intent.getAction())) {
				showAllMatches();
			} else if (UPDATE_STARTED.equals(intent.getAction())) {
				refreshStart();
			} else if (UPDATE_COMPLETED.equals(intent.getAction())) {
				refreshEnd();
			}
		}
	}

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

}