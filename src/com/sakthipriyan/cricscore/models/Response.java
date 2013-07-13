package com.sakthipriyan.cricscore.models;

public class Response {
	public static final Response NULL = new Response(null, null);

	private String json;
	private String lastModified;

	public Response(String json, String lastModified) {
		super();
		this.json = json;
		this.lastModified = lastModified;
	}

	public String getJson() {
		return json;
	}

	public String getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "Response [json=" + json + ", lastModified=" + lastModified
				+ "]";
	}
}