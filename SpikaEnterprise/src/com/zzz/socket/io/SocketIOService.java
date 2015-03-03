package com.zzz.socket.io;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.json.JSONException;
import org.json.JSONObject;

import zzz.my.autobahn.WebSocketConnection;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.google.gson.Gson;
import com.zzz.socket.models.CallMessage;
import com.zzz.socket.models.CheckAvailableRoom;
import com.zzz.test.socket.SocketClient;

public class SocketIOService extends Service {
	
	private int action = Const.ACTION_IDLE;
	
	private boolean isServiceStarted = false;
	private long openedTime = 0;
	private User user;
	private String sessionId = "1";
	private String activeSessionOfInteractiveUser = "-1";
	private int id = 2;
	
	private final WebSocketConnection mConn = new WebSocketConnection();
	private final IBinder mBinder = new LocalBinder();
	
	/**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	public SocketIOService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SocketIOService.this;
        }
    }

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d("LOG", "ON create "+isServiceStarted);
		
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {

		if (!isServiceStarted) {
			isServiceStarted = true;
		}
		
		new Handler().post(new Runnable() {
			
			@Override
			public void run() {
				connect();
			}
		});
		
//		new SocketClient().getSessionId(true, new ApiCallback<String>() {
//			
//			@Override
//			public void onApiResponse(Result<String> result) {
//				Log.d("LOG", "res: "+result.toString());
//				sessionId = result.getResultData();
//				if(sessionId == null){
//					return;
//				}
//				sessionId = sessionId.substring(0, sessionId.indexOf(":"));
//				Log.d("LOG", "sessionId: "+sessionId);
//				user = Helper.getUser(SocketService.this);
//				
//				work(sessionId);
//			}
//			
//		});
		
//		user = (User) intent.getSerializableExtra(Const.USER);
//		String sessionId = intent.getStringExtra("SSS");
//		work(sessionId);
		
		Log.d("LOG", "ON COMMAND START");
		
		return Service.START_STICKY; 
	}
	
	public void connect(){
		if(mConn != null && mConn.isConnected()){
			Log.e("LOG", "CONNECTED");
			mConn.disconnect();
		}
		new SocketClient().getSessionId(true, new ApiCallback<String>() {
			
			@Override
			public void onApiResponse(Result<String> result) {
				Log.d("LOG", "res: "+result.toString());
				sessionId = result.getResultData();
				if(sessionId == null){
					return;
				}
				sessionId = sessionId.substring(0, sessionId.indexOf(":"));
				Log.d("LOG", "sessionId: "+sessionId);
				user = Helper.getUser(SocketIOService.this);
				
				try {
					work(sessionId);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}
	
	private static final String WS_URL = "wss://www.spikaent.com";
	private static final String WS_PORT = "32443";
	private static final String WS_SUFIX_URL = "/socket.io/1/websocket/";

	private void work(String sessionId) throws MalformedURLException, NoSuchAlgorithmException {

		final String wsString = WS_URL + ":" + WS_PORT + WS_SUFIX_URL + sessionId;
		Log.d("LOG", wsString);
		
		SocketIO.setDefaultSSLSocketFactory(SSLContext.getDefault());
		SocketIO socket = new SocketIO();
		
		socket.connect(wsString, new IOCallback() {
			
			@Override
			public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
				// TODO Auto-generated method stub
				try {
                    Log.d("LOG", "Server said:" + arg0.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
			}
			
			@Override
			public void onMessage(String arg0, IOAcknowledge arg1) {
				// TODO Auto-generated method stub
				Log.d("LOG", "Server said MESS:" + arg0);
			}
			
			@Override
			public void onError(SocketIOException arg0) {
				// TODO Auto-generated method stub
				Log.d("LOG", "ERROR:" + arg0);
			}
			
			@Override
			public void onDisconnect() {
				// TODO Auto-generated method stub
				Log.d("LOG", "DISSCONETS:");
			}
			
			@Override
			public void onConnect() {
				// TODO Auto-generated method stub
				Log.d("LOG", "CONNECT:");
			}
			
			@Override
			public void on(String arg0, IOAcknowledge arg1, Object... arg2) {
				// TODO Auto-generated method stub
				Log.d("LOG", "ON:");
			}
		});
		
		URI uri = null;
		try {
			uri = new URI(wsString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}
	
	private void onMessageReceive(String message){
		Intent inBroadcast = new Intent();
		inBroadcast.setAction(Const.SOCKET_ACTION);
		if(message.equals("2::")){
			mConn.sendTextMessage(message);
		}else if(message.startsWith("6:::0+[null,")){
			if(action == Const.ACTION_CHECK){
				String toJson = message.substring(12, message.length() - 1);
				try {
					CheckAvailableRoom item = new Gson().fromJson(toJson, CheckAvailableRoom.class);
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CHECK_USER_AVAILABLE);
					if(item.getClients().size() == 1){
						inBroadcast.putExtra(Const.AVAILABLE_TYPE, Const.USER_AVAILABLE);
						inBroadcast.putExtra(Const.SESSION_ID, (String)item.getClients().keySet().toArray()[0]);
						activeSessionOfInteractiveUser = (String)item.getClients().keySet().toArray()[0];
						Log.i("LOG", "user is available");
					}else if(item.getClients().size() < 1){
						inBroadcast.putExtra(Const.AVAILABLE_TYPE, Const.USER_NOT_CONNECTED);
						Log.i("LOG", "user not connected");
					}else{
						inBroadcast.putExtra(Const.AVAILABLE_TYPE, Const.USER_BUSY);
						Log.i("LOG", "user is busy");
					}
				} catch (Exception e) {
					inBroadcast.putExtra(Const.AVAILABLE_TYPE, Const.USER_NOT_CONNECTED);
					e.printStackTrace();
				}
				LocalBroadcastManager.getInstance(this).sendBroadcast(inBroadcast);
				action = Const.ACTION_IDLE;
			}
		}else if(message.equals("6:::2")){
			if(action == Const.ACTION_CALL){
				inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_USER);
				inBroadcast.putExtra(Const.SESSION_ID, activeSessionOfInteractiveUser);
				
				LocalBroadcastManager.getInstance(this).sendBroadcast(inBroadcast);
				
				action = Const.ACTION_IDLE;
			}
		}else if(message.startsWith("5:::")){
			if(action == Const.ACTION_CALL_CANCEL){
				String toJson = message.substring(4, message.length());
				try {
					CallMessage item = new Gson().fromJson(toJson, CallMessage.class);
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_ENDED);
					
					if(item.getArgs().get(0).getType().equals("callEnd")){
						User user = Helper.getUser(this);
						mConn.sendTextMessage(joinRoomMessage(String.valueOf(user.getId()), user.getFirstName(), user.getImage(), user.getImageThumb(), user.getLastName(), "join", id));
						id++;
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				LocalBroadcastManager.getInstance(this).sendBroadcast(inBroadcast);
				action = Const.ACTION_IDLE;
			}
		}
	}
	
	private String joinRoomMessage(String userId, String firstName, String image, String imageThumb, String lastName, String action, int id){
		String json = "{"
					+ "\"args\":{"
						+ "\"room_id\":\"" + userId + "\","
						+ "\"user\":{"
							+ "\"firstname\":\"" + firstName + "\","
							+ "\"image\":\"" + image + "\","
							+ "\"image_thumb\":\"" + imageThumb + "\","
							+ "\"lastname\":\"" + lastName + "\","
							+ "\"user_id\":\"" + userId + "\""
						+ "}"
					+ "},"
					+ "\"name\" : " + "\"" + action + "\""
				+ "}";
		String message = "5:"+id+"+::" + json;
		return message;
	}

	@Override
	public void onDestroy() {
		Log.d("LOG", "ON DESTROY");
		isServiceStarted = false;
		if(mConn != null)mConn.disconnect();
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public void callOffer(String userId){
		action = Const.ACTION_CHECK;
		mConn.sendTextMessage(checkIsRoomAvailableMessage(userId));
	}
	
	public void call(String sessionId){
		action = Const.ACTION_CALL;
		mConn.sendTextMessage(callMessage(sessionId, "callOffer", id));
		id++;
	}
	
	public void callCancel(String sessionId){
		action = Const.ACTION_CALL_CANCEL;
		mConn.sendTextMessage(callMessage(sessionId, "callCancel", id));
		activeSessionOfInteractiveUser = "-1";
		id++;
	}
	
	public String callMessage(String sessionId, String type, int id){
		User user = Helper.getUser(this);
		String action = "message";
		String json = "{"
				+ "\"args\":{"
					+ "\"payload\":{"
						+ "\"user\":{"
							+ "\"firstname\":\"" + user.getFirstName() + "\","
							+ "\"image\":\"" + user.getImage() + "\","
							+ "\"image_thumb\":\"" + user.getImageThumb() + "\","
							+ "\"lastname\":\"" + user.getLastName() + "\","
							+ "\"user_id\":\"" + user.getId() + "\""
						+ "}"
					+ "},"
					+ "\"to\" : \"" + sessionId + "\","
					+ "\"type\" : \""+ type +"\""
				+ "},"
				+ "\"name\" : " + "\"" + action + "\""
			+ "}";
		String message = "5:"+ id +"::" + json;
		Log.d("LOG", "JSON REQ: " + message);
		return message;
	}
	
	private String checkIsRoomAvailableMessage(String userId){
		String json = "{\"args\" : \"" + userId +"\", \"name\" : \"room\"}";
		String message = "5:0+::" + json;
		return message;
	}


}
