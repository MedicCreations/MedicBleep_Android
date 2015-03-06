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

import java.util.HashMap;
import java.util.Map;

import org.webrtc.StatsReport;
import org.webrtc.VideoRendererGui.ScalingType;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;

/**
 * Fragment for call control.
 */
public class CallFragment extends Fragment {
	private View controlView;
	private TextView encoderStatView;
	private TextView roomIdView;
	private ImageButton toggleDebugButton;
	private OnCallEvents callEvents;
	private ScalingType scalingType;
	private boolean displayHud;
	private volatile boolean isRunning;
	private TextView hudView;
	
	private TextView tvName;
	private Chronometer chCallTime;
	private Button cameraSwitchButton;
	private TextView muteAudio;
	private TextView videoOnOff;
	private TextView speaker;
	private TextView messages;
	private TextView callDecline;
	private Button videoScalingButton;

	/**
	 * Call control interface for container activity.
	 */
	public interface OnCallEvents {
		public void onCallHangUp();

		public void onCameraSwitch();

		public void onVideoScalingSwitch(ScalingType scalingType);
		
		public void onMuteAudio(boolean toMute);
		
		public void onVideoOnOff(boolean toOff);
		
		public void onSpeakerOnOff(boolean toOff);
		
		public void onMessages();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		controlView = inflater.inflate(R.layout.webrtc_fragment_call, container, false);

		// Create UI controls.
		encoderStatView = (TextView) controlView.findViewById(R.id.encoder_stat_call);
		roomIdView = (TextView) controlView.findViewById(R.id.contact_name_call);
		hudView = (TextView) controlView.findViewById(R.id.hud_stat_call);
		cameraSwitchButton = (Button) controlView.findViewById(R.id.switchCameraButton);
		videoScalingButton = (Button) controlView.findViewById(R.id.switchScaleType);
		videoScalingButton.setActivated(true);
		toggleDebugButton = (ImageButton) controlView.findViewById(R.id.button_toggle_debug); 
		
		tvName = (TextView) controlView.findViewById(R.id.userName);
		chCallTime = (Chronometer) controlView.findViewById(R.id.chronoCallTime);
		chCallTime.stop();
		chCallTime.setBase(SystemClock.elapsedRealtime());
		muteAudio = (TextView) controlView.findViewById(R.id.audioMute);
		videoOnOff = (TextView) controlView.findViewById(R.id.videoOnOff);
		speaker = (TextView) controlView.findViewById(R.id.audioSpeaker);
		messages = (TextView) controlView.findViewById(R.id.messages);
		callDecline = (TextView) controlView.findViewById(R.id.callDeclineFragment);

		// Add buttons click events.
		callDecline.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				callEvents.onCallHangUp();
			}
		});

		cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				callEvents.onCameraSwitch();
			}
		});

		videoScalingButton.setOnClickListener(new View.OnClickListener() {
			@Override 
			public void onClick(View view) {
				if (scalingType == ScalingType.SCALE_ASPECT_FILL) {
					videoScalingButton.setActivated(false);
					scalingType = ScalingType.SCALE_ASPECT_FIT;
				} else {
					videoScalingButton.setActivated(true);
					scalingType = ScalingType.SCALE_ASPECT_FILL;
				}
				callEvents.onVideoScalingSwitch(scalingType);
			}
		});
		scalingType = ScalingType.SCALE_ASPECT_FILL;

		toggleDebugButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (displayHud) {
					int visibility = (hudView.getVisibility() == View.VISIBLE) ? View.INVISIBLE : View.VISIBLE;
					hudView.setVisibility(visibility);
				}
			}
		});
		
		muteAudio.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				callEvents.onMuteAudio(v.isActivated());
				v.setActivated(!v.isActivated());
			}
		});
		
		videoOnOff.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				callEvents.onVideoOnOff(v.isActivated());
				v.setActivated(!v.isActivated());
			}
		});
		
		speaker.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				callEvents.onSpeakerOnOff(v.isActivated());
				v.setActivated(!v.isActivated());
			}
		});
		
		messages.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				callEvents.onMessages();
			}
		});

		return controlView;
	}

	@Override
	public void onStart() {
		super.onStart();

		Bundle args = getArguments();
		if (args != null) {
			String roomId = args.getString(CallActivity.EXTRA_ROOMID);
			roomIdView.setText(roomId);
			displayHud = args.getBoolean(CallActivity.EXTRA_DISPLAY_HUD, false);
		}
		displayHud = false;
		int visibility = displayHud ? View.VISIBLE : View.INVISIBLE;
		encoderStatView.setVisibility(visibility);
		toggleDebugButton.setVisibility(visibility);
		hudView.setVisibility(View.INVISIBLE);
		hudView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
		isRunning = true;
		
	}
	
	public void setUserNameAndStarChrono(String name){
		tvName.setText(name);
		chCallTime.start();
	}
	
	public void setAudioMuteButton(boolean toActivate){
		muteAudio.setActivated(true);
	}
	 
	public void setVideoOnOffButton(boolean toActivate){
		videoOnOff.setActivated(toActivate);
	}
	
	public void setSpeakerButton(boolean toActivate){
		speaker.setActivated(toActivate);
	}
	
	@Override
	public void onStop() {
		isRunning = false;
		super.onStop();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callEvents = (OnCallEvents) activity;
	}

	private Map<String, String> getReportMap(StatsReport report) {
		Map<String, String> reportMap = new HashMap<String, String>();
		for (StatsReport.Value value : report.values) {
			reportMap.put(value.name, value.value);
		}
		return reportMap;
	}

	public void updateEncoderStatistics(final StatsReport[] reports) {
		if (!isRunning || !displayHud) {
			return;
		}
		String fps = null;
		String targetBitrate = null;
		String actualBitrate = null;
		StringBuilder bweBuilder = new StringBuilder();
		for (StatsReport report : reports) {
			if (report.type.equals("ssrc") && report.id.contains("ssrc") && report.id.contains("send")) {
				Map<String, String> reportMap = getReportMap(report);
				String trackId = reportMap.get("googTrackId");
				if (trackId != null && trackId.contains(PeerConnectionClient.VIDEO_TRACK_ID)) {
					fps = reportMap.get("googFrameRateSent");
				}
			} else if (report.id.equals("bweforvideo")) {
				Map<String, String> reportMap = getReportMap(report);
				targetBitrate = reportMap.get("googTargetEncBitrate");
				actualBitrate = reportMap.get("googActualEncBitrate");

				for (StatsReport.Value value : report.values) {
					String name = value.name.replace("goog", "").replace("Available", "").replace("Bandwidth", "").replace("Bitrate", "").replace("Enc", "");
					bweBuilder.append(name).append("=").append(value.value).append(" ");
				}
				bweBuilder.append("\n");
			}
		}

		StringBuilder stat = new StringBuilder(128);
		if (fps != null) {
			stat.append("Fps:  ").append(fps).append("\n");
		}
		if (targetBitrate != null) {
			stat.append("Target BR: ").append(targetBitrate).append("\n");
		}
		if (actualBitrate != null) {
			stat.append("Actual BR: ").append(actualBitrate).append("\n");
		}
		encoderStatView.setText(stat.toString());
		hudView.setText(bweBuilder.toString() + hudView.getText());
	}
}
