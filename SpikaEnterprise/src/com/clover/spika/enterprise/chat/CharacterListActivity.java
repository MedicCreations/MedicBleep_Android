package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.adapters.CharacterAdapter;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Character;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CharacterListActivity extends BaseActivity implements OnClickListener, OnItemClickListener {

	public static final int FROM_FRIENDS_TAB = 1989;
	public static final int FROM_UPDATE = 1988;

	RelativeLayout noItemsLayout;

	ListView main_list_view;

	CharacterAdapter profileAdapter;

	private boolean isPaggingRunning = false;

	String gameId = null;
	String gameName = null;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_character_list);

		noItemsLayout = (RelativeLayout) findViewById(R.id.noItemsLayout);

		profileAdapter = new CharacterAdapter(this, new ArrayList<Character>());

		main_list_view = (ListView) findViewById(R.id.main_list_view);
		main_list_view.setOnItemClickListener(this);
		main_list_view.setAdapter(profileAdapter);
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
		}
	}

	public void findMyProfiles() {

		if (isPaggingRunning) {
			return;
		}

		new BaseAsyncTask<Void, Void, Integer>(this, true) {

			List<Character> profGame = new ArrayList<Character>();

			protected void onPreExecute() {
				super.onPreExecute();

				isPaggingRunning = true;
			}

			;

			protected Integer doInBackground(Void... params) {
				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_USERS));
					getParams.put(Const.FUNCTION, Const.F_USER_GET_ALL_CHARACTERS);
					getParams.put(Const.TOKEN, Const.TOKEN_DEFAULT);

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
			}

			;

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);

				if (result.equals(Const.E_SUCCESS)) {
					profileAdapter.clearItems();
					profileAdapter.addItems(profGame);
				}

				if (profileAdapter.getCount() > 0) {
					noItemsLayout.setVisibility(View.GONE);
				} else {
					noItemsLayout.setVisibility(View.VISIBLE);
				}

				isPaggingRunning = false;
			}

			;

		}.execute();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

		final Character character = profileAdapter.getItem(position);

		final String pushToken = getPushToken();

		new BaseAsyncTask<Void, Void, Integer>(this, true) {

			protected void onPreExecute() {
			}

			;

			protected Integer doInBackground(Void... params) {
				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_USERS));
					getParams.put(Const.FUNCTION, Const.F_USER_CREATE_CHARACTER);
					getParams.put(Const.TOKEN, Const.TOKEN_DEFAULT);

					JSONObject reqData = new JSONObject();
					reqData.put(Const.USERNAME, character.getUsername());
					reqData.put(Const.UUID_KEY, Const.getUUID(context));
					reqData.put(Const.ANDROID_PUSH_TOKEN, pushToken);

					JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

					if (result != null) {

						int code = result.getInt(Const.CODE);

						if (code == Const.C_SUCCESS) {
							String token = result.getString(Const.TOKEN);
							// String character_id =
							// result.getString(Const.CHARACTER_ID);

							SpikaEnterpriseApp.getSharedPreferences(context).setUserTokenId(token);
							Helper.setUserProperties(context, character.getCharacterId(), character.getImage_name(), character.getUsername());

							return Const.E_SUCCESS;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return Const.E_FAILED;
			}

			;

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				if (result.equals(Const.E_SUCCESS)) {
					Intent intent = new Intent(context, GroupListActivity.class);
					((BaseActivity) context).startActivity(intent);
				}
			}

			;

		}.execute();
	}
}