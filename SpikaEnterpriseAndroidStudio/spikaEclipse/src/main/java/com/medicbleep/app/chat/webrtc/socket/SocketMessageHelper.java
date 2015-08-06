package com.medicbleep.app.chat.webrtc.socket;

import org.json.JSONException;
import org.json.JSONObject;

import com.medicbleep.app.chat.models.User;
import com.medicbleep.app.chat.utils.Helper;

public class SocketMessageHelper {

	public static String checkIsRoomAvailableMessage(String userId) {
		// try {
		// JSONObject object = new JSONObject();
		// object.put("args", userId);
		// object.put("name", "room");
		// return new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id +
		// "+"), "", object.toString()).toString();
		// } catch (JSONException e) {
		// e.printStackTrace();
		// return "";
		// }
		String json = "{" + "\"args\" : \"" + userId + "\"," + " \"name\" : \"room\"" + "}";
		return json;

	}
	
	public static String createLeaveMessage() {
		String json = "{\"name\" : \"leave\"}";
		return json;
	}
	
	public static String joinRoomMessage(String roomId, String userId, String firstName, String image, String imageThumb, String lastName, String action, int id) {
		
//		try {
//			JSONObject object = new JSONObject();
//			
//			JSONObject args = new JSONObject();
//			args.put("room_id", roomId);
//			
//			JSONObject user = new JSONObject();
//			user.put("firstname", firstName);
//			user.put("image", image);
//			user.put("image_thumb", imageThumb);
//			user.put("lastname", lastName);
//			user.put("user_id", userId);
//			
//			args.put("user", user);
//			
//			object.put("args", args);
//			object.put("name", action);
//			
//			return new SocketParser(SocketParser.TYPE_EVENT, (id + "+"), "", object.toString()).toString();
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return "";
//		}
		
		String json = "{" 
						+ "\"args\":{" 
							+ "\"room_id\":\"" + roomId + "\"," 
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
		return json;
	}
	
	public static String callMessage(String sessionId, String type, int id) {
//		try {
//			User userModel = Helper.getUser(this);
//			String action = "message";
//			
//			JSONObject object = new JSONObject();
//			
//			JSONObject args = new JSONObject();
//			args.put("to", sessionId);
//			args.put("type", type);
//			
//			JSONObject payload = new JSONObject();
//			
//			JSONObject user = new JSONObject();
//			user.put("firstname", userModel.getFirstName());
//			user.put("image", userModel.getImage());
//			user.put("image_thumb", userModel.getImageThumb());
//			user.put("lastname", userModel.getLastName());
//			user.put("user_id", userModel.getId());
//			
//			payload.put("user", user);
//			
//			args.put("payload", payload);
//			
//			object.put("args", args);
//			object.put("name", action);
//			
//			return new SocketParser(SocketParser.TYPE_EVENT, String.valueOf(id), "", object.toString()).toString();
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return "";
//		}
		
		User userModel = Helper.getUser();
		String action = "message";
		String json = "{" 
						+ "\"args\":{" 
							+ "\"payload\":{" 
								+ "\"user\":{" 
									+ "\"firstname\":\"" + userModel.getFirstName() + "\"," 
									+ "\"image\":\"" + userModel.getImage() + "\"," 
									+ "\"image_thumb\":\"" + userModel.getImageThumb() + "\"," 
									+ "\"lastname\":\"" + userModel.getLastName() + "\"," 
									+ "\"user_id\":\"" + userModel.getId() + "\"" 
								+ "}" 
							+ "}," 
							+ "\"to\" : \"" + sessionId + "\","
							+ "\"type\" : \"" + type + "\""
						+ "}," 
						+ "\"name\" : " + "\"" + action + "\"" 
					+ "}";
		return json;
	}
	
	public static String sendWebRtcMessageOfferForAnswer(String sdp, String activeSessionOfInteractiveUser, String sessionId, User user) {
		String formatedSdp = sdp.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\r\\\\n");
		String action = "message";
		
//		try {
//			JSONObject object = new JSONObject();
//			
//			JSONObject args = new JSONObject();
//			args.put("to", activeSessionOfInteractiveUser);
//			args.put("type", "answer");
//			args.put("from", sessionId);
//			args.put("roomType", "video");
//			
//			JSONObject payload = new JSONObject();
//			payload.put("sdp", formatedSdp);
//			payload.put("type", "answer");
//			
//			JSONObject userObj = new JSONObject();
//			userObj.put("firstname", user.getFirstName());
//			userObj.put("image", user.getImage());
//			userObj.put("image_thumb", user.getImageThumb());
//			userObj.put("lastname", user.getLastName());
//			userObj.put("user_id", user.getId());
//			
//			payload.put("user", userObj);
//			
//			args.put("payload", payload);
//			
//			object.put("args", args);
//			object.put("name", action);
//			
//			return object.toString()
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return "";
//		}
		
		String json = "{" 
						+ "\"args\":{" 
							+ "\"type\" : \"answer\"," 
							+ "\"to\" : \"" + activeSessionOfInteractiveUser + "\"," 
							+ "\"from\" : \"" + sessionId + "\"," 
							+ "\"payload\":{" 
								+ "\"sdp\" : \"" + formatedSdp + "\"," 
								+ "\"type\" : \"answer\"," 
								+ "\"user\":{" 
									+ "\"firstname\":\"" + user.getFirstName() + "\"," 
									+ "\"image\":\"" + user.getImage() + "\"," 
									+ "\"image_thumb\":\"" + user.getImageThumb() + "\"," 
									+ "\"lastname\":\"" + user.getLastName() + "\"," 
									+ "\"user_id\":\"" + user.getId() + "\"" 
								+ "}" 
							+ "}," 
							+ "\"roomType\" : \"video\"" 
						+ "}," 
						+ "\"name\" : " + "\"" + action + "\"" 
					+ "}";
		return json;
	}
	
	public static String sendWebRtcMessageOffer(String sdp, String activeSessionOfInteractiveUser, User user) {
		String formatedSdp = sdp.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\r\\\\n");
		String action = "message";
		
//		try {
//			JSONObject object = new JSONObject();
//			
//			JSONObject args = new JSONObject();
//			args.put("to", activeSessionOfInteractiveUser);
//			args.put("type", "offer");
//			args.put("roomType", "video");
//			
//			JSONObject payload = new JSONObject();
//			payload.put("sdp", formatedSdp);
//			payload.put("type", "offer");
//			
//			JSONObject userObj = new JSONObject();
//			userObj.put("firstname", user.getFirstName());
//			userObj.put("image", user.getImage());
//			userObj.put("image_thumb", user.getImageThumb());
//			userObj.put("lastname", user.getLastName());
//			userObj.put("user_id", user.getId());
//			
//			payload.put("user", userObj);
//			
//			args.put("payload", payload);
//			
//			object.put("args", args);
//			object.put("name", action);
//			
//			return object.toString()
//		} catch (JSONException e) {
//		e.printStackTrace();
//		return "";
//	}
		
		String json = "{" 
						+ "\"args\":{" 
							+ "\"type\" : \"offer\"," 
							+ "\"to\" : \"" + activeSessionOfInteractiveUser + "\"," 
							+ "\"payload\":{" 
								+ "\"sdp\" : \"" + formatedSdp + "\"," 
								+ "\"type\" : \"offer\"," 
								+ "\"user\":{" 
									+ "\"firstname\":\"" + user.getFirstName() + "\"," 
									+ "\"image\":\"" + user.getImage() + "\"," 
									+ "\"image_thumb\":\"" + user.getImageThumb() + "\"," 
									+ "\"lastname\":\"" + user.getLastName() + "\"," 
									+ "\"user_id\":\"" + user.getId() + "\"" 
								+ "}"
							+ "}," 
							+ "\"roomType\" : \"video\"" 
						+ "}," 
						+ "\"name\" : " + "\"" + action	+ "\"" 
					+ "}";
		return json;
	}
	
	public static String createWebRtcMessage(String mess, String activeSessionOfInteractiveUser, User user) {
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
		
//		try {
//			JSONObject object = new JSONObject();
//			
//			JSONObject args = new JSONObject();
//			args.put("to", activeSessionOfInteractiveUser);
//			args.put("type", "candidate");
//			args.put("roomType", "video");
//			
//			JSONObject payload = new JSONObject();
//			
//			JSONObject candObj = new JSONObject();
//			candObj.put("sdpMLineIndex", sdpMIndex);
//			candObj.put("sdpMid", sdpMid);
//			candObj.put("candidate", candidate);
//			
//			payload.put("candidate", candObj);
//			
//			JSONObject userObj = new JSONObject();
//			userObj.put("firstname", user.getFirstName());
//			userObj.put("image", user.getImage());
//			userObj.put("image_thumb", user.getImageThumb());
//			userObj.put("lastname", user.getLastName());
//			userObj.put("user_id", user.getId());
//			
//			payload.put("user", userObj);
//			
//			args.put("payload", payload);
//			
//			object.put("args", args);
//			object.put("name", action);
//			
//			return object.toString();
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return "";
//		}

		String json = "{" 
						+ "\"args\":{" 
							+ "\"type\" : \"candidate\"," 
							+ "\"to\" : \"" + activeSessionOfInteractiveUser + "\"," 
							+ "\"payload\":{" 
								+ "\"candidate\":{" 
									+ "\"sdpMLineIndex\":\"" + sdpMIndex + "\"," 
									+ "\"sdpMid\":\"" + sdpMid + "\"," 
									+ "\"candidate\":\"" + candidate + "\"" 
								+ "}," 
								+ "\"user\":{" 
									+ "\"firstname\":\"" + user.getFirstName() + "\"," 
									+ "\"image\":\"" + user.getImage() + "\"," 
									+ "\"image_thumb\":\"" + user.getImageThumb() + "\"," 
									+ "\"lastname\":\"" + user.getLastName() + "\"," 
									+ "\"user_id\":\"" + user.getId() + "\"" 
								+ "}" 
							+ "}," 
							+ "\"roomType\" : \"video\"" 
						+ "}," 
						+ "\"name\" : " + "\"" + action + "\"" 
					+ "}";

		return json;
	}
	
	public static String createMuteMessage(String videoOrAudio, String mute, String activeSessionOfInteractiveUser, String sessionId, User user) {

		String action = "message";
		
//		try {
//			JSONObject object = new JSONObject();
//			
//			JSONObject args = new JSONObject();
//			args.put("to", activeSessionOfInteractiveUser);
//			args.put("from", sessionId);
//			args.put("type", mute);
//			args.put("roomType", "video");
//			
//			JSONObject payload = new JSONObject();
//			payload.put("name", videoOrAudio);
//			
//			JSONObject userObj = new JSONObject();
//			userObj.put("firstname", user.getFirstName());
//			userObj.put("image", user.getImage());
//			userObj.put("image_thumb", user.getImageThumb());
//			userObj.put("lastname", user.getLastName());
//			userObj.put("user_id", user.getId());
//			
//			payload.put("user", userObj);
//			
//			args.put("payload", payload);
//			
//			object.put("args", args);
//			object.put("name", action);
//			
//			return object.toString();
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return "";
//		}
		
		String json = "{" 
						+ "\"args\":{" 
							+ "\"type\" : \"" + mute + "\"," 
							+ "\"to\" : \"" + activeSessionOfInteractiveUser + "\"," 
							+ "\"from\" : \"" + sessionId + "\"," 
							+ "\"roomType\" : \"video\"," 
							+ "\"payload\":{" 
								+ "\"name\" : \"" + videoOrAudio + "\"," 
								+ "\"user\":{" 
									+ "\"firstname\":\"" + user.getFirstName() + "\"," 
									+ "\"image\":\"" + user.getImage() + "\"," 
									+ "\"image_thumb\":\"" + user.getImageThumb() + "\"," 
									+ "\"lastname\":\"" + user.getLastName() + "\"," 
									+ "\"user_id\":\"" + user.getId() + "\"" 
								+ "}" 
							+ "}" 
						+ "}," 
						+ "\"name\" : "	+ "\"" + action + "\"" 
					+ "}";

		return json;
	}

}
