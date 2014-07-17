package com.clover.spika.enterprise.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.adapters.UserAdapter;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddMembers extends BaseActivity implements OnClickListener {

	ImageView headerEditBack;
	ImageView headerRightIcon;
	ListView main_list_view;
	RelativeLayout noItemsLayout;

	UserAdapter profileAdapter;

	String groupName = "";

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_add_members);

		headerEditBack = (ImageView) findViewById(R.id.headerEditBack);
		headerEditBack.setOnClickListener(this);
		headerRightIcon = (ImageView) findViewById(R.id.headerRightIcon);
		headerRightIcon.setOnClickListener(this);
		main_list_view = (ListView) findViewById(R.id.main_list_view);
		noItemsLayout = (RelativeLayout) findViewById(R.id.noItemsLayout);

		profileAdapter = new UserAdapter(this, new ArrayList<User>());
//		profileAdapter.setSelect(true);
		main_list_view.setAdapter(profileAdapter);

		if (getIntent().getExtras().containsKey(Const.GROUP_NAME)) {
			groupName = getIntent().getExtras().getString(Const.GROUP_NAME);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		findMyProfiles();
	}

	@Override
	public void onClick(View view) {

		int id = view.getId();
		if (id == R.id.headerEditBack) {
			finish();
		} else if (id == R.id.headerRightIcon) {
//			if (profileAdapter.getSelectedIds().length > 0) {
//				createGroup(groupName, profileAdapter.getSelectedIds());
//			}
		} else {
		}
	}

	public void findMyProfiles() {
		new BaseAsyncTask<Void, Void, Integer>(this, true) {

			List<Character> profGame = new ArrayList<Character>();

			protected Integer doInBackground(Void... params) {
				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_USERS));
					getParams.put(Const.FUNCTION, Const.F_USER_GET_ALL_CHARACTERS);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject result = NetworkManagement.httpPostRequest(getParams, new JSONObject());

					if (result != null) {

						JSONArray items = result.getJSONArray(Const.ITEMS);

						for (int i = 0; i < items.length(); i++) {
							JSONObject obj = (JSONObject) items.get(i);

							Gson sGsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
							Character profile = sGsonExpose.fromJson(obj.toString(), Character.class);

							if (profile != null) {
								profGame.add(profile);
							}
						}

						if (profGame.size() > 0) {
							return Const.E_SUCCESS;
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
//					profileAdapter.clearItems();
//					profileAdapter.addItems(profGame);
				}

				if (profileAdapter.getCount() > 0) {
					noItemsLayout.setVisibility(View.GONE);
				} else {
					noItemsLayout.setVisibility(View.VISIBLE);
				}
			};

		}.execute();
	}

	private void createGroup(final String groupName, final String[] selected) {
		new BaseAsyncTask<Void, Void, Integer>(this, true) {

			protected Integer doInBackground(Void... params) {
				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_USERS));
					getParams.put(Const.FUNCTION, Const.F_USER_CREATE_GROUP);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqData = new JSONObject();
					reqData.put(Const.GROUP_NAME, groupName);

					StringBuilder finalString = new StringBuilder();

					for (int i = 0; i < selected.length; i++) {
						finalString.append(selected[i]);

						if (i != selected.length - 1) {
							finalString.append(",");
						}

					}

					reqData.put(Const.ADD_MEMBERS, finalString.toString());

					JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

					if (result != null) {
						return result.getInt(Const.CODE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return Const.E_FAILED;
			};

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);

				Intent intent = new Intent();
				intent.putExtra(Const.CODE, result);
				setResult(RESULT_OK, intent);
				((Activity) context).finish();
			};

		}.execute();
	}
}
