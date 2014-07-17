package com.clover.spika.enterprise.chat.networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.Preferences;

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
import org.apache.http.entity.StringEntity;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkManagement {

    // <!-- HTTP User Agent -->
    private static final String HTTP_USER_AGENT = "VectorChat Android v1.0";

    public static final String TOKEN = "Token";

    public static JSONObject httpPostRequest(HashMap<String, String> postParams, JSONObject reqData) throws IOException, JSONException {
        return httpPostRequest("", postParams, reqData);
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
    public static JSONObject httpPostRequest(String apiUrl, HashMap<String, String> postParams, JSONObject reqData) throws IOException, JSONException {

        // TODO: što s parametrom reqData?

        HttpPost httppost = new HttpPost(Const.BASE_URL + (TextUtils.isEmpty(apiUrl) ? "" : apiUrl));
        Logger.custom("RawRequest", httppost.getURI().toString());

        httppost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

        httppost.setHeader("Encoding", "utf-8");

        // form parameters
        if (postParams != null && !postParams.isEmpty()) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entity : postParams.entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
            }
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            Logger.custom("RawRequestParams", nameValuePairs.toString()); // TODO: maknuti ovaj logger !!
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

            params += URLEncodedUtils.format(nameValuePairs, "utf-8");
        }

        HttpGet httpGet = new HttpGet(Const.BASE_URL + (TextUtils.isEmpty(apiUrl) ? "" : apiUrl) + (TextUtils.isEmpty(params) ? "" : "?" + params));
        Logger.custom("RawRequest", httpGet.getURI().toString());

        httpGet.setHeader("Encoding", "utf-8");
        if (!TextUtils.isEmpty(token)) {
            httpGet.setHeader("token", token);
        }

        HttpResponse response = HttpSingleton.getInstance().execute(httpGet);
        HttpEntity entity = response.getEntity();

        return Helper.jObjectFromString(getString(entity.getContent()));
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
    public static JSONObject httpPostFileRequest(Preferences prefs, HashMap<String, String> postParams) throws ClientProtocolException, IOException, JSONException {

        HttpPost httppost = new HttpPost(Const.BASE_URL);

        httppost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

        httppost.setHeader(TOKEN, prefs.getToken());

        if (postParams.size() > 0) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            for (Map.Entry<String, String> entry : postParams.entrySet()) {
                final File file = new File(entry.getValue());
                FileBody fb = new FileBody(file);

                builder.addPart(entry.getKey(), fb);
            }

            final HttpEntity entity = builder.build();
            httppost.setEntity(entity);
        }

        HttpResponse response = HttpSingleton.getInstance().execute(httppost);
        HttpEntity entity = response.getEntity();

        return Helper.jObjectFromString(getString(entity.getContent()));
    }

    /**
     * Post method for downloading a file
     *
     * @param params
     * @param reqData
     * @return InputStream
     * @throws IllegalStateException
     * @throws IOException
     * @throws JSONException
     */
    public static InputStream httpPostGetFile(HashMap<String, String> params, JSONObject reqData) throws IllegalStateException, IOException, JSONException {
        HttpPost httppost = new HttpPost(Const.BASE_URL);

        httppost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

        httppost.setHeader("Content-Type", "application/json");
        httppost.setHeader("Encoding", "utf-8");

        if (params.size() > 0) {
            JSONObject postBody = new JSONObject();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                postBody.put(entry.getKey(), entry.getValue());
            }

            if (reqData != null) {
                postBody.put(Const.REQDATA, reqData);
            }

            httppost.setEntity(new StringEntity(postBody.toString(), "UTF-8"));
        }

        HttpResponse response = HttpSingleton.getInstance().execute(httppost);
        HttpEntity entity = response.getEntity();

        return entity.getContent();
    }
    
    public static InputStream httpGetGetFile(String apiUrl, HashMap<String, String> getParams) throws IllegalStateException, IOException, JSONException {
    	String params = "";

        // form parameters
        if (getParams != null && !getParams.isEmpty()) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entity : getParams.entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
            }

            params += URLEncodedUtils.format(nameValuePairs, "utf-8");
        }

        HttpGet httpGet = new HttpGet(Const.BASE_URL + (TextUtils.isEmpty(apiUrl) ? "" : apiUrl) + (TextUtils.isEmpty(params) ? "" : "?" + params));
        Logger.custom("RawRequest", httpGet.getURI().toString());

        httpGet.setHeader("Encoding", "utf-8");

        HttpResponse response = HttpSingleton.getInstance().execute(httpGet);
        HttpEntity entity = response.getEntity();

        return entity.getContent();
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
                params.setParameter(CoreProtocolPNames.USER_AGENT, HTTP_USER_AGENT);
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

}
