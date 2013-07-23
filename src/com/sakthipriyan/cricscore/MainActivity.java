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
import com.sakthipriyan.cricscore.models.DetailedScore;
import com.sakthipriyan.cricscore.models.DetailedScore.Status;

public class MainActivity extends SherlockActivity {

	public static final String UPDATE_MATCHES = "com.sakthipriyan.cricscore.UPDATE_MATCHES";
	public static final String UPDATE_STARTED = "com.sakthipriyan.cricscore.UPDATE_STARTED";
	public static final String UPDATE_COMPLETED = "com.sakthipriyan.cricscore.UPDATE_COMPLETED";
	public static final String UPDATE_NONE = "com.sakthipriyan.cricscore.UPDATE_NONE";
	private static final String TAG = MainActivity.class.getSimpleName();

	// For local reference
	private Context cxt;
	private CricScoreAPI cricScoreAPI;
	private boolean bound = false;
	private CricScoreReceiver receiver;
	private IntentFilter filter;
	private List<DetailedScore> liveScores;
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
		this.filter.addAction(UPDATE_NONE);
		this.liveScores = new ArrayList<DetailedScore>(20);
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

	private void matchClicked(DetailedScore score, View view) {
		int id = score.getMatchId();
		if (liveMatches.contains(id)) {
			// view.setBackgroundResource(0);
			liveMatches.remove(id);
			cricScoreAPI.removeMatch(id);
			score.setStatus(Status.MATCH);
			matchAdapter.notifyDataSetChanged();
		} else {
			view.setBackgroundColor(getResources().getColor(R.color.light_yellow));
			liveMatches.add(id);
			cricScoreAPI.addMatch(id);
		}
	}

	private void showAllMatches() {
		this.liveScores.clear();
		this.liveScores.addAll(cricScoreAPI.listMatches());
		final ListView list = (ListView) findViewById(R.id.content);
		list.setAdapter(matchAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				DetailedScore score = (DetailedScore) list
						.getItemAtPosition(position);
				matchClicked(score, view);
			}
		});
		((LinearLayout) findViewById(R.id.loadingInProgress))
				.setVisibility(View.GONE);
		list.setVisibility(View.VISIBLE);
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

		@Override
		public int getCount() {
			return liveScores.size();
		}

		@Override
		public Object getItem(int position) {
			return liveScores.get(position);
		}

		@Override
		public long getItemId(int position) {
			return liveScores.get(position).getMatchId();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View view = convertView;
			System.out.println(liveScores.size());
			System.out.println(position);
			// if (view == null) {
			DetailedScore score = liveScores.get(position);
			Log.d(TAG, "score " + score);
			switch (score.getStatus()) {
			case MATCH:
				MatchViewHolder holder = new MatchViewHolder();
				view = mInflater.inflate(R.layout.matchview, null);
				holder.text1 = (TextView) view.findViewById(R.id.TeamOne);
				holder.text2 = (TextView) view.findViewById(R.id.TeamTwo);

				holder.text1.setText(score.getTeam1());
				holder.text2.setText(score.getTeam2());
				view.setTag(holder);
				break;
			case LIVE:
				view = mInflater.inflate(R.layout.detail_view, null);
				((TextView) view.findViewById(R.id.team1)).setText(score.getTeam1());
				((TextView) view.findViewById(R.id.team1s1)).setText(score.getScoret1s1());
				((TextView) view.findViewById(R.id.team1s2)).setText(score.getScoret1s2());
				
				((TextView) view.findViewById(R.id.team2)).setText(score.getTeam2());
				((TextView) view.findViewById(R.id.team2s1)).setText(score.getScoret2s1());
				((TextView) view.findViewById(R.id.team2s2)).setText(score.getScoret2s2());
				
				((TextView) view.findViewById(R.id.teamshort)).setText(score.getPlayingTeam());
				((TextView) view.findViewById(R.id.teamruns)).setText(score.getPlayingScore());
				((TextView) view.findViewById(R.id.teamovers)).setText(score.getPlayingOver());
				
				((TextView) view.findViewById(R.id.bat1)).setText(score.getBatsman1());
				((TextView) view.findViewById(R.id.bat1Score)).setText(score.getBatsman1score());
				((TextView) view.findViewById(R.id.bat2)).setText(score.getBatsman2());
				((TextView) view.findViewById(R.id.bat2Score)).setText(score.getBatsman2score());
				((TextView) view.findViewById(R.id.blower)).setText(score.getBowler());
				((TextView) view.findViewById(R.id.blowerEco)).setText(score.getBowlerEco());
				
				String status = score.getMatchStatus();
				TextView matchStatusView = (TextView) view.findViewById(R.id.match_status);
				if(DetailedScore.EMPTY.equals(status)){
					matchStatusView.setText("Live");
					//matchStatusView.setTextColor(getResources().getColor(R.color.dark_green));
				} else {
					matchStatusView.setText(status);
				}
				
				
				
				
				
				// view.setTag(holder);
				break;
			case FUTURE:
				view = mInflater.inflate(R.layout.detail_view, null);
				// view.setTag(holder);
				break;

			default:
				Log.e(TAG, "Wrong type");
			}
			// }
			return view;
		}

		/*
		 * public View getView1(int position, View convertView, ViewGroup
		 * parent) { ViewHolder holder; if (convertView == null) { convertView =
		 * mInflater.inflate(R.layout.matchview, null); holder = new
		 * ViewHolder(); holder.text = (TextView)
		 * convertView.findViewById(R.id.TeamOne); holder.text2 = (TextView)
		 * convertView .findViewById(R.id.TeamTwo); convertView.setTag(holder);
		 * } else { holder = (ViewHolder) convertView.getTag(); } try { Score
		 * score = (Score) liveScores.get(position);
		 * holder.text.setText(score.getTeam1());
		 * holder.text2.setText(score.getTeam2()); } catch
		 * (ArrayIndexOutOfBoundsException e) {
		 * 
		 * }
		 * 
		 * return convertView; }
		 */

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		class MatchViewHolder {
			TextView text1;
			TextView text2;
		}
	}

	private class CricScoreReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UPDATE_MATCHES.equals(action)) {
				showAllMatches();
			} else if (UPDATE_STARTED.equals(action)) {
				refreshStart();
			} else if (UPDATE_COMPLETED.equals(action)) {
				for (DetailedScore score : cricScoreAPI.getLiveScores()) {
					int index = liveScores.indexOf(score);
					liveScores.remove(index);
					liveScores.add(index, score);
				}
				matchAdapter.notifyDataSetChanged();
				refreshEnd();
			} else if (UPDATE_NONE.equals(action)) {
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