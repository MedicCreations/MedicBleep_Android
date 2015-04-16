package com.clover.spika.enterprise.chat.webrtc.socket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.models.PreLogin;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
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

public class SocketService extends Service {

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
				user = Helper.getUser();
				
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

		new SocketClient().fakeApiForSSL(this, new ApiCallback<PreLogin>() {
			
			@Override
			public void onApiResponse(Result<PreLogin> result) {
				new SocketClient().getSessionId(true, new ApiCallback<String>() {

					@Override
					public void onApiResponse(Result<String> result) {
						sessionId = result.getResultData();
						if (sessionId == null) {
							return;
						}
						sessionId = sessionId.substring(0, sessionId.indexOf(":"));
						Logger.custom("d", "LOG", "Socket SessionId: " + sessionId);
						user = Helper.getUser();
						
						if (user.getId() == -1) {
							SocketService.this.stopSelf();
						} else {
							work(sessionId);
						}
					}

				});
			}
		});
		/* end:FakeApi */
		
	}

	private void work(String sessionId) {

		wsString = Const.WS_URL + ":" + Const.WS_PORT + Const.WS_SUFIX_URL + sessionId;
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

				if (item.getArgs().get(0).getType().equals(Const.WebRTCCall.CALL_END) || item.getArgs().get(0).getType().equals(Const.WebRTCCall.CALL_DECLINE)) {
					joinMyRoom();
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_ENDED);
				} else if (item.getArgs().get(0).getType().equals(Const.WebRTCCall.CALL_ANSWER)) {
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_ANSWER);
					inBroadcast.putExtra(Const.USER, item.getArgs().get(0).getPayload().getUser());
					activeUserInteractive = item.getArgs().get(0).getPayload().getUser();
					activeSessionOfInteractiveUser = item.getArgs().get(0).getFrom();
					inBroadcast.putExtra(Const.SESSION_ID, activeSessionOfInteractiveUser);
				} else if (item.getArgs().get(0).getType().equals(Const.WebRTCCall.CALL_OFFER)) {
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_RECEIVE);
					inBroadcast.putExtra(Const.USER, item.getArgs().get(0).getPayload().getUser());
					activeSessionOfInteractiveUser = item.getArgs().get(0).getFrom();
					activeUserInteractive = item.getArgs().get(0).getPayload().getUser();
					inBroadcast.putExtra(Const.SESSION_ID, activeSessionOfInteractiveUser);
				} else if (item.getArgs().get(0).getType().equals(Const.WebRTCCall.CALL_RINGING)) {
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_RINGING);
				} else if (item.getArgs().get(0).getType().equals(Const.WebRTCCall.CALL_CANCEL)) {
					inBroadcast.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_CANCELED);
				} else if (item.getArgs().get(0).getType().equals(Const.WebRTCCall.CALL_MUTE) 
						|| item.getArgs().get(0).getType().equals(Const.WebRTCCall.CALL_UNMUTE)  
						|| item.getArgs().get(0).getType().equals(Const.WebRTCCall.CALL_MUTE_REMOTE_VIDEO)) {
					inBroadcast.setAction(Const.CALL_ACTION);
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
					inBroadcast.setAction(Const.CALL_ACTION);
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

			if (item.getArgs().get(0).getType().equals(Const.WebRTCCall.CALL_OFFER)) {
				activeSessionOfInteractiveUser = item.getArgs().get(0).getFrom();
				activeUserInteractive = item.getArgs().get(0).getPayload().getUser();
				User user = item.getArgs().get(0).getPayload().getUser();
				Intent intentInAp = new Intent(this, MainActivity.class);
				intentInAp.putExtra(Const.TYPE_OF_SOCKET_RECEIVER, Const.CALL_RECEIVE);
				intentInAp.putExtra(Const.USER, user);
				intentInAp.putExtra(Const.SESSION_ID, activeSessionOfInteractiveUser);
				intentInAp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentInAp);
			} else if (item.getArgs().get(0).getType().equals(Const.WebRTCCall.CALL_END)) {
				joinMyRoom();
				Intent inBroadcast = new Intent();
				inBroadcast.setAction(Const.CALL_ACTION);
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

	public String createMuteMessage(String videoOrAudio, String mute) {
		String json = SocketMessageHelper.createMuteMessage(videoOrAudio, mute, activeSessionOfInteractiveUser, sessionId, user);
		return json;
	}

	public String createWebRtcMessage(String mess) {
		String json = SocketMessageHelper.createWebRtcMessage(mess, activeSessionOfInteractiveUser, user);
		return json;
	}

	public void sendWebRtcMessageOffer(String sdp) {
		String json = SocketMessageHelper.sendWebRtcMessageOffer(sdp, activeSessionOfInteractiveUser, user);
		mConn.sendTextMessage(new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id), "", json).toString());
		id++;
	}

	public void sendWebRtcMessageOfferForAnswer(String sdp) {
		String json = SocketMessageHelper.sendWebRtcMessageOfferForAnswer(sdp, activeSessionOfInteractiveUser, sessionId, user);
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
		mConn.sendTextMessage(callMessage(sessionId, Const.WebRTCCall.CALL_OFFER, id));
		Logger.custom("d", "LOG", "CALL OFFER");
		id++;
	}

	public void callCancel(String sessionId) {
		action = Const.ACTION_CALL_CANCEL;
		if (sessionId == null)
			sessionId = activeSessionOfInteractiveUser;
		mConn.sendTextMessage(callMessage(sessionId, Const.WebRTCCall.CALL_CANCEL, id));
		activeSessionOfInteractiveUser = "-1";
		activeUserInteractive = null;
		Logger.custom("d", "LOG", "CALL CANCEL");
		id++;
	}

	public void callDecline(String sessionId) {
		action = Const.ACTION_CALL_DECLINE;
		if (sessionId == null)
			sessionId = activeSessionOfInteractiveUser;
		mConn.sendTextMessage(callMessage(sessionId, Const.WebRTCCall.CALL_DECLINE, id));
		Logger.custom("d", "LOG", "CALL DECLINE");
		id++;
	}

	public void callEnd(String sessionId) {
		action = Const.ACTION_CALL_END;
		if (sessionId == null)
			sessionId = activeSessionOfInteractiveUser;
		mConn.sendTextMessage(callMessage(sessionId, Const.WebRTCCall.CALL_END, id));
		Logger.custom("d", "LOG", "CALL END");
		id++;
	}

	public void callAccept(String sessionId) {
		action = Const.ACTION_CALL_ACCEPT;
		mConn.sendTextMessage(callMessage(sessionId, Const.WebRTCCall.CALL_ANSWER, id));
		Logger.custom("d", "LOG", "CALL ACCEPT");
		id++;
	}

	public void callRinging(String sessionId) {
		action = Const.ACTION_CALL_RINGING;
		mConn.sendTextMessage(callMessage(sessionId, Const.WebRTCCall.CALL_RINGING, id));
		Logger.custom("d", "LOG", "CALL RINGING");
		id++;
	}

	public void joinMyRoom() {
		User user = Helper.getUser();
		mConn.sendTextMessage(joinRoomMessage(String.valueOf(user.getId()), String.valueOf(user.getId()), user.getFirstName(), user.getImage(), user.getImageThumb(),
				user.getLastName(), "join", id));
		Logger.custom("d", "LOG", "JOIN MY ROOM");
		id++;
	}

	public void joinOtherUserRoom(String userId) {
		User user = Helper.getUser();
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
		String json = SocketMessageHelper.callMessage(sessionId, type, id);
		return new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id), "", json).toString();
	}

	private String joinRoomMessage(String roomId, String userId, String firstName, String image, String imageThumb, String lastName, String action, int id) {
		String json = SocketMessageHelper.joinRoomMessage(roomId, userId, firstName, image, imageThumb, lastName, action, id);
		return new SocketParser(SocketParser.TYPE_EVENT, (id + "+"), "", json).toString();
	}

	private String checkIsRoomAvailableMessage(String userId) {
		String json = SocketMessageHelper.checkIsRoomAvailableMessage(userId);
		return new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id + "+"), "", json).toString();
		
	}

	private String createLeaveMessage() {
		String json = SocketMessageHelper.createLeaveMessage();
		return new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id), "", json).toString();
	}

}
