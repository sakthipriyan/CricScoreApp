package com.sakthipriyan.cricscore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

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

	private HttpClient client;

	private BackEnd() {
		client = new DefaultHttpClient(new BasicHttpParams());
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

		HttpGet httpGet = new HttpGet(getURL(request.getMatchIds()));
		if (request.getLastModified() != null) {
			httpGet.setHeader("If-Modified-Since",
					DateUtils.formatDate(request.getLastModified()));
		}

		InputStream content = null;
		try {

			HttpResponse httpResponse = client.execute(httpGet);

			StatusLine statusLine = httpResponse.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			if (statusCode == 200) {
				content = httpResponse.getEntity().getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				StringBuilder builder = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				Date lastModified = null;
				try {
					lastModified = DateUtils.parseDate(httpResponse
							.getFirstHeader("Last-Modified").getValue());
				} catch (DateParseException e) {
					Log.e(BackEnd.class.toString(),
							"Invalid date in Last-Modified header");
				}
				response = new Response(builder.toString(), lastModified);
			} else if (statusCode == 304) {
				Log.i(BackEnd.class.toString(), "No updated");
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