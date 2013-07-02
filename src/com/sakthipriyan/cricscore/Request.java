package com.sakthipriyan.cricscore;

import java.util.Date;
import java.util.List;

public class Request {
	
	private List<Integer> matchIds;
	private Date lastModified;

	public Request(List<Integer> matchIds, Date lastModified) {
		super();
		this.matchIds = matchIds;
		this.lastModified = lastModified;
	}

	public List<Integer> getMatchIds() {
		return matchIds;
	}

	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "Request [matchIds=" + matchIds + ", lastModified="
				+ lastModified + "]";
	}
	
}
