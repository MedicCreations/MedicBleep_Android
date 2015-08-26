package com.medicbleep.app.chat.share;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.medicbleep.app.chat.ChatActivity;
import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.adapters.RecentAdapter;
import com.medicbleep.app.chat.api.ApiCallback;
import com.medicbleep.app.chat.api.FileManageApi;
import com.medicbleep.app.chat.api.robospice.ChatSpice;
import com.medicbleep.app.chat.caching.LobbyCaching.OnLobbyDBChanged;
import com.medicbleep.app.chat.caching.LobbyCaching.OnLobbyNetworkResult;
import com.medicbleep.app.chat.caching.robospice.LobbyCacheSpice;
import com.medicbleep.app.chat.dialogs.AppDialog;
import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.models.Chat;
import com.medicbleep.app.chat.models.Message;
import com.medicbleep.app.chat.models.Result;
import com.medicbleep.app.chat.models.SendMessageResponse;
import com.medicbleep.app.chat.models.UploadFileModel;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Helper;
import com.medicbleep.app.chat.utils.Utils;
import com.medicbleep.app.chat.views.pulltorefresh.PullToRefreshBase;
import com.medicbleep.app.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class ChooseLobbyActivity extends BaseActivity implements OnItemClickListener, OnLobbyDBChanged, OnLobbyNetworkResult {

	private PullToRefreshListView mainListView;
	private RecentAdapter adapter;
	private TextView noItems;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;

	public static void start(Context c, String fileId, String thumbId) {
		c.startActivity(new Intent(c, ChooseLobbyActivity.class).putExtra(Const.FILE_ID, fileId).putExtra(Const.THUMB_ID, thumbId).putExtra(Const.TYPE, Const.MSG_TYPE_PHOTO));
	}

	public static void start(Context c, String videoId) {
		c.startActivity(new Intent(c, ChooseLobbyActivity.class).putExtra(Const.FILE_ID, videoId).putExtra(Const.TYPE, Const.MSG_TYPE_VIDEO));
	}

	public static void start(Context c, Uri audioFile) {
		c.startActivity(new Intent(c, ChooseLobbyActivity.class).putExtra(Intent.EXTRA_STREAM, audioFile).putExtra(Const.TYPE, Const.MSG_TYPE_FILE)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_lobby_child);

		mCurrentIndex = 0;

		noItems = (TextView) findViewById(R.id.noItems);

		mainListView = (PullToRefreshListView) findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setOnItemClickListener(this);

		adapter = new RecentAdapter(spiceManager, this, new ArrayList<Chat>(), false);

		mainListView.setAdapter(adapter);
		mainListView.setOnRefreshListener(refreshListener2);

		getLobby(0, true);
	}

	@SuppressWarnings("rawtypes")
	PullToRefreshBase.OnRefreshListener2 refreshListener2 = new PullToRefreshBase.OnRefreshListener2() {
		@Override
		public void onPullDownToRefresh(PullToRefreshBase refreshView) {
			// mCurrentIndex--; don't need this for now
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase refreshView) {
			mCurrentIndex++;
			getLobby(mCurrentIndex, false);
		}
	};

	private void setData(List<Chat> data, boolean toClearPrevious) {
		if (mainListView == null) {
			return;
		}

		for (Chat item : data) {
			item.last_message = Message.decryptContent(this, item.last_message);
		}

		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();

		if (toClearPrevious) {
			currentCount = data.size();
		}

		if (toClearPrevious) {
			adapter.setData(data);
		} else {
			adapter.addData(data);
		}

		if (toClearPrevious) {
			mainListView.getRefreshableView().setSelection(0);
		}

		mainListView.onRefreshComplete();

		if (adapter.getCount() == 0) {
			mainListView.setVisibility(View.INVISIBLE);
			noItems.setVisibility(View.VISIBLE);
		} else {
			mainListView.setVisibility(View.VISIBLE);
			noItems.setVisibility(View.GONE);
		}

		if (currentCount >= mTotalCount) {
			mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		} else if (currentCount < mTotalCount) {
			mainListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		} else {
			mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		}
	}

	public void getLobby(int page, final boolean toClear) {

		LobbyCacheSpice.GetData recentFragmentGetData = new LobbyCacheSpice.GetData(this, spiceManager, page, toClear, this, this);
		spiceManager.execute(recentFragmentGetData, new CustomSpiceListener<List>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onRequestSuccess(List result) {
				super.onRequestSuccess(result);
				setData(result, toClear);
			}

		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - 1;

		Chat chat = adapter.getItem(position);

		if (getIntent().getIntExtra(Const.TYPE, -1) == Const.MSG_TYPE_PHOTO) {
			sendMessage(Const.MSG_TYPE_PHOTO, chat, "Shared Photo", getIntent().getStringExtra(Const.FILE_ID), getIntent().getStringExtra(Const.THUMB_ID), null, null);
		} else if (getIntent().getIntExtra(Const.TYPE, -1) == Const.MSG_TYPE_VIDEO) {
			sendMessage(Const.MSG_TYPE_VIDEO, chat, "Shared Video", getIntent().getStringExtra(Const.FILE_ID), null, null, null);
		} else if (getIntent().getIntExtra(Const.TYPE, -1) == Const.MSG_TYPE_FILE) {
			uploadFile((Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM), chat);
		}

	}

	public void uploadFile(Uri file, final Chat chat) {

		if (file.toString().contains("file://")) {
			try {
				String path = file.toString().substring(7);
				final String fileName = path.substring(path.lastIndexOf("/") + 1);
				new FileManageApi().uploadFile(path, this, true, new ApiCallback<UploadFileModel>() {

					@Override
					public void onApiResponse(Result<UploadFileModel> result) {
						if (result.isSuccess()) {
							sendMessage(Const.MSG_TYPE_FILE, chat, fileName, result.getResultData().getFileId(), null, null, null);
						} else {
							AppDialog dialog = new AppDialog(ChooseLobbyActivity.this, false);
							if (result.hasResultData()) {
								dialog.setFailed(result.getResultData().getMessage());
							} else {
								dialog.setFailed(Helper.errorDescriptions(getApplicationContext(), result.getResultData().getCode()));
							}
						}
					}
				});
			} catch (Exception e) {
				AppDialog dialog = new AppDialog(ChooseLobbyActivity.this, false);
				dialog.setFailed(getString(R.string.e_something_went_wrong));
			}
		} else {
			AppDialog dialog = new AppDialog(ChooseLobbyActivity.this, false);
			dialog.setFailed(getString(R.string.e_something_went_wrong));
		}
	}

	public void sendMessage(int type, final Chat chat, String text, String fileId, String thumbId, String longitude, String latitude) {

		handleProgress(true);

		ChatSpice.SendMessage sendMessage = new ChatSpice.SendMessage(type, String.valueOf(chat.getId()), text, fileId, thumbId, longitude, latitude, null, null);
		spiceManager.execute(sendMessage, new CustomSpiceListener<SendMessageResponse>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, ChooseLobbyActivity.this, ex);
			}

			@Override
			public void onRequestSuccess(SendMessageResponse result) {
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {
					ChatActivity.startWithChatId(ChooseLobbyActivity.this, chat, chat.user);
				} else {
					AppDialog dialog = new AppDialog(ChooseLobbyActivity.this, false);
					dialog.setFailed(result.getCode());
				}
			}
		});
	}

	@Override
	public void onRecentDBChanged(List<Chat> usableData, boolean isClear) {
		setData(usableData, isClear);
	}

	@Override
	public void onRecentNetworkResult(int totalCount) {
		mTotalCount = totalCount;
	}

}
