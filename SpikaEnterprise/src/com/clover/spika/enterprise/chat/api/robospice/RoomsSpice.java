package com.clover.spika.enterprise.chat.api.robospice;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.ConfirmUsersList;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.google.gson.Gson;
import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class RoomsSpice {

	public static class GetDistinctUserOK extends OkHttpSpiceRequest<ConfirmUsersList> {

		private Context ctx;
		private String userIds;
		private String groupIds;
		private String roomIds;
		private String groupAllIds;
		private String roomAllIds;

		public GetDistinctUserOK(String userIds, String groupIds, String roomIds, String groupAllIds, String roomAllIds, Context context) {
			super(ConfirmUsersList.class);

			this.ctx = context;
			this.userIds = userIds;
			this.groupIds = groupIds;
			this.roomIds = roomIds;
			this.groupAllIds = groupAllIds;
			this.roomAllIds = roomAllIds;
		}

		@Override
		public ConfirmUsersList loadDataFromNetwork() throws Exception {

			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put(Const.USER_IDS, userIds);
			parameters.put(Const.GROUP_IDS, groupIds);
			parameters.put(Const.GROUP_ALL_IDS, groupAllIds);
			parameters.put(Const.ROOM_IDS, roomIds);
			parameters.put(Const.ROOM_ALL_IDS, roomAllIds);

			boolean isAdd = false;
			String urlParams = "";

			Map<String, String> uriVariables = new HashMap<String, String>();

			if (!TextUtils.isEmpty(userIds)) {
				urlParams += Const.USER_IDS + "=" + userIds;
				isAdd = true;

				uriVariables.put(Const.USER_IDS, userIds);
			}

			if (!TextUtils.isEmpty(groupIds)) {

				if (isAdd) {
					urlParams += "&";
				}

				urlParams += Const.GROUP_IDS + "=" + groupIds;
				isAdd = true;

				uriVariables.put(Const.GROUP_IDS, groupIds);
			}

			if (!TextUtils.isEmpty(groupAllIds)) {

				if (isAdd) {
					urlParams += "&";
				}

				urlParams += Const.GROUP_ALL_IDS + "=" + groupAllIds;
				isAdd = true;

				uriVariables.put(Const.GROUP_ALL_IDS, groupAllIds);
			}

			if (!TextUtils.isEmpty(roomIds)) {

				if (isAdd) {
					urlParams += "&";
				}

				urlParams += Const.ROOM_IDS + "=" + roomIds;
				isAdd = true;

				uriVariables.put(Const.ROOM_IDS, roomIds);
			}

			if (!TextUtils.isEmpty(roomAllIds)) {

				if (isAdd) {
					urlParams += "&";
				}

				urlParams += Const.ROOM_ALL_IDS + "=" + roomAllIds;
				isAdd = true;

				uriVariables.put(Const.ROOM_ALL_IDS, roomAllIds);
			}

			Request request = new Request.Builder()
			.addHeader("Encoding", "UTF-8")
			.addHeader(Const.APP_VERSION, Helper.getAppVersion())
			.addHeader(Const.PLATFORM, "android")
			.addHeader("token", SpikaEnterpriseApp.getSharedPreferences(ctx).getToken())
			.url(Const.BASE_URL + Const.F_GET_DISTINC_USER + (TextUtils.isEmpty(urlParams) ? "" : "?" + urlParams)).get().build();

			Call connection = getOkHttpClient().newCall(request);

			// TODO Jackson2 needs to be implemented for arrays
			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseString = resBody.string();

			// ObjectMapper mapper = new ObjectMapper();

			// ConfirmUsersList ress = mapper.readValue(responseString,
			// ConfirmUsersList.class);

			return new Gson().fromJson(responseString, ConfirmUsersList.class);
		}
	}

}
