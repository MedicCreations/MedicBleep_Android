package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.GroupAdapter;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GroupListActivity extends BaseActivity implements OnClickListener {

	public static GroupListActivity instance;

	TextView screenTitle;

	RelativeLayout noItemsLayout;

	ListView mainListView;
	public GroupAdapter adapter;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_group_list);

		instance = this;

		screenTitle = (TextView) findViewById(R.id.screenTitle);

		noItemsLayout = (RelativeLayout) findViewById(R.id.noItemsLayout);

		mainListView = (ListView) findViewById(R.id.mainListView);
		adapter = new GroupAdapter(this, new ArrayList<Group>());

		mainListView.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getList();
	}

	public void getList() {
		new BaseAsyncTask<Void, Void, Integer>(this, true) {

			List<Group> tempDiscussion = new ArrayList<Group>();
			String newGroupPeriod = null;
			Integer code = 0;

			protected void onPreExecute() {
				super.onPreExecute();
			};

			protected Integer doInBackground(Void... params) {

				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_USERS));
					getParams.put(Const.FUNCTION, Const.F_USER_GET_GROUPS);
					getParams.put(Const.TOKEN, BaseActivity.getPreferences().getToken());

					JSONObject result = NetworkManagement.httpPostRequest(getParams, new JSONObject());

					if (result != null) {

						code = result.getInt(Const.CODE);

						if (code == Const.E_SUCCESS) {

							newGroupPeriod = result.getString(Const.NEW_GROUP_PERIOD);

							JSONArray items = result.getJSONArray(Const.ITEMS);

							for (int i = 0; i < items.length(); i++) {
								JSONObject obj = (JSONObject) items.get(i);

								Gson sGsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
								Group discussion = sGsonExpose.fromJson(obj.toString(), Group.class);

								if (discussion != null) {
									tempDiscussion.add(discussion);
								}
							}

							if (tempDiscussion.size() > 0) {
								return Const.E_SUCCESS;
							}
						} else {
							return code;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return Const.E_FAILED;
			};

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);

				if (result.equals(Const.E_SUCCESS)) {
					noItemsLayout.setVisibility(View.GONE);
					adapter.clearItems();
					adapter.addItems(tempDiscussion);
					adapter.setNewGroupPeriod(newGroupPeriod);
				} else {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setFailed(result);
					// noItemsLayout.setVisibility(View.VISIBLE);
				}
			};

		}.execute();
	}

	@Override
	public void onClick(View view) {
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
	}

}
