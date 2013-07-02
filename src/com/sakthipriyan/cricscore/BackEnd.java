package com.sakthipriyan.cricscore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.params.BasicHttpParams;

import android.util.Log;

public class BackEnd {

	private static final String BASE_URL = "http://cricscore-api.appspot.com/csa";

	private static BackEnd instance;

	private BackEnd() {
	}

	public static BackEnd getInstance() {
		if (instance == null) {
			instance = new BackEnd();
		}
		return instance;
	}

	private String getURL(int[] matchIds) {
		StringBuilder url = new StringBuilder(BASE_URL);
		int length = matchIds.length;
		if (length > 0) {
			url.append("?id=");
		}
		for (int i = 0; i < length; i++) {
			url.append(matchIds[i]);
			if (i < length - 1) {
				url.append("+");
			}
		}
		return url.toString();
	}

	public Response fetchData(Request request) {
		Response response = null;
		String url = getURL(request.getMatchIds());
		System.out.println(url);
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient(new BasicHttpParams());
		HttpGet httpGet = new HttpGet(url);
		if (request.getLastModified() != null) {
			String date = DateUtils.formatDate(request.getLastModified());
			System.out.println("Input date: " + date);
			httpGet.setHeader("If-Modified-Since",date);
		}
		InputStream content = null;
		try {
			HttpResponse httpResponse = client.execute(httpGet);
			StatusLine statusLine = httpResponse.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = httpResponse.getEntity();
				content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				String modified = httpResponse.getFirstHeader("Last-Modified")
						.getValue();
				System.out.println("Last Modified:"+ modified);
				Date lastModified = null;
				try {
					lastModified = DateUtils.parseDate(modified);
				} catch (DateParseException e) {
					// TODO - remove print stack
					e.printStackTrace();
					Log.e(BackEnd.class.toString(),
							"Invalid date in Last-Modified header");
				}
				response = new Response(builder.toString(), lastModified);
			} else if (statusCode == 304) {
				Log.i(BackEnd.class.toString(), "No updated");
				return null;
			} else {
				Log.e(BackEnd.class.toString(), "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (content != null)
				try {
					content.close();
				} catch (IOException e) {
					Log.e(BackEnd.class.toString(),
							"Failed to close the content");
				}
		}
		return response;
	}
}