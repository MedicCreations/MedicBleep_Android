package com.clover.spika.enterprise.chat.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.UserAdapter;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatSettingsDialog extends DialogFragment implements OnTouchListener {

	private static final int ADD = 0;
	private static final int KICK = 1;

	String groupId = "";

	TextView goBackLayout;

	RelativeLayout settingsLayout;

	TextView addMembers;
	TextView kickMembers;
	TextView clearGroup;

	RelativeLayout settingsListLayout;

	ListView main_list_view;
	UserAdapter adapter;
	TextView noItemsView;

	TextView submitChanges;

	boolean isAdd = false;
	boolean isKick = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Dialog);

		Bundle bundle = getArguments();

		if (bundle != null) {
			this.groupId = bundle.getString(Const.GROUP_ID);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.dialog_wall_settings, container);

		goBackLayout = (TextView) view.findViewById(R.id.goBackLayout);
		goBackLayout.setOnTouchListener(this);

		settingsLayout = (RelativeLayout) view.findViewById(R.id.settingsLayout);

		addMembers = (TextView) view.findViewById(R.id.addMembers);
		addMembers.setOnTouchListener(this);
		kickMembers = (TextView) view.findViewById(R.id.kickMembers);
		kickMembers.setOnTouchListener(this);
		clearGroup = (TextView) view.findViewById(R.id.clearGroup);
		clearGroup.setOnTouchListener(this);

		settingsListLayout = (RelativeLayout) view.findViewById(R.id.settingsListLayout);

		main_list_view = (ListView) view.findViewById(R.id.main_list_view);
//		adapter = new UserAdapter(getActivity(), new ArrayList<Character>());
//		adapter.setSelect(true);

		main_list_view.setAdapter(adapter);

		noItemsView = (TextView) view.findViewById(R.id.noItemsView);

		submitChanges = (TextView) view.findViewById(R.id.submitChanges);
		submitChanges.setOnTouchListener(this);

		return view;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {

		int id = view.getId();
		if (id == R.id.goBackLayout) {
			settingsListLayout.setVisibility(View.GONE);
			settingsLayout.setVisibility(View.VISIBLE);
//			adapter.clearItems();
		} else if (id == R.id.addMembers) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				addMembers.setBackgroundResource(R.drawable.tab_mask_blue);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				addMembers.setBackgroundResource(R.drawable.tab_mask);

				showList(ADD);
			}
		} else if (id == R.id.kickMembers) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				kickMembers.setBackgroundResource(R.drawable.tab_mask_blue);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				kickMembers.setBackgroundResource(R.drawable.tab_mask);

				showList(KICK);
			}
		} else if (id == R.id.clearGroup) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				clearGroup.setBackgroundResource(R.drawable.tab_mask_blue);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				clearGroup.setBackgroundResource(R.drawable.tab_mask);

				deleteGroup(groupId);
			}
		} else if (id == R.id.submitChanges) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				submitChanges.setBackgroundResource(R.drawable.tab_mask_blue);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				submitChanges.setBackgroundResource(R.drawable.tab_mask);

//				if (isAdd) {
//					if (adapter.getSelectedIds().length > 0) {
//						addMembers(groupId, adapter.getSelectedIds());
//					}
//				} else if (isKick) {
//					if (adapter.getSelectedIds().length > 0) {
//						kickMembers(groupId, adapter.getSelectedIds());
//					}
//				}
			}
		} else {
		}

		return true;
	}

	private void showList(int type) {
		settingsListLayout.setVisibility(View.VISIBLE);
		settingsLayout.setVisibility(View.GONE);

		if (type == ADD) {
			isAdd = true;
			isKick = false;
			findCharacters(groupId);
		} else if (type == KICK) {
			isAdd = false;
			isKick = true;
			findMembers(groupId);
		}
	}

	private void findCharacters(final String groupId) {
		new BaseAsyncTask<Void, Void, Integer>(getActivity(), false) {

			List<Character> profGame = new ArrayList<Character>();

			protected Integer doInBackground(Void... params) {
				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_USERS));
					getParams.put(Const.FUNCTION, Const.F_USER_GET_ALL_CHARACTERS);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqData = new JSONObject();
					reqData.put(Const.GROUP_ID, groupId);

					JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

					if (result != null) {

						int code = result.getInt(Const.CODE);

						if (code == Const.E_SUCCESS) {
							JSONArray items = result.getJSONArray(Const.ITEMS);

							for (int i = 0; i < items.length(); i++) {
								JSONObject obj = (JSONObject) items.get(i);

								Gson sGsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
								Character profile = sGsonExpose.fromJson(obj.toString(), Character.class);

								if (profile != null) {
									profGame.add(profile);
								}
							}

							return Const.E_SUCCESS;
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

				if (result == Const.E_SUCCESS) {
//					adapter.clearItems();
//					adapter.addItems(profGame);
				} else {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setFailed(result);
				}

				if (adapter.getCount() > 0) {
					noItemsView.setVisibility(View.GONE);
				} else {
					noItemsView.setVisibility(View.VISIBLE);
				}
			};

		}.execute();
	}

	private void findMembers(final String groupId) {
		new BaseAsyncTask<Void, Void, Integer>(getActivity(), false) {

			List<Character> profGame = new ArrayList<Character>();

			protected Integer doInBackground(Void... params) {
				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_USERS));
					getParams.put(Const.FUNCTION, Const.F_USER_GET_GROUP_MEMBERS);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqData = new JSONObject();
					reqData.put(Const.GROUP_ID, groupId);

					JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

					if (result != null) {

						int code = result.getInt(Const.CODE);

						if (code == Const.E_SUCCESS) {
							JSONArray items = result.getJSONArray(Const.ITEMS);

							for (int i = 0; i < items.length(); i++) {
								JSONObject obj = (JSONObject) items.get(i);

								Gson sGsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
								Character profile = sGsonExpose.fromJson(obj.toString(), Character.class);

								if (profile != null) {
									profGame.add(profile);
								}
							}

							return Const.E_SUCCESS;
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
//					adapter.clearItems();
//
//					for (int i = 0; i < profGame.size(); i++) {
//						profGame.get(i).setSelected(true);
//					}
//
//					adapter.addItems(profGame);
				} else {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setFailed(result);
				}

				if (adapter.getCount() > 0) {
					noItemsView.setVisibility(View.GONE);
				} else {
					noItemsView.setVisibility(View.VISIBLE);
				}
			};

		}.execute();
	}

	private void addMembers(final String groupId, final String[] selected) {
		new BaseAsyncTask<Void, Void, Integer>(getActivity(), true) {

			protected void onPreExecute() {
				dismiss();
				super.onPreExecute();
			};

			protected Integer doInBackground(Void... params) {
				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_USERS));
					getParams.put(Const.FUNCTION, Const.F_USER_ADD_MEMBER);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqData = new JSONObject();
					reqData.put(Const.GROUP_ID, groupId);

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

				if (result == Const.E_SUCCESS) {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setSucceed();
				} else {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setFailed(result);
				}
			};

		}.execute();
	}

	private void kickMembers(final String groupId, final String[] deselected) {
		new BaseAsyncTask<Void, Void, Integer>(getActivity(), false) {

			protected void onPreExecute() {
				dismiss();
				super.onPreExecute();
			};

			protected Integer doInBackground(Void... params) {
				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_USERS));
					getParams.put(Const.FUNCTION, Const.F_USER_KICK_MEMBER);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqData = new JSONObject();
					reqData.put(Const.GROUP_ID, groupId);

					StringBuilder finalString = new StringBuilder();

					for (int i = 0; i < deselected.length; i++) {
						finalString.append(deselected[i]);

						if (i != deselected.length - 1) {
							finalString.append(",");
						}
					}

					reqData.put(Const.KICK_MEMBERS, finalString.toString());

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

				if (result == Const.E_SUCCESS) {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setSucceed();
				} else {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setFailed(result);
				}
			};

		}.execute();
	}

	private void deleteGroup(final String group_id) {
		new BaseAsyncTask<Void, Void, Integer>(getActivity(), false) {

			protected Integer doInBackground(Void... params) {

				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_USERS));
					getParams.put(Const.FUNCTION, Const.F_USER_DELETE_GROUP);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqObj = new JSONObject();
					reqObj.put(Const.GROUP_ID, String.valueOf(group_id));

					JSONObject result = NetworkManagement.httpPostRequest(getParams, reqObj);

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
				if (result == Const.E_SUCCESS) {
					dismiss();

					AppDialog dialog = new AppDialog(context, true);
					dialog.setSucceed();

				} else {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setFailed(result);
				}
			};
		}.execute();
	}
}