package com.sakthipriyan.cricscore;

import java.util.ArrayList;
import java.util.List;

public class DetailedScore {

	private static String EMPTY = "";

	private int matchId;
	private String team1;
	private String team2;

	private String scoret1s1;
	private String scoret1s2;
	private String scoret2s1;
	private String scoret2s2;

	private String playingTeam;
	private String playingOver;
	private String playingScore;

	private String batsman1;
	private String batsman2;
	private String batsman1score;
	private String batsman2score;
	private String bowler;
	private String bowlerEco;

	private String matchStatus;

	private String place;
	private String matchInfo;
	private String matchDate;

	public DetailedScore(Score score) {
		this.matchId = score.getId();
		this.processSimple(score.getSimple());
		this.processDetail(score.getDetail());
	}

	private void processSimple(final String simple) {

		if (simple == null) {
			return;
		}
		
		String teams[] = simple.split(" v ");
		String team1 = teams[0].trim();
		String team2 = teams[1].trim();
		this.team1 = team1.replaceAll("[^a-zA-Z- ]", "").trim();
		this.team2 = team2.replaceAll("[^a-zA-Z- ]", "").trim();
		String team1Score = team1.replaceAll("[a-zA-Z-* ]", "");
		String team2Score = team2.replaceAll("[a-zA-Z-* ]", "");

		if (!EMPTY.equals(team1Score)) {
			String team1Scores[] = team1Score.split("&");
			this.scoret1s1 = team1Scores[0];
			if (team1Scores.length > 1) {
				this.scoret1s2 = team1Scores[1];
			}
		}

		if (!EMPTY.equals(team2Score)) {
			String team2Scores[] = team2Score.split("&");
			this.scoret2s1 = team2Scores[0];
			if (team2Scores.length > 1) {
				this.scoret2s2 = team2Scores[1];
			}
		}
	}

	private void processDetail(final String detail) {
		if (detail == null) {
			return;
		}

		int openBracket = detail.indexOf("(");
		int closeBracket = detail.indexOf(")");
		if (openBracket > 0 && closeBracket > 0) {
			List<String> scoreList = new ArrayList<String>();
			String innerTxt = detail.substring(openBracket + 1, closeBracket);
			String innerArray[] = innerTxt.split(",");
			this.playingOver = innerArray[0];
			for (int i = 1; i < innerArray.length; i++) {
				innerArray[i] = innerArray[i].trim().replaceAll("[*]", "");
				int split = innerArray[i].lastIndexOf(" ");
				scoreList.add(innerArray[i].substring(0, split));
				scoreList.add(innerArray[i].substring(split + 1,
						innerArray[i].length()));
			}
			setCurrentInfo(scoreList);
		}

		if (openBracket > 0) {
			int scoreStart = detail.lastIndexOf(" ", openBracket - 2);
			this.playingScore = detail.substring(scoreStart + 1,
					openBracket - 1);
			this.playingTeam = detail.substring(0, scoreStart);

			if (this.playingTeam != null && playingTeam.length() > 8) {
				String[] arr = playingTeam.split(" ");
				StringBuilder builder = new StringBuilder();
				if (arr.length >= 2) {
					for (int i = 0; i < arr.length; i++) {
						builder.append(arr[i].charAt(0));
					}
					playingTeam = builder.toString();
				} else {
					playingTeam = playingTeam.substring(0, 6) + "..";
				}
			}
		}

		int statusStart = detail.indexOf("-", closeBracket);
		if (statusStart > 0) {
			this.matchStatus = detail.substring(statusStart + 2,
					detail.length());
		}

		if (detail.contains(" at ") || detail.contains(":")) {
			String val = detail.substring(detail.indexOf(" at ") + 4,
					detail.length());

			int commaIndex = val.indexOf(",");
			int colenIndex = detail.indexOf(":");
			if (commaIndex != -1) {
				this.place = val.substring(0, commaIndex);
				this.matchDate = val.substring(commaIndex + 2, val.length());
			}
			if (colenIndex != -1) {
				this.matchInfo = detail.substring(0, colenIndex);
			}
		}
	}

	private void setCurrentInfo(List<String> listArray) {
		int size = listArray.size();			
		if (size == 6) {
			this.batsman1 = listArray.get(0);
			this.batsman1score = listArray.get(1);
			this.batsman2 = listArray.get(2);
			this.batsman2score = listArray.get(3);
			this.bowler = listArray.get(4);
			this.bowlerEco = listArray.get(5);
		} else if (size == 4) {
			this.batsman1 = listArray.get(0);
			this.batsman1score = listArray.get(1);
			this.bowler = listArray.get(2);
			this.bowlerEco = listArray.get(3);
		} else if (size == 2) {
			this.batsman1 = listArray.get(0);
			this.batsman1score = listArray.get(1);
		}
	}

	private String getString(String string) {
		return string != null ? string : EMPTY;
	}

	public int getMatchId() {
		return matchId;
	}

	public String getTeam1() {
		return team1;
	}

	public String getTeam2() {
		return team2;
	}

	public String getScoret1s1() {
		return getString(scoret1s1);
	}

	public String getScoret1s2() {
		return getString(scoret1s2);
	}

	public String getScoret2s1() {
		return getString(scoret2s1);
	}

	public String getScoret2s2() {
		return getString(scoret2s2);
	}

	public String getPlayingTeam() {
		return getString(playingTeam);
	}

	public String getPlayingOver() {
		return getString(playingOver);
	}

	public String getPlayingScore() {
		return getString(playingScore);
	}

	public String getBatsman1() {
		return getString(batsman1);
	}

	public String getBatsman2() {
		return getString(batsman2);
	}

	public String getBatsman1score() {
		return getString(batsman1score);
	}

	public String getBatsman2score() {
		return getString(batsman2score);
	}

	public String getBowler() {
		return getString(bowler);
	}

	public String getBowlerEco() {
		return getString(bowlerEco);
	}

	public String getMatchStatus() {
		return getString(matchStatus);
	}

	public String getPlace() {
		return getString(place);
	}

	public String getMatchInfo() {
		return getString(matchInfo);
	}

	public String getMatchDate() {
		return getString(matchDate);
	}

	@Override
	public String toString() {
		return "DetailedScore [matchId=" + matchId + ", team1=" + team1
				+ ", team2=" + team2 + ", scoret1s1=" + scoret1s1
				+ ", scoret1s2=" + scoret1s2 + ", scoret2s1=" + scoret2s1
				+ ", scoret2s2=" + scoret2s2 + ", playingTeam=" + playingTeam
				+ ", playingOver=" + playingOver + ", playingScore="
				+ playingScore + ", batsman1=" + batsman1 + ", batsman2="
				+ batsman2 + ", batsman1score=" + batsman1score
				+ ", batsman2score=" + batsman2score + ", bowler=" + bowler
				+ ", bowlerEco=" + bowlerEco + ", matchStatus=" + matchStatus
				+ ", place=" + place + ", matchInfo=" + matchInfo
				+ ", matchDate=" + matchDate + "]";
	}

	/*
	 * public static boolean containsMatchOver(SimpleScore score) { if (score !=
	 * null && score.getDetail() != null) { String scoreUpperCase =
	 * score.getDetail().toUpperCase(); if (scoreUpperCase.contains("MATCH") &&
	 * scoreUpperCase.contains("OVER") && scoreUpperCase.indexOf("MATCH") <
	 * scoreUpperCase .indexOf("OVER")) { return true; } } return false; }
	 */

}