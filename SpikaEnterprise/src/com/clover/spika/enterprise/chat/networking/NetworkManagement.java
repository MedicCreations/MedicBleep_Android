package com.clover.spika.enterprise.chat.networking;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.listeners.ProgressBarListeners;
import com.clover.spika.enterprise.chat.networking.CustomMultiPartEntity.ProgressListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.Preferences;

public class NetworkManagement {

	public static final String TOKEN = "Token";

	public static JSONObject httpPostRequest(HashMap<String, String> postParams, String token) throws IOException, JSONException {
		return httpPostRequest("", postParams, token);
	}

	/**
	 * Http POST request
	 * 
	 * @param apiUrl
	 * @param postParams
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject httpPostRequest(String apiUrl, HashMap<String, String> postParams) throws IOException, JSONException {
		return httpPostRequest(apiUrl, postParams, null);
	}

	public static JSONObject httpPostRequest(String apiUrl, HashMap<String, String> postParams, String token) throws IOException, JSONException {

		HttpPost httppost = new HttpPost(Const.BASE_URL + (TextUtils.isEmpty(apiUrl) ? "" : apiUrl));
		Logger.custom("RawRequest", httppost.getURI().toString());

		httppost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

		httppost.setHeader("Encoding", "UTF-8");
		if (!TextUtils.isEmpty(token)) {
			httppost.setHeader("token", token);
		}

		httppost.setHeader(Const.APP_VERSION, Helper.getAppVersion());
		httppost.setHeader(Const.PLATFORM, "android");

		// form parameters
		if (postParams != null && !postParams.isEmpty()) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entity : postParams.entrySet()) {
				nameValuePairs.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
			}

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		}

		HttpResponse response = HttpSingleton.getInstance().execute(httppost);
		HttpEntity entity = response.getEntity();

		return Helper.jObjectFromString(getString(entity.getContent()));
	}

	public static JSONObject httpGetRequest(String apiUrl, HashMap<String, String> getParams) throws IOException {
		return httpGetRequest(apiUrl, getParams, null);
	}

	public static JSONObject httpGetRequest(String apiUrl, HashMap<String, String> getParams, String token) throws IOException {

		String params = "";

		// form parameters
		if (getParams != null && !getParams.isEmpty()) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entity : getParams.entrySet()) {
				nameValuePairs.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
			}

			params += URLEncodedUtils.format(nameValuePairs, "UTF-8");
		}

		HttpGet httpGet = new HttpGet(Const.BASE_URL + (TextUtils.isEmpty(apiUrl) ? "" : apiUrl) + (TextUtils.isEmpty(params) ? "" : "?" + params));
		Logger.custom("RawRequest", httpGet.getURI().toString());

		httpGet.setHeader("Encoding", "UTF-8");
		if (!TextUtils.isEmpty(token)) {
			httpGet.setHeader("token", token);
		}

		httpGet.setHeader(Const.APP_VERSION, Helper.getAppVersion());
		httpGet.setHeader(Const.PLATFORM, "android");

		HttpResponse response = HttpSingleton.getInstance().execute(httpGet);
		HttpEntity entity = response.getEntity();

		return Helper.jObjectFromString(getString(entity.getContent()));
	}

	public static JSONObject httpGetCustomUrlRequest(String apiUrl, HashMap<String, String> getParams) throws IOException {

		String params = "";

		// form parameters
		if (getParams != null && !getParams.isEmpty()) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entity : getParams.entrySet()) {
				nameValuePairs.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
			}

			params += URLEncodedUtils.format(nameValuePairs, "UTF-8");
		}

		HttpGet httpGet = new HttpGet(apiUrl + (TextUtils.isEmpty(params) ? "" : "?" + params));
		Logger.custom("RawRequest", httpGet.getURI().toString());

		httpGet.setHeader("Encoding", "UTF-8");

		HttpResponse response = HttpSingleton.getInstance().execute(httpGet);
		HttpEntity entity = response.getEntity();

		return Helper.jObjectRawFromString(getString(entity.getContent()));
	}

	/**
	 * Post/upload file
	 * 
	 * @param postParams
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject httpPostFileRequest(Preferences prefs, HashMap<String, String> postParams, final ProgressBarListeners listener) throws ClientProtocolException,
			IOException, JSONException {

		HttpPost httppost = new HttpPost(Const.BASE_URL + Const.F_USER_UPLOAD_FILE);

		httppost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

		httppost.setHeader(TOKEN, prefs.getToken());

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

		return Helper.jObjectFromString(getString(entity.getContent()));
	}

	public static HttpEntity httpGetGetFile(Preferences prefs, String apiUrl, HashMap<String, String> getParams) throws IllegalStateException, IOException, JSONException {
		String params = "";
		String gifString = null;
		
		// form parameters
		if (getParams != null && !getParams.isEmpty()) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entity : getParams.entrySet()) {
				nameValuePairs.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
				if(entity.getKey() == "file_id" && entity.getValue().startsWith("http")){
					gifString = entity.getValue();
					break;
				}
			}

			params += URLEncodedUtils.format(nameValuePairs, "UTF-8");
		}
		
		HttpGet httpGet = new HttpGet(Const.BASE_URL + (TextUtils.isEmpty(apiUrl) ? "" : apiUrl) + (TextUtils.isEmpty(params) ? "" : "?" + params));
		if(gifString != null) httpGet = new HttpGet(gifString);
		Logger.custom("RawRequest", httpGet.getURI().toString());
		
		httpGet.setHeader("Encoding", "UTF-8");
		httpGet.setHeader(TOKEN, prefs.getToken());

		httpGet.setHeader(Const.APP_VERSION, Helper.getAppVersion());
		httpGet.setHeader(Const.PLATFORM, "android");

		HttpResponse response = HttpSingleton.getInstance().execute(httpGet);
		HttpEntity entity = response.getEntity();

		return entity;
	}

	/**
	 * Get string from InputStream, Http response
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String getString(InputStream is) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
		StringBuilder builder = new StringBuilder();
		String line = null;

		while ((line = reader.readLine()) != null) {
			builder.append(line + "\n");
		}

		is.close();

		return builder.toString();
	}

	/**
	 * Checks whether this app has mobile or wireless internet connection
	 * 
	 * @return
	 */
	public static boolean hasNetworkConnection(Context context) {
		
		if(context == null) return true;

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

		for (NetworkInfo ni : networkInfo) {

			if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
				if (ni.isConnected()) {
					return true;
				}
			}

			if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
				if (ni.isConnected()) {
					return true;
				}
			}
		}

		return false;
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

		private static HttpClient sClosableInstance = null;

		/**
		 * Does not work needs to studied more
		 * 
		 * @return
		 */
		public static HttpClient getNewInstance() {

			if (sClosableInstance == null) {

				HttpClientBuilder builder = HttpClientBuilder.create();
				builder.setUserAgent(Const.HTTP_USER_AGENT);
				builder.setMaxConnPerRoute(20);

				sClosableInstance = builder.build();
			}

			return sClosableInstance;
		}
	}
	
	public static String httpGetRequestWithRawResponse(String url) throws IOException {

		HttpGet httpGet = new HttpGet(url);
		Logger.custom("RawRequest", httpGet.getURI().toString());

//		HttpResponse response = HttpSingleton.getInstance().execute(httpGet);
		HttpResponse response = getNewHttpClient().execute(httpGet);
		HttpEntity entity = response.getEntity();

		return getString(entity.getContent());
	}
	
	public static HttpClient getNewHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        final SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}
	
	public static class MySSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");

	    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(truststore);

	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };

	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }

	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	    }
	}

}
