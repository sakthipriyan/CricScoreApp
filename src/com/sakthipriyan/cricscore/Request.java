package com.sakthipriyan.cricscore;

import java.util.Arrays;
import java.util.Date;

public class Request {
	
	private int[] matchIds;
	private Date lastModified;

	public Request(int[] matchIds, Date lastModified) {
		super();
		this.matchIds = matchIds;
		this.lastModified = lastModified;
	}

	public int[] getMatchIds() {
		return matchIds;
	}

	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "Request [matchIds=" + Arrays.toString(matchIds)
				+ ", lastModified=" + lastModified + "]";
	}
	
}
