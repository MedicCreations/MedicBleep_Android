package com.clover.spika.enterprise.chat.api.robospice;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import android.content.Context;
import com.clover.spika.enterprise.chat.models.ConfirmUsersList;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;

public class RoomsSpice {

	public static class GetDistinctUser extends CustomSpiceRequest<ConfirmUsersList> {

		private Context ctx;
		private String userIds;
		private String groupIds;
		private String roomIds;
		private String groupAllIds;
		private String roomAllIds;

		public GetDistinctUser(String userIds, String groupIds, String roomIds, String groupAllIds, String roomAllIds, Context context) {
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

			ResponseEntity<ConfirmUsersList> entity = getRestTemplate().exchange(Const.BASE_URL + Const.F_GET_DISTINC_USER, HttpMethod.GET, getGetheaders(ctx),
					ConfirmUsersList.class, parameters);
			return entity.getBody();
		}
	}

}
