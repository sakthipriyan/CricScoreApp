package com.sakthipriyan.cricscore;

import java.util.Date;

public class Response {
	public static final Response NULL = new Response(null,null);
	
	private String json;
	private Date lastModified;

	public Response(String json, Date lastModified) {
		super();
		this.json = json;
		this.lastModified = lastModified;
	}

	public String getJson() {
		return json;
	}

	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "Response [json=" + json + ", lastModified="
				+ lastModified + "]";
	}
}