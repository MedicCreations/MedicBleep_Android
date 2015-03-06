/*
 * libjingle
 * Copyright 2015 Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.zzz.my.webrtc;

import org.apache.http.conn.routing.RouteInfo.LayerType;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoRendererGui.ScalingType;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.RoundImageView;
import com.google.gson.Gson;
import com.zzz.my.webrtc.AppRTCAudioManager.AudioDevice;
import com.zzz.my.webrtc.AppRTCClient.SignalingParameters;
import com.zzz.my.webrtc.PeerConnectionClient.PeerConnectionParameters;
import com.zzz.my.webrtc.WebSocketChannelClient.WebSocketChannelEvents;
import com.zzz.my.webrtc.WebSocketChannelClient.WebSocketConnectionState;
import com.zzz.socket.models.CallMessage;
import com.zzz.socket.models.WebRtcSDPCandidate;
import com.zzz.socket.models.WebRtcSDPMessage;


/**
 * Activity for peer connection call setup, call waiting and call view.
 */
public class CallActivity extends BaseActivity implements AppRTCClient.SignalingEvents, PeerConnectionClient.PeerConnectionEvents, CallFragment.OnCallEvents {

	public static final String EXTRA_ROOMID = "org.appspot.apprtc.ROOMID";
	public static final String EXTRA_LOOPBACK = "org.appspot.apprtc.LOOPBACK";
	public static final String EXTRA_HWCODEC = "org.appspot.apprtc.HWCODEC";
	public static final String EXTRA_VIDEO_BITRATE = "org.appspot.apprtc.VIDEO_BITRATE";
	public static final String EXTRA_VIDEO_WIDTH = "org.appspot.apprtc.VIDEO_WIDTH";
	public static final String EXTRA_VIDEO_HEIGHT = "org.appspot.apprtc.VIDEO_HEIGHT";
	public static final String EXTRA_VIDEO_FPS = "org.appspot.apprtc.VIDEO_FPS";
	public static final String EXTRA_VIDEOCODEC = "org.appspot.apprtc.VIDEOCODEC";
	public static final String EXTRA_CPUOVERUSE_DETECTION = "org.appspot.apprtc.CPUOVERUSE_DETECTION";
	public static final String EXTRA_DISPLAY_HUD = "org.appspot.apprtc.DISPLAY_HUD";
	public static final String EXTRA_CMDLINE = "org.appspot.apprtc.CMDLINE";
	public static final String EXTRA_RUNTIME = "org.appspot.apprtc.RUNTIME";
	private static final String TAG = "CallRTCClient";
	// Peer connection statistics callback period in ms.
	private static final int STAT_CALLBACK_PERIOD = 1000;
	// Local preview screen position before call is connected.
	private static final int LOCAL_WIDTH_CONNECTING = 100;
	private static final int LOCAL_HEIGHT_CONNECTING = 100;
	private static final int LOCAL_X_CONNECTING = 0;
	private static final int LOCAL_Y_CONNECTING = 0;
	// Local preview screen position after call is connected.
	private static final int LOCAL_WIDTH_CONNECTED = 30;
	private static final int LOCAL_HEIGHT_CONNECTED = 25;
	// Remote video screen position
	private static final int REMOTE_X = 0;
	private static final int REMOTE_Y = 0;
	private static final int REMOTE_WIDTH = 100;
	private static final int REMOTE_HEIGHT = 100;

	private PeerConnectionClient peerConnectionClient = null;
	private AppRTCClient appRtcClient;
	private SignalingParameters signalingParameters;
	private AppRTCAudioManager audioManager = null;
	private VideoRenderer.Callbacks localRender;
	private VideoRenderer.Callbacks remoteRender;
	private ScalingType scalingType;
	private Toast logToast;
	private boolean commandLineRun;
	private int runTimeMs;
	private boolean activityRunning;
	private PeerConnectionParameters peerConnectionParameters;
	private boolean hwCodecAcceleration;
	private String videoCodec;
	private boolean iceConnected;
	private boolean isError;
	private boolean callControlFragmentVisible = true;

	// Controls
	private GLSurfaceView videoView;
	CallFragment callFragment;
	
	
	private WebSocketChannelClient wsClient;
	
	private int localXConnected = 0;
	private int localYConnected = 0;
	
	private User activeUser = null;
	private boolean isMyCameraOn = false;
	private boolean isRemoteCameraOn = true;
	private boolean isServiceAllreadyConnect = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

		// Set window styles for fullscreen-window size. Needs to be done before
		// adding content.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		setContentView(R.layout.webrtc_activity_call);

		iceConnected = false;
		signalingParameters = null;
		scalingType = ScalingType.SCALE_ASPECT_FILL;
		
		isMyCameraOn = getIntent().getBooleanExtra(Const.IS_VIDEO_ACCEPT, false);

		// Create UI controls.
		videoView = (GLSurfaceView) findViewById(R.id.glview_call); 
		callFragment = new CallFragment();

		// Create video renderers.
		VideoRendererGui.setView(videoView, new Runnable() {
			@Override
			public void run() {
				createPeerConnectionFactory();
			}
		});
		
		localXConnected = 100 - LOCAL_WIDTH_CONNECTED - 5;
		localYConnected = 2;
		
		remoteRender = VideoRendererGui.create(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
		localRender = VideoRendererGui.create(LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);

		// Show/hide call control fragment on view click.
		videoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toggleCallControlFragmentVisibility();
			}
		});

		// Get Intent parameters.
		final Intent intent = getIntent();
		
		hwCodecAcceleration = intent.getBooleanExtra(EXTRA_HWCODEC, true);
		if (intent.hasExtra(EXTRA_VIDEOCODEC)) {
			videoCodec = intent.getStringExtra(EXTRA_VIDEOCODEC);
		} else {
			videoCodec = PeerConnectionClient.VIDEO_CODEC_VP8; // use VP8 by
																// default.
		}
		peerConnectionParameters = new PeerConnectionParameters(intent.getIntExtra(EXTRA_VIDEO_WIDTH, 0), intent.getIntExtra(EXTRA_VIDEO_HEIGHT, 0), intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
				intent.getIntExtra(EXTRA_VIDEO_BITRATE, 0), intent.getBooleanExtra(EXTRA_CPUOVERUSE_DETECTION, true));
		
		LooperExecutor executor = new LooperExecutor();
		
		// Create connection client and connection parameters.
		wsClient = new WebSocketChannelClient(executor, new WebSocketChannelEvents() {
			
			@Override
			public void onWebSocketOpen() {
				Log.d(TAG, "Websocket connection completed. Registering..."); 
				wsClient.register();
				acceptCall();  
			}
			
			@Override
			public void onWebSocketMessage(String message) {
				if (wsClient.getState() != WebSocketConnectionState.REGISTERED) { 
					Log.e(TAG, "Got WebSocket message in non registered state.");
					return;
				} 
				try { 
					WebRtcSDPMessage item = new Gson().fromJson(message, WebRtcSDPMessage.class);
					if (item.getArgs().get(0).getType().equals("answer")){ 
						SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("ANSWER"), item.getArgs().get(0).getPayload().getSdp());
						onRemoteDescription(sdp);
					}else if (item.getArgs().get(0).getType().equals("offer")){
						SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("OFFER"), item.getArgs().get(0).getPayload().getSdp());
						onRemoteDescription(sdp);
					}else if (item.getArgs().get(0).getType().equals("candidate")){
						WebRtcSDPCandidate candidateModel = item.getArgs().get(0).getPayload().getCandidate();
						IceCandidate candidate = new IceCandidate(candidateModel.getSdmMid(), Integer.valueOf(candidateModel.getSdpMLineIndex()), candidateModel.getCandidate());
						onRemoteIceCandidate(candidate);
					}else if (item.getArgs().get(0).getType().equals("bye")) {
						onChannelClose();
					}
				} catch (Exception e) {
					Log.e("LOG", e.toString()); 
				}
			}
			
			@Override
			public void onWebSocketError(String description) {
				onChannelClose();
			}
			
			@Override
			public void onWebSocketClose() {
				reportError("WebSocket error: ");
			}
		}, this);
		
		WebRtcSDPMessage item = (WebRtcSDPMessage) getIntent().getSerializableExtra(Const.CANDIDATE);
		appRtcClient = new WebSocketRTCClient(this, executor, this, item, wsClient);

		// Send intent arguments to fragment.
		callFragment.setArguments(intent.getExtras());
		// Activate call fragment and start the call.
		getFragmentManager().beginTransaction().add(R.id.call_fragment_container, callFragment).commit();

		// For command line execution run connection for <runTimeMs> and exit.
		if (commandLineRun && runTimeMs > 0) {
			videoView.postDelayed(new Runnable() {
				public void run() {
					disconnect();
				}
			}, runTimeMs);
		}
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(intent.hasExtra(Const.TYPE_OF_SOCKET_RECEIVER)){
			if(intent.getIntExtra(Const.TYPE_OF_SOCKET_RECEIVER, -1) == Const.CALL_ENDED){
				disconnect();
			}
		}
	}
	
	private void reportError(final String errorMessage) {
		Log.e(TAG, errorMessage);
		onChannelError(errorMessage);
	}
	
	@Override
    protected void onStart() {
    	super.onStart();
    	intentFilterSocketCall = new IntentFilter("CALL");
		LocalBroadcastManager.getInstance(this).registerReceiver(rec, intentFilterSocketCall);
    };
    
    @Override
	protected void onStop() {
//		LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
		super.onStop();
    }
    
    IntentFilter intentFilterSocketCall;
	BroadcastReceiver rec = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("NEW", "RECEIVER");
			if(intent.hasExtra(Const.MESSAGES)){
				Log.d("NEW", "RECEIVER MESSAGE");
				//MUTE UNMUTE
				CallMessage mess = (CallMessage) intent.getSerializableExtra(Const.MESSAGES);
				if(mess.getArgs().get(0).getPayload().getName().equals("video")){
					Log.d("NEW", "VIDEO");
					manageRemoteVideo(mess.getArgs().get(0).getType());
				}
				return;
			}else if(intent.hasExtra(Const.TYPE_OF_SOCKET_RECEIVER)){
				if(intent.getIntExtra(Const.TYPE_OF_SOCKET_RECEIVER, -1) == Const.CALL_ENDED){
					disconnect();
					return;
				}
			}
			WebRtcSDPMessage item = (WebRtcSDPMessage) intent.getSerializableExtra(Const.CANDIDATE);
			activeUser = item.getArgs().get(0).getPayload().getUser();
			startCall(item);
			
		}
	};

	@Override
	protected void onServiceBaseConnected() {
		if(isServiceAllreadyConnect) return;
		isServiceAllreadyConnect = true;
		wsClient.connect();
	};
	
	public void acceptCall(){
		if(getIntent().hasExtra(Const.SESSION_ID)) {
			//RECEIVE CALL
			mService.callAccept(getIntent().getStringExtra(Const.SESSION_ID));
		}else{
			//MAKE CALL
			User user = (User) getIntent().getSerializableExtra(Const.USER);
			activeUser = user;
			startCall(null);
		}
	}
	
	private void manageRemoteVideo(String type) {
		Log.d("NEW", "MANAGE REMOTE VIDEO: "+type);
		if(type.equals("mute")){
			isRemoteCameraOn = false;
			for(int i = 1; i < 5; i++){
				int id = getResources().getIdentifier("backBlue" + i, "id", getPackageName());
				findViewById(id).setVisibility(View.VISIBLE);
			}
//			callFragment.showBlueScreen(); //TODO
			manageLocalVideo();
		}else{
			isRemoteCameraOn = true;
			for(int i = 1; i < 5; i++){
				int id = getResources().getIdentifier("backBlue" + i, "id", getPackageName());
				findViewById(id).setVisibility(View.INVISIBLE);
			}
			manageLocalVideo();
//			callFragment.hideBlueScreen(); //TODO
		}
	}
	
	private void manageLocalVideo() {
		if(!isMyCameraOn){
			if(!isRemoteCameraOn) {
				findViewById(R.id.backgroundInMyCamera).setVisibility(View.VISIBLE);
				findViewById(R.id.imageInCall).setVisibility(View.VISIBLE);
			}
			else {
				findViewById(R.id.backgroundInMyCamera).setVisibility(View.INVISIBLE);
				findViewById(R.id.imageInCall).setVisibility(View.GONE);
			}
			VideoRendererGui.update(localRender, 99, 99, 1, 1, ScalingType.SCALE_ASPECT_FILL);
//			VideoRendererGui.remove(localRender);
		}else{
			findViewById(R.id.backgroundInMyCamera).setVisibility(View.INVISIBLE);
			findViewById(R.id.imageInCall).setVisibility(View.GONE);
			VideoRendererGui.update(localRender, localXConnected, localYConnected, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED, ScalingType.SCALE_ASPECT_FILL);
		}
	}
	
	@Override
	protected void callEnded() {
		disconnect();
	}
	

	// Activity interfaces
	@Override
	public void onPause() {
		super.onPause();
		videoView.onPause();
		activityRunning = false;
		if (peerConnectionClient != null) {
			peerConnectionClient.stopVideoSource();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		videoView.onResume();
		activityRunning = true;
		if (peerConnectionClient != null) {
			peerConnectionClient.startVideoSource();
		}
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
		disconnect();
		super.onDestroy();
		if (logToast != null) {
			logToast.cancel();
		}
		activityRunning = false;
	}

	// CallFragment.OnCallEvents interface implementation.
	@Override
	public void onCallHangUp() {
		disconnect();
	}

	@Override
	public void onCameraSwitch() {
		if (peerConnectionClient != null) {
			peerConnectionClient.switchCamera();
		}
	}

	@Override
	public void onVideoScalingSwitch(ScalingType scalingType) {
		this.scalingType = scalingType;
		updateVideoView();
		manageLocalVideo();
	}
	
	@Override
	public void onMuteAudio(boolean toMute) {
		String mute = "unmute";
		if(toMute) mute = "mute";
		mService.sendWebRtcUnMuteOrMute("audio", mute);
		peerConnectionClient.setAudioEnabled(!toMute);
	}
	
	@Override
	public void onVideoOnOff(boolean toOff) {
		String mute = "unmute";
		if(toOff) mute = "mute";
		mService.sendWebRtcUnMuteOrMute("video", mute);
		peerConnectionClient.setLocalVideoEnabled(!toOff);
		isMyCameraOn = !toOff;
		manageLocalVideo();
	}
	
	@Override
	public void onSpeakerOnOff(boolean toOff) {
		if(toOff){
			audioManager.setForceEarpiece(true);
			audioManager.setAudioDevice(AudioDevice.EARPIECE);
		}else{
			audioManager.setForceEarpiece(false);
			audioManager.setAudioDevice(AudioDevice.SPEAKER_PHONE);
		}
	}
	
	@Override
	public void onMessages() {
		Log.d("NEW", "GO TO MESSAGES"); //TODO
	}

	// Helper functions.
	private void toggleCallControlFragmentVisibility() {
		if (!iceConnected || !callFragment.isAdded()) {
			return;
		}
		// Show/hide call control fragment
		callControlFragmentVisible = !callControlFragmentVisible;
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if (callControlFragmentVisible) {
			ft.show(callFragment);
		} else {
			ft.hide(callFragment);
		}
		ft.commit();
	}

	private void updateVideoView() {
		VideoRendererGui.update(remoteRender, REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT, scalingType); 
		if (iceConnected) {
			VideoRendererGui.update(localRender, localXConnected, localYConnected, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED, ScalingType.SCALE_ASPECT_FILL);
		} else {
			VideoRendererGui.update(localRender, LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType);
		}
	}

	private void startCall(WebRtcSDPMessage webRtcMessage) {
		if (appRtcClient == null) {
			Log.e(TAG, "AppRTC client is not allocated for a call."); 
			return;
		}
		// Start room connection. 
		signalingParameters = appRtcClient.setRoomParameters(webRtcMessage);
		
		onConnectedToRoom(signalingParameters);

		// Create and audio manager that will take care of audio routing,
		// audio modes, audio device enumeration etc.
		audioManager = AppRTCAudioManager.create(this, new Runnable() {
			// This method will be called each time the audio state (number and
			// type of devices) has been changed.
			@Override
			public void run() {
				onAudioManagerChangedState();
			}
		});
		// Store existing audio settings and change audio mode to
		// MODE_IN_COMMUNICATION for best possible VoIP performance.
		Log.d(TAG, "Initializing the audio manager...");
		audioManager.init(); 
		
		RoundImageView profile = (RoundImageView) findViewById(R.id.imageInCall);
		profile.setBorderColor(Color.WHITE);
		if(activeUser != null)ImageLoader.getInstance(this).displayImage(this, activeUser.getImageThumb(), profile);
	}

	// Should be called from UI thread
	private void callConnected() {
		// Update video view. 
		if(!isMyCameraOn) {
			peerConnectionClient.setLocalVideoEnabled(false);
			mService.sendWebRtcUnMuteOrMute("video", "mute");
		}else{
//			callFragment.hideBlueScreen(); //TODO
			findViewById(R.id.backgroundInMyCamera).setVisibility(View.INVISIBLE);
			findViewById(R.id.imageInCall).setVisibility(View.GONE);
		}
		updateVideoView();
		
		// Enable statistics callback. 
		peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
		
		if(activeUser != null && callFragment != null) callFragment.setUserNameAndStarChrono(activeUser.getFirstName() + " " + activeUser.getLastName());
		callFragment.setAudioMuteButton(true);
		callFragment.setVideoOnOffButton(isMyCameraOn);
		callFragment.setSpeakerButton(true);
		
	}

	private void onAudioManagerChangedState() {
		//  disable video if
		// AppRTCAudioManager.AudioDevice.EARPIECE
		// is active.
	} 

	// Create peer connection factory when EGL context is ready.
	private void createPeerConnectionFactory() { 
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (peerConnectionClient == null) {
					peerConnectionClient = new PeerConnectionClient();
					peerConnectionClient.createPeerConnectionFactory(CallActivity.this, videoCodec, hwCodecAcceleration, VideoRendererGui.getEGLContext(), CallActivity.this);
				}
				if (signalingParameters != null) {
					Log.w(TAG, "EGL context is ready after room connection.");
					onConnectedToRoomInternal(signalingParameters);
				}
			}
		});
	}
 
	// Disconnect from remote resources, dispose of local resources, and exit.
	private void disconnect() {
		mService.setIsInWebRtc(false);
		if (appRtcClient != null) {
			appRtcClient.disconnect();
			appRtcClient = null;
		}
		if (peerConnectionClient != null) {
			peerConnectionClient.close();
			peerConnectionClient = null;
		}
		if (audioManager != null) {
			audioManager.close();
			audioManager = null;
		}
		if (iceConnected && !isError) {
			setResult(RESULT_OK);
		} else {
			setResult(RESULT_CANCELED);
		}
		finish();
	}

	private void disconnectWithErrorMessage(final String errorMessage) {
		if (commandLineRun || !activityRunning) {
			Log.e(TAG, "Critical error: " + errorMessage);
			disconnect();
		} else {
			new AlertDialog.Builder(this).setTitle(getText(R.string.channel_error_title)).setMessage(errorMessage).setCancelable(false)
					.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							disconnect();
						}
					}).create().show();
		}
	}

	// Log |msg| and Toast about it.
	private void logAndToast(String msg) {
		Log.d(TAG, msg);
		if (logToast != null) {
			logToast.cancel();
		}
		logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		logToast.show();
	}

	// -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
	// All callbacks are invoked from websocket signaling looper thread and
	// are routed to UI thread.
	private void onConnectedToRoomInternal(final SignalingParameters params) {
		signalingParameters = params;
		if (peerConnectionClient == null) {
			Log.w(TAG, "Room is connected, but EGL context is not ready yet.");
			return;
		}
		logAndToast("Creating peer connection...");
		peerConnectionClient.createPeerConnection(localRender, remoteRender, signalingParameters, peerConnectionParameters);

		if (signalingParameters.initiator) {
			logAndToast("Creating OFFER...");
			// Create offer. Offer SDP will be sent to answering client in
			// PeerConnectionEvents.onLocalDescription event.
			peerConnectionClient.createOffer();
		} else {
			if (params.offerSdp != null) {
				peerConnectionClient.setRemoteDescription(params.offerSdp);
				logAndToast("Creating ANSWER...");
				// Create answer. Answer SDP will be sent to offering client in
				// PeerConnectionEvents.onLocalDescription event.
				peerConnectionClient.createAnswer();
			}
			if (params.iceCandidates != null) {
				// Add remote ICE candidates from room.
				for (IceCandidate iceCandidate : params.iceCandidates) {
					peerConnectionClient.addRemoteIceCandidate(iceCandidate);
				}
			}
		}
	}

	@Override
	public void onConnectedToRoom(final SignalingParameters params) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onConnectedToRoomInternal(params);
			}
		});
	}

	@Override
	public void onRemoteDescription(final SessionDescription sdp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (peerConnectionClient == null) {
					Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
					return;
				}
				logAndToast("Received remote " + sdp.type + " ...");
				peerConnectionClient.setRemoteDescription(sdp);
				if (!signalingParameters.initiator) {
					logAndToast("Creating ANSWER...");
					// Create answer. Answer SDP will be sent to offering client
					// in
					// PeerConnectionEvents.onLocalDescription event.
//					Log.d("NEW", "REMOTE DESCRIPTION CREATE ANSWER");
//					peerConnectionClient.createAnswer();
				}
			}
		});
	}

	@Override
	public void onRemoteIceCandidate(final IceCandidate candidate) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (peerConnectionClient == null) {
					Log.e(TAG, "Received ICE candidate for non-initilized peer connection.");
					return;
				}
				peerConnectionClient.addRemoteIceCandidate(candidate);
			}
		});
	}

	@Override
	public void onChannelClose() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				logAndToast("Remote end hung up; dropping PeerConnection");
				disconnect();
			}
		});
	}

	@Override
	public void onChannelError(final String description) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isError) {
					isError = true;
					disconnectWithErrorMessage(description);
				}
			}
		});
	}

	// -----Implementation of
	// PeerConnectionClient.PeerConnectionEvents.---------
	// Send local peer connection SDP and ICE candidates to remote party.
	// All callbacks are invoked from peer connection client looper thread and
	// are routed to UI thread.
	@Override
	public void onLocalDescription(final SessionDescription sdp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (appRtcClient != null) {
					logAndToast("Sending " + sdp.type + " ...");
					if (signalingParameters.initiator) {
						appRtcClient.sendOfferSdp(sdp);
					} else {
						appRtcClient.sendAnswerSdp(sdp);
					}
				}
			}
		});
	}

	@Override
	public void onIceCandidate(final IceCandidate candidate) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (appRtcClient != null) {
					appRtcClient.sendLocalIceCandidate(candidate);
				}
			}
		});
	}

	@Override
	public void onIceConnected() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				logAndToast("ICE connected");
				iceConnected = true;
				callConnected();
			}
		});
	}

	@Override
	public void onIceDisconnected() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				logAndToast("ICE disconnected");
				iceConnected = false;
				disconnect();
			}
		});
	}

	@Override
	public void onPeerConnectionClosed() {
	}

	@Override
	public void onPeerConnectionStatsReady(final StatsReport[] reports) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isError && iceConnected) {
					callFragment.updateEncoderStatistics(reports);
				}
			}
		});
	}

	@Override
	public void onPeerConnectionError(final String description) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isError) {
					isError = true;
					disconnectWithErrorMessage(description);
				}
			}
		});
	}
}
