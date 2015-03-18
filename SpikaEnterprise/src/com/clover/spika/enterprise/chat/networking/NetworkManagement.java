package com.clover.spika.enterprise.chat.networking;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.listeners.ProgressBarListeners;
import com.clover.spika.enterprise.chat.networking.CustomMultiPartEntity.ProgressListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.Preferences;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class NetworkManagement {

	/**
	 * Post/upload file
	 * 
	 * @param postParams
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static String httpPostFileRequest(Preferences prefs, HashMap<String, String> postParams, final ProgressBarListeners listener) throws ClientProtocolException,
			IOException, JSONException {

		HttpPost httppost = new HttpPost(Const.BASE_URL + Const.F_USER_UPLOAD_FILE);

		httppost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

		httppost.setHeader(Const.TOKEN_BIG_T, prefs.getToken());

		httppost.setHeader(Const.APP_VERSION, Helper.getAppVersion());
		httppost.setHeader(Const.PLATFORM, "android");

		if (postParams.size() > 0) {
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

			File file = new File(postParams.get(Const.FILE));
			FileBody fb = new FileBody(file);

			builder.addPart(Const.FILE, fb);

			final HttpEntity entity = builder.build();
			CustomMultiPartEntity progEntity = new CustomMultiPartEntity(new ProgressListener() {

				@Override
				public void transferred(long num, long total) {
					listener.onSetMax(total);
					listener.onProgress(num);
				}
			}, entity);

			httppost.setEntity(progEntity);
		}

		HttpResponse response = HttpSingleton.getInstance().execute(httppost);
		HttpEntity entity = response.getEntity();

		return getString(entity.getContent());
	}
	
	private static String getString(InputStream is) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
		StringBuilder builder = new StringBuilder();
		String line = null;

		while ((line = reader.readLine()) != null) {
			builder.append(line + "\n");
		}

		is.close();

		return builder.toString();
	}

	public static HttpEntity httpGetGetFile(Preferences prefs, String apiUrl, HashMap<String, String> getParams) throws IllegalStateException, IOException, JSONException {
		String params = "";
		String gifString = null;

		// form parameters
		if (getParams != null && !getParams.isEmpty()) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entity : getParams.entrySet()) {
				nameValuePairs.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
				if (entity.getKey() == "file_id" && entity.getValue().startsWith("http")) {
					gifString = entity.getValue();
					break;
				}
			}

			params += URLEncodedUtils.format(nameValuePairs, "UTF-8");
		}

		HttpGet httpGet = new HttpGet(Const.BASE_URL + (TextUtils.isEmpty(apiUrl) ? "" : apiUrl) + (TextUtils.isEmpty(params) ? "" : "?" + params));
		if (gifString != null)
			httpGet = new HttpGet(gifString);
		Logger.custom("RawRequest", httpGet.getURI().toString());

		httpGet.setHeader("Encoding", "UTF-8");
		httpGet.setHeader(Const.TOKEN_BIG_T, prefs.getToken());

		httpGet.setHeader(Const.APP_VERSION, Helper.getAppVersion());
		httpGet.setHeader(Const.PLATFORM, "android");

		HttpResponse response = HttpSingleton.getInstance().execute(httpGet);
		HttpEntity entity = response.getEntity();

		return entity;
	}
	
	/**
	 * HttpClient mini singleton
	 */
	public static class HttpSingleton {

		private static HttpClient sInstance = null;
		private static long sTimestamp = 0L;
		private static long sHour = 3600L;

		public static HttpClient getInstance() {

			long current = System.currentTimeMillis() / 1000L;

			if (sInstance == null || (current > (sTimestamp + sHour))) {

				sTimestamp = System.currentTimeMillis() / 1000L;

				HttpParams params = new BasicHttpParams();
				params.setParameter(CoreProtocolPNames.USER_AGENT, Const.HTTP_USER_AGENT);
				params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
				final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
				schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));

				ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

				sInstance = new DefaultHttpClient(cm, params);
			}

			return sInstance;
		}
	}

	// start: App requests
	public static String httpPostRequest(HashMap<String, String> postParams, String token) throws IOException, JSONException {
		return httpPostRequest("", postParams, token);
	}

	public static String httpPostRequest(String apiUrl, HashMap<String, String> postParams) throws IOException {
		return httpPostRequest(apiUrl, postParams, null);
	}

	public static String httpPostRequest(String apiUrl, HashMap<String, String> postParams, String token) throws IOException {
		return postOkRequest(Const.BASE_URL + (TextUtils.isEmpty(apiUrl) ? "" : apiUrl), postParams, true, token);
	}

	public static String httpGetRequest(String apiUrl, HashMap<String, String> getParams) throws IOException {
		return httpGetRequest(apiUrl, getParams, null);
	}

	public static String httpGetRequest(String apiUrl, HashMap<String, String> getParams, String token) throws IOException {
		return getOkRequest(Const.BASE_URL + (TextUtils.isEmpty(apiUrl) ? "" : apiUrl), getParams, true, token);
	}
	
	public static JSONObject httpGetCustomUrlRequest(String apiUrl, HashMap<String, String> getParams) throws IOException {
		return Helper.jObjectRawFromString(getOkRequest(apiUrl, getParams, false, null));
	}
	
	public static String httpGetRequestWithRawResponse(String url) throws IOException {
		return getOkRequest(url, null, false, null);
	}
	// end: App requests

	// start: request handling
	private static String getOkRequest(String url, HashMap<String, String> getParams, boolean addHeaders, String token) throws IOException {
		
		GetUrl urlParams = new GetUrl(getParams);
		String finalUrl = url + urlParams.toString();

		Request.Builder requestBuilder = new Request.Builder().url(finalUrl);
		
		if(addHeaders){
			requestBuilder.headers(getGetHeadersWithToken(token));
		}
		
		Request request = requestBuilder.build();
		Response response = client().newCall(request).execute();

		return response.body().string();
	}

	private static String postOkRequest(String url, HashMap<String, String> postParams, boolean addHeaders, String token) throws IOException {

		FormEncodingBuilder formBodyBuilder = new FormEncodingBuilder();

		if (postParams != null) {
			for (Map.Entry<String, String> entry : postParams.entrySet()) {
				formBodyBuilder.add(entry.getKey(), entry.getValue());
			}
		}
		
		Request.Builder requestBuilder = new Request.Builder().url(url).post(formBodyBuilder.build());
		
		if(addHeaders){
			requestBuilder.headers(getPostHeadersWithToken(token));
		}
		
		Request request = requestBuilder.build();
		Response response = client().newCall(request).execute();

		return response.body().string();
	}
	
	private static OkHttpClient client() {
		return new OkHttpClient();
	}
	
	public static Headers getPostHeadersWithContext(Context ctx){
		return postHeaders(null, ctx);
	}
	
	public static Headers getPostHeadersWithToken(String token){
		return postHeaders(token, null);
	}
	
	private static Headers postHeaders(String token, Context ctx) {

		Headers.Builder headersBuilder = new Headers.Builder()
		.add("Encoding", "UTF-8")
		.add(Const.APP_VERSION, Helper.getAppVersion())
		.add(Const.PLATFORM, "android")
		.add("User-Agent", Const.HTTP_USER_AGENT);

		if(TextUtils.isEmpty(token)){
		 token = SpikaEnterpriseApp.getSharedPreferences(ctx).getToken();
		}
		
		if (!TextUtils.isEmpty(token)) {
			headersBuilder.add(Const.TOKEN_BIG_T, token);
		}

		return headersBuilder.build();
	}
	
	public static Headers getGetHeadersWithContext(Context ctx){
		return getHeaders(null, ctx);
	}
	
	public static Headers getGetHeadersWithToken(String token){
		return getHeaders(token, null);
	}

	private static Headers getHeaders(String token, Context ctx) {
		
		Headers.Builder headersBuilder = new Headers.Builder()
		.add("Encoding", "UTF-8")
		.add(Const.APP_VERSION, Helper.getAppVersion())
		.add(Const.PLATFORM, "android")
		.add("User-Agent", Const.HTTP_USER_AGENT);

		if(TextUtils.isEmpty(token)){
			 token = SpikaEnterpriseApp.getSharedPreferences(ctx).getToken();
		}
		
		if (!TextUtils.isEmpty(token)) {
			headersBuilder.add(Const.TOKEN_BIG_T, token);
		}
		
		return headersBuilder.build();
	}
	// end: Request handling

}
