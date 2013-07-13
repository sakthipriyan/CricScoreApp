package com.sakthipriyan.cricscore;

import java.util.List;

public class Request {
	
	public static final Request NULL = new Request(null,null);	
	
	private List<Integer> matchIds;
	private String lastModified;

	public Request(List<Integer> matchIds, String lastModified) {
		super();
		this.matchIds = matchIds;
		this.lastModified = lastModified;
	}

	public List<Integer> getMatchIds() {
		return matchIds;
	}

	public String getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "Request [matchIds=" + matchIds + ", lastModified="
				+ lastModified + "]";
	}

}