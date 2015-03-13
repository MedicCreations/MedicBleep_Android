package com.clover.spika.enterprise.chat.webrtc.socket;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.clover.spika.enterprise.chat.ChangePasswordActivity;
import com.clover.spika.enterprise.chat.ChooseOrganizationActivity;
import com.clover.spika.enterprise.chat.LoginActivity;
import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.robospice.LoginSpice;
import com.clover.spika.enterprise.chat.extendables.LoginBaseActivity;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Organization;
import com.clover.spika.enterprise.chat.models.PreLogin;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.PushHandle;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.webrtc.LooperExecutor;
import com.clover.spika.enterprise.chat.webrtc.WebSocketChannelClient;
import com.clover.spika.enterprise.chat.webrtc.autobahn.WebSocketConnection;
import com.clover.spika.enterprise.chat.webrtc.autobahn.WebSocketConnectionHandler;
import com.clover.spika.enterprise.chat.webrtc.autobahn.WebSocketException;
import com.clover.spika.enterprise.chat.webrtc.socket.models.CallMessage;
import com.clover.spika.enterprise.chat.webrtc.socket.models.CheckAvailableRoom;
import com.clover.spika.enterprise.chat.webrtc.socket.models.SocketParser;
import com.clover.spika.enterprise.chat.webrtc.socket.models.WebRtcSDPMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class SocketService extends Service {

	private static final String WS_URL = "wss://www.spikaent.com";
	private static final String WS_PORT = "32443";
	private static final String WS_SUFIX_URL = "/socket.io/1/websocket/";

	private int action = Const.ACTION_IDLE;

	private User user;
	private String sessionId = "1";
	private String activeSessionOfInteractiveUser = "-1";
	private User activeUserInteractive = null;
	private int id = 2;

	private String wsString;

	private WebSocketChannelClient.WebSocketChannelEvents events = null;
	private LooperExecutor executor = null;
	private boolean isWebRtc = false;
	private WebSocketChannelClient.WebSocketConnectionState state;

	private WebSocketConnection mConn = new WebSocketConnection();
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public SocketService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return SocketService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Logger.custom("d", "LOG", "Service Created");

	}

	public WebSocketConnection getWebSocketConnection() {
		return mConn;
	}

	public String getUrl() {
		return wsString;
	}

	public void setWebRtcParameters(WebSocketChannelClient.WebSocketChannelEvents events, LooperExecutor executor, WebSocketChannelClient.WebSocketConnectionState state) {
		this.events = events;
		this.executor = executor;
		this.state = state;
		isWebRtc = true;
	}

	public void setIsInWebRtc(boolean isIn) {
		isWebRtc = isIn;
	}

	public void setState(WebSocketChannelClient.WebSocketConnectionState state) {
		this.state = state;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {

		if (intent != null && intent.getBooleanExtra(Const.IS_APLICATION_OPEN, false)) {
			new Handler().post(new Runnable() {

				@Override
				public void run() {
					connect();
				}
			});
		} else {
			new Handler().post(new Runnable() {

				@Override
				public void run() {
					// because of this error
					// java.security.cert.CertPathValidatorException: Trust
					// anchor for certification path not found.
					connectWithFakeLoginApi();
				}
			});
		}

		return Service.START_STICKY;
	}

	public void connect() {
		if (mConn != null && mConn.isConnected()) {
			Logger.custom("e", "LOG", "ALLREADY CONNECTED");
			mConn.disconnect();
		}
		new SocketClient().getSessionId(true, new ApiCallback<String>() {

			@Override
			public void onApiResponse(Result<String> result) {
				sessionId = result.getResultData();
				if (sessionId == null) {
					return;
				}
				sessionId = sessionId.substring(0, sessionId.indexOf(":"));
				Logger.custom("d", "LOG", "Socket SessionId: " + sessionId);
				user = Helper.getUser(SocketService.this);

				work(sessionId);
			}

		});
	}

	public void connectWithFakeLoginApi() {
		if (mConn != null && mConn.isConnected()) {
			Logger.custom("e", "LOG", "ALLREADY CONNECTED");
			mConn.disconnect();
		}

		/* start:FakeApi A api reauest that is meant to fail - unknown hack */

		Headers.Builder headersBuilder = new Headers.Builder().add("Encoding", "UTF-8").add(Const.APP_VERSION, Helper.getAppVersion()).add(Const.PLATFORM, "android")
				.add("User-Agent", Const.HTTP_USER_AGENT);

		String token = SpikaEnterpriseApp.getSharedPreferences(getApplicationContext()).getToken();
		if (!TextUtils.isEmpty(token)) {
			headersBuilder.add("token", token);
		}

		Headers header = headersBuilder.build();

		RequestBody formBody = new FormEncodingBuilder().add(Const.USERNAME, "FAKE").add(Const.PASSWORD, "FAKE").build();
		Request.Builder requestBuilder = new Request.Builder().headers(header).url(Const.BASE_URL + Const.F_PRELOGIN).post(formBody);

		OkHttpClient client = new OkHttpClient();

		Call connection = client.newCall(requestBuilder.build());

		try {
			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responsBody = resBody.string();

			PreLogin preLogin = new ObjectMapper().readValue(responsBody, PreLogin.class);
		} catch (Exception ex) {
		}
		/* end:FakeApi */

		new SocketClient().getSessionId(true, new ApiCallback<String>() {

			@Override
			public void onApiResponse(Result<String> result) {
				sessionId = result.getResultData();
				if (sessionId == null) {
					return;
				}
				sessionId = sessionId.substring(0, sessionId.indexOf(":"));
				Logger.custom("d", "LOG", "Socket SessionId: " + sessionId);
				user = Helper.getUser(SocketService.this);

				if (user.getId() == -1) {
					SocketService.this.stopSelf();
				} else {
					work(sessionId);
				}
			}

		});
	}

	private void work(String sessionId) {

		wsString = WS_URL + ":" + WS_PORT + WS_SUFIX_URL + sessionId;
		Logger.custom("d", "LOG", "Connected to Socket: " + wsString);

		URI uri = null;
		try {
			uri = new URI(wsString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		try {

			mConn.connect(uri, new WebSocketConnectionHandler() {

				@Override
				public void onTextMessage(String payload) {
					Logger.custom("d", "WEBSOCKET", "MESSAGE RECEIVED: " + payload);
					onMessageReceive(payload);
				}

				@Override
				public void onRawTextMessage(byte[] payload) {
				}

				@Override
				public void onOpen() {
					Intent inBroadcast = new Intent();
					inBroadcast.setAction(Const.SOCKET_ACTION);
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.WEB_SOCKET_OPENED);
					LocalBroadcastManager.getInstance(SocketService.this).sendBroadcast(inBroadcast);

					Logger.custom("d", "WEBSOCKET", "OPEN IN TIME: " + System.currentTimeMillis());

					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							joinMyRoom();
						}
					}, 100);
				}

				@Override
				public void onClose(WebSocketCloseNotification code, String reason) {
					Logger.custom("d", "WEBSOCKET", "CLOSE IN TIME: " + System.currentTimeMillis());
				}

				@Override
				public void onBinaryMessage(byte[] payload) {
				}
			});

		} catch (WebSocketException e) {
			e.printStackTrace();
		}

	}

	private void onMessageReceive(String message) {
		SocketParser socketParser = new SocketParser(message);
		if (socketParser.getType() == SocketParser.TYPE_HEARTBEAT) {
			mConn.sendTextMessage(message);
			return;
		}

		if (!isActiveApp()) {
			inactiveAppHandleMessage(socketParser);
			return;
		}

		Intent inBroadcast = new Intent();
		inBroadcast.setAction(Const.SOCKET_ACTION);

		if (socketParser.getType() == SocketParser.TYPE_ACK) {
			if (action == Const.ACTION_CHECK) {
				try {
					CheckAvailableRoom item = socketParser.parseCheckUser(socketParser.getData());
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CHECK_USER_AVAILABLE);
					if (item.getClients().size() == 1) {
						inBroadcast.putExtra(Const.AVAILABLE_TYPE, Const.USER_AVAILABLE);
						inBroadcast.putExtra(Const.SESSION_ID, (String) item.getClients().keySet().toArray()[0]);
						activeSessionOfInteractiveUser = (String) item.getClients().keySet().toArray()[0];
					} else if (item.getClients().size() < 1) {
						inBroadcast.putExtra(Const.AVAILABLE_TYPE, Const.USER_NOT_CONNECTED);
					} else {
						inBroadcast.putExtra(Const.AVAILABLE_TYPE, Const.USER_BUSY);
					}
				} catch (Exception e) {
					inBroadcast.putExtra(Const.AVAILABLE_TYPE, Const.USER_NOT_CONNECTED);
					e.printStackTrace();
				}
				LocalBroadcastManager.getInstance(this).sendBroadcast(inBroadcast);
				action = Const.ACTION_IDLE;
			} else if (action == Const.ACTION_CALL) {
				inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_USER);
				inBroadcast.putExtra(Const.SESSION_ID, activeSessionOfInteractiveUser);

				LocalBroadcastManager.getInstance(this).sendBroadcast(inBroadcast);

				action = Const.ACTION_IDLE;
			} else if (action == Const.ACTION_CALL_DECLINE) {
				callCancel(activeSessionOfInteractiveUser);
			} else if (action == Const.ACTION_JOIN_OTHER_ROOM) {
				inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_CONNECT);
				inBroadcast.putExtra(Const.USER, activeUserInteractive);
				LocalBroadcastManager.getInstance(this).sendBroadcast(inBroadcast);
				action = Const.ACTION_IDLE;
			}
		} else if (socketParser.getType() == SocketParser.TYPE_EVENT) {
			try {
				CallMessage item = socketParser.parseCallMessage(socketParser.getData());

				if (item.getArgs().get(0).getType().equals("callEnd") || item.getArgs().get(0).getType().equals("callDecline")) {
					joinMyRoom();
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_ENDED);
				} else if (item.getArgs().get(0).getType().equals("callAnswer")) {
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_ANSWER);
					inBroadcast.putExtra(Const.USER, item.getArgs().get(0).getPayload().getUser());
					activeUserInteractive = item.getArgs().get(0).getPayload().getUser();
					activeSessionOfInteractiveUser = item.getArgs().get(0).getFrom();
					inBroadcast.putExtra(Const.SESSION_ID, activeSessionOfInteractiveUser);
				} else if (item.getArgs().get(0).getType().equals("callOffer")) {
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_RECEIVE);
					inBroadcast.putExtra(Const.USER, item.getArgs().get(0).getPayload().getUser());
					activeSessionOfInteractiveUser = item.getArgs().get(0).getFrom();
					activeUserInteractive = item.getArgs().get(0).getPayload().getUser();
					inBroadcast.putExtra(Const.SESSION_ID, activeSessionOfInteractiveUser);
				} else if (item.getArgs().get(0).getType().equals("callRinging")) {
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_RINGING);
				} else if (item.getArgs().get(0).getType().equals("callCancel")) {
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_CANCELED);
				} else if (item.getArgs().get(0).getType().equals("mute") || item.getArgs().get(0).getType().equals("unmute")
						|| item.getArgs().get(0).getType().equals("muteRemoteVideo")) {
					inBroadcast.setAction("CALL");
					inBroadcast.putExtra(Const.MESSAGES, item);
				}
			} catch (Exception e) {
				if (action == Const.ACTION_LEAVE_MY_ROOM) {
					joinOtherUserRoom(String.valueOf(activeUserInteractive.getId()));
				} else if (action == Const.ACTION_LEAVE_OTHER_ROOM) {
					joinMyRoom();
				}
			}

			if (action == Const.ACTION_CALL_ACCEPT) {
				try {
					inBroadcast.setAction("CALL");
					WebRtcSDPMessage item = new ObjectMapper().readValue(socketParser.getData(), WebRtcSDPMessage.class);
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_ACCEPTED);
					inBroadcast.putExtra(Const.CANDIDATE, item);
				} catch (Exception e) {
				}
				action = Const.ACTION_IDLE;
			}

			LocalBroadcastManager.getInstance(this).sendBroadcast(inBroadcast);
			if (action != Const.ACTION_JOIN_OTHER_ROOM)
				action = Const.ACTION_IDLE;
		}

		if (isWebRtc && socketParser.getType() != SocketParser.TYPE_HEARTBEAT) {
			final String message2 = socketParser.getData();
			if (message2.length() < 7)
				return;
			executor.execute(new Runnable() {
				@Override
				public void run() {
					if (state == WebSocketChannelClient.WebSocketConnectionState.CONNECTED || state == WebSocketChannelClient.WebSocketConnectionState.REGISTERED) {
						events.onWebSocketMessage(message2);
					}
				}
			});
		}

	}

	private void inactiveAppHandleMessage(SocketParser socketParser) {
		try {
			CallMessage item = socketParser.parseCallMessage(socketParser.getData());

			if (item.getArgs().get(0).getType().equals("callOffer")) {
				activeSessionOfInteractiveUser = item.getArgs().get(0).getFrom();
				activeUserInteractive = item.getArgs().get(0).getPayload().getUser();
				User user = item.getArgs().get(0).getPayload().getUser();
				Intent intentInAp = new Intent(this, MainActivity.class);
				intentInAp.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_RECEIVE);
				intentInAp.putExtra(Const.USER, user);
				intentInAp.putExtra(Const.SESSION_ID, activeSessionOfInteractiveUser);
				intentInAp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentInAp);
			} else if (item.getArgs().get(0).getType().equals("callEnd")) {
				joinMyRoom();
				Intent inBroadcast = new Intent();
				inBroadcast.setAction("CALL");
				inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_ENDED);
				LocalBroadcastManager.getInstance(this).sendBroadcast(inBroadcast);
			}
		} catch (Exception e) {
			Logger.custom("e", "LOG", e.toString());
		}
	}

	@Override
	public void onDestroy() {

		Logger.custom("d", "LOG", "On Service Destroy");
		if (mConn != null)
			mConn.disconnect();

		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private boolean isActiveApp() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName componentInfo = null;
		boolean isActive = false;

		if (Utils.isBuildOver(Build.VERSION_CODES.KITKAT_WATCH)) {
			isActive = PushHandle.isActivePCG(am);
		} else {
			List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
			componentInfo = taskInfo.get(0).topActivity;
			isActive = componentInfo.getPackageName().equalsIgnoreCase("com.clover.spika.enterprise.chat");
		}

		boolean isScreenOn = true;
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (Utils.isBuildOver(Build.VERSION_CODES.KITKAT)) {
			isScreenOn = pm.isInteractive();
		} else {
			isScreenOn = pm.isScreenOn();
		}

		return isActive && isScreenOn;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void sendWebRtcMessage(String message) {
		mConn.sendTextMessage(new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id), "", createWebRtcMessage(message)).toString());
		id++;
	}

	public void sendWebRtcUnMuteOrMute(String videoOrAudio, String mute) {
		mConn.sendTextMessage(new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id), "", createMuteMessage(videoOrAudio, mute)).toString());
		id++;
	}

	public void sendWebRtcMessageInit() {
		String json = "{" + "\"args\":{" + "\"type\" : \"init\"," + "\"to\" : \"" + activeSessionOfInteractiveUser + "\"" + "}," + "\"name\" : " + "\"message\"" + "}";
		mConn.sendTextMessage(new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id - 1), "", json).toString());
	}

	public String createMuteMessage(String videoOrAudio, String mute) {

		String action = "message";
		String json = "{" + "\"args\":{" + "\"type\" : \"" + mute + "\"," + "\"to\" : \"" + activeSessionOfInteractiveUser + "\"," + "\"from\" : \"" + sessionId + "\","
				+ "\"roomType\" : \"video\"," + "\"payload\":{" + "\"name\" : \"" + videoOrAudio + "\"," + "\"user\":{" + "\"firstname\":\"" + user.getFirstName() + "\","
				+ "\"image\":\"" + user.getImage() + "\"," + "\"image_thumb\":\"" + user.getImageThumb() + "\"," + "\"lastname\":\"" + user.getLastName() + "\","
				+ "\"user_id\":\"" + user.getId() + "\"" + "}" + "}" + "}," + "\"name\" : " + "\"" + action + "\"" + "}";

		return json;
	}

	public String createWebRtcMessage(String mess) {
		String candidate = "";
		String sdpMid = "";
		String sdpMIndex = "";
		try {
			JSONObject jo = new JSONObject(mess);
			candidate = jo.getString("candidate");
			sdpMid = jo.getString("id");
			sdpMIndex = jo.getString("label");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String action = "message";
		String json = "{" + "\"args\":{" + "\"type\" : \"candidate\"," + "\"to\" : \"" + activeSessionOfInteractiveUser + "\"," + "\"payload\":{" + "\"candidate\":{"
				+ "\"sdpMLineIndex\":\"" + sdpMIndex + "\"," + "\"sdpMid\":\"" + sdpMid + "\"," + "\"candidate\":\"" + candidate + "\"" + "}," + "\"user\":{" + "\"firstname\":\""
				+ user.getFirstName() + "\"," + "\"image\":\"" + user.getImage() + "\"," + "\"image_thumb\":\"" + user.getImageThumb() + "\"," + "\"lastname\":\""
				+ user.getLastName() + "\"," + "\"user_id\":\"" + user.getId() + "\"" + "}" + "}," + "\"roomType\" : \"video\"" + "}," + "\"name\" : " + "\"" + action + "\"" + "}";

		return json;
	}

	public void sendWebRtcMessageOffer(String sdp) {
		String formatedSdp = sdp.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\r\\\\n");
		String action = "message";
		String json = "{" + "\"args\":{" + "\"type\" : \"offer\"," + "\"to\" : \"" + activeSessionOfInteractiveUser + "\"," + "\"payload\":{" + "\"sdp\" : \"" + formatedSdp
				+ "\"," + "\"type\" : \"offer\"," + "\"user\":{" + "\"firstname\":\"" + user.getFirstName() + "\"," + "\"image\":\"" + user.getImage() + "\","
				+ "\"image_thumb\":\"" + user.getImageThumb() + "\"," + "\"lastname\":\"" + user.getLastName() + "\"," + "\"user_id\":\"" + user.getId() + "\"" + "}" + "},"
				+ "\"roomType\" : \"video\"" + "}," + "\"name\" : " + "\"" + action + "\"" + "}";
		mConn.sendTextMessage(new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id), "", json).toString());
		id++;
	}

	public void sendWebRtcMessageOfferForAnswer(String sdp) {
		String formatedSdp = sdp.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\r\\\\n");
		String action = "message";
		String json = "{" + "\"args\":{" + "\"type\" : \"answer\"," + "\"to\" : \"" + activeSessionOfInteractiveUser + "\"," + "\"from\" : \"" + sessionId + "\","
				+ "\"payload\":{" + "\"sdp\" : \"" + formatedSdp + "\"," + "\"type\" : \"answer\"," + "\"user\":{" + "\"firstname\":\"" + user.getFirstName() + "\","
				+ "\"image\":\"" + user.getImage() + "\"," + "\"image_thumb\":\"" + user.getImageThumb() + "\"," + "\"lastname\":\"" + user.getLastName() + "\","
				+ "\"user_id\":\"" + user.getId() + "\"" + "}" + "}," + "\"roomType\" : \"video\"" + "}," + "\"name\" : " + "\"" + action + "\"" + "}";
		mConn.sendTextMessage(new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id), "", json).toString());
		id++;
	}

	public void callOffer(String userId) {
		action = Const.ACTION_CHECK;
		mConn.sendTextMessage(checkIsRoomAvailableMessage(userId));
		Logger.custom("d", "LOG", "CHECK USER ID: " + userId);
	}

	public void call(String sessionId, boolean isVideo) {
		action = Const.ACTION_CALL;
		mConn.sendTextMessage(callMessage(sessionId, "callOffer", id));
		Logger.custom("d", "LOG", "CALL OFFER");
		id++;
	}

	public void callCancel(String sessionId) {
		action = Const.ACTION_CALL_CANCEL;
		if (sessionId == null)
			sessionId = activeSessionOfInteractiveUser;
		mConn.sendTextMessage(callMessage(sessionId, "callCancel", id));
		activeSessionOfInteractiveUser = "-1";
		activeUserInteractive = null;
		Logger.custom("d", "LOG", "CALL CANCEL");
		id++;
	}

	public void callDecline(String sessionId) {
		action = Const.ACTION_CALL_DECLINE;
		if (sessionId == null)
			sessionId = activeSessionOfInteractiveUser;
		mConn.sendTextMessage(callMessage(sessionId, "callDecline", id));
		Logger.custom("d", "LOG", "CALL DECLINE");
		id++;
	}

	public void callEnd(String sessionId) {
		action = Const.ACTION_CALL_END;
		if (sessionId == null)
			sessionId = activeSessionOfInteractiveUser;
		mConn.sendTextMessage(callMessage(sessionId, "callEnd", id));
		Logger.custom("d", "LOG", "CALL END");
		id++;
	}

	public void callAccept(String sessionId) {
		action = Const.ACTION_CALL_ACCEPT;
		mConn.sendTextMessage(callMessage(sessionId, "callAnswer", id));
		Logger.custom("d", "LOG", "CALL ACCEPT");
		id++;
	}

	public void callRinging(String sessionId) {
		action = Const.ACTION_CALL_RINGING;
		mConn.sendTextMessage(callMessage(sessionId, "callRinging", id));
		Logger.custom("d", "LOG", "CALL RINGING");
		id++;
	}

	public void joinMyRoom() {
		User user = Helper.getUser(this);
		mConn.sendTextMessage(joinRoomMessage(String.valueOf(user.getId()), String.valueOf(user.getId()), user.getFirstName(), user.getImage(), user.getImageThumb(),
				user.getLastName(), "join", id));
		Logger.custom("d", "LOG", "JOIN MY ROOM");
		id++;
	}

	public void joinOtherUserRoom(String userId) {
		User user = Helper.getUser(this);
		mConn.sendTextMessage(joinRoomMessage(userId, String.valueOf(user.getId()), user.getFirstName(), user.getImage(), user.getImageThumb(), user.getLastName(), "join", id));
		action = Const.ACTION_JOIN_OTHER_ROOM;
		Logger.custom("d", "LOG", "JOIN OTHER ROOM");
		id++;
	}

	public void leaveMyRoom() {
		action = Const.ACTION_LEAVE_MY_ROOM;
		String mess = createLeaveMessage();
		mConn.sendTextMessage(mess);
		Logger.custom("d", "LOG", "LEAVE MY ROOM");
		id++;
	}

	public void leaveOtherRoom() {
		action = Const.ACTION_LEAVE_OTHER_ROOM;
		String mess = createLeaveMessage();
		mConn.sendTextMessage(mess);
		Logger.custom("d", "LOG", "LEAVE OTHER ROOM");
		id++;
	}

	public String callMessage(String sessionId, String type, int id) {
		User user = Helper.getUser(this);
		String action = "message";
		String json = "{" + "\"args\":{" + "\"payload\":{" + "\"user\":{" + "\"firstname\":\"" + user.getFirstName() + "\"," + "\"image\":\"" + user.getImage() + "\","
				+ "\"image_thumb\":\"" + user.getImageThumb() + "\"," + "\"lastname\":\"" + user.getLastName() + "\"," + "\"user_id\":\"" + user.getId() + "\"" + "}" + "},"
				+ "\"to\" : \"" + sessionId + "\"," + "\"type\" : \"" + type + "\"" + "}," + "\"name\" : " + "\"" + action + "\"" + "}";
		return new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id), "", json).toString();
	}

	private String joinRoomMessage(String roomId, String userId, String firstName, String image, String imageThumb, String lastName, String action, int id) {
		String json = "{" + "\"args\":{" + "\"room_id\":\"" + roomId + "\"," + "\"user\":{" + "\"firstname\":\"" + firstName + "\"," + "\"image\":\"" + image + "\","
				+ "\"image_thumb\":\"" + imageThumb + "\"," + "\"lastname\":\"" + lastName + "\"," + "\"user_id\":\"" + userId + "\"" + "}" + "}," + "\"name\" : " + "\"" + action
				+ "\"" + "}";
		return new SocketParser(SocketParser.TYPE_EVENT, (id + "+"), "", json).toString();
	}

	private String checkIsRoomAvailableMessage(String userId) {
		String json = "{\"args\" : \"" + userId + "\", \"name\" : \"room\"}";
		return new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id + "+"), "", json).toString();
	}

	private String createLeaveMessage() {
		String json = "{\"name\" : \"leave\"}";
		return new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id), "", json).toString();
	}

}
