package com.sakthipriyan.cricscore;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Score {

	private int id;
	private String team1;
	private String team2;
	private String simple;
	private String detail;

	public Score(int id) {
		this.id = id;
	}

	public void setTeams(String team1, String team2) {
		this.team1 = team1;
		this.team2 = team2;
	}

	public void setScore(String simple, String detail) {
		this.simple = simple;
		this.detail = detail;
	}

	public void updateScore(Score score) {
		if (this.id == score.id) {
			this.simple = score.simple;
			this.detail = score.detail;
		}
	}

	public int getId() {
		return id;
	}

	public String getTeam1() {
		return team1;
	}

	public String getTeam2() {
		return team2;
	}

	public String getDetail() {
		return detail;
	}

	public String getSimple() {
		return simple;
	}

	@Override
	public String toString() {
		return "Score [id=" + id + ", team1=" + team1 + ", team2=" + team2
				+ ", detail=" + detail + ", simple=" + simple + "]";
	}

	public static List<Score> getScores(String json) {
		List<Score> scores = new ArrayList<Score>();

		if (null == json) {
			return scores;
		}

		try {
			JSONArray jArray = new JSONArray(json);
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject object = jArray.getJSONObject(i);
				Score score = new Score(object.getInt("id"));
				if (object.has("si")) {
					score.setScore(object.getString("si"),
							object.getString("de"));
				} else {
					score.setTeams(object.getString("t1"),
							object.getString("t2"));
				}
				scores.add(score);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(Score.class.toString(), "Failed to parse the JSON response");
		}
		return scores;
	}
}