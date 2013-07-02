package com.sakthipriyan.cricscore;

import java.util.Date;

public class Response {
	
	private String response;
	private Date lastModified;

	public Response(String response, Date lastModified) {
		super();
		this.response = response;
		this.lastModified = lastModified;
	}

	public String getResponse() {
		return response;
	}

	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "Response [response=" + response + ", lastModified="
				+ lastModified + "]";
	}
}