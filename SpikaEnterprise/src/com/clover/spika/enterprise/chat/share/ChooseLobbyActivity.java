package com.clover.spika.enterprise.chat.share;

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

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.RecentAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.api.LobbyApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.LobbyModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class ChooseLobbyActivity extends BaseActivity implements OnItemClickListener{
	
	private PullToRefreshListView mainListView;
	private RecentAdapter adapter;
	private TextView noItems;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	
	public static void start(Context c, String fileId, String thumbId){
		c.startActivity(new Intent(c, ChooseLobbyActivity.class)
				.putExtra(Const.FILE_ID, fileId)
				.putExtra(Const.THUMB_ID, thumbId)
				.putExtra(Const.TYPE, Const.MSG_TYPE_PHOTO));
	}
	
	public static void start(Context c, String videoId){
		c.startActivity(new Intent(c, ChooseLobbyActivity.class)
				.putExtra(Const.FILE_ID, videoId)
				.putExtra(Const.TYPE, Const.MSG_TYPE_VIDEO));
	}
	
	public static void start(Context c, Uri audioFile){
		c.startActivity(new Intent(c, ChooseLobbyActivity.class)
				.putExtra(Intent.EXTRA_STREAM, audioFile)
				.putExtra(Const.TYPE, Const.MSG_TYPE_FILE)
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
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();
		if (toClearPrevious)
			currentCount = data.size();

		if (toClearPrevious)
			adapter.setData(data);
		else
			adapter.addData(data);
		if (toClearPrevious)
			mainListView.getRefreshableView().setSelection(0);

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
		new LobbyApi().getLobbyByType(page, Const.ALL_TOGETHER_TYPE, this, true, new ApiCallback<LobbyModel>() {

			@Override
			public void onApiResponse(Result<LobbyModel> result) {
				if (result.isSuccess()) {
					mTotalCount = result.getResultData().getAllLobby().getTotalCount();
					setData(result.getResultData().getAllLobby().getChatsList(), toClear);
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - 1;
		
		Chat chat = adapter.getItem(position);
		
		if(getIntent().getIntExtra(Const.TYPE, -1) == Const.MSG_TYPE_PHOTO){
			sendMessage(Const.MSG_TYPE_PHOTO, chat, "Shared Photo", getIntent().getStringExtra(Const.FILE_ID), 
					getIntent().getStringExtra(Const.THUMB_ID), null, null);
		}else if(getIntent().getIntExtra(Const.TYPE, -1) == Const.MSG_TYPE_VIDEO){
			sendMessage(Const.MSG_TYPE_VIDEO, chat, "Shared Video", getIntent().getStringExtra(Const.FILE_ID), 
					null, null, null);
		}else if(getIntent().getIntExtra(Const.TYPE, -1) == Const.MSG_TYPE_FILE){
			uploadFile((Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM), chat);
		}

	}
	
	public void uploadFile(Uri file, final Chat chat){
		if(file.toString().contains("file://")){
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
		}else{
			AppDialog dialog = new AppDialog(ChooseLobbyActivity.this, false);
			dialog.setFailed(getString(R.string.e_something_went_wrong));
		}
		
	}
	
	public void sendMessage(int type, final Chat chat, String text, String fileId, String thumbId, String longitude, String latitude) {
		new ChatApi().sendMessage(type, String.valueOf(chat.getId()), text, fileId, thumbId, longitude, latitude, this, new ApiCallback<Integer>() {

			@Override
			public void onApiResponse(Result<Integer> result) {
				if (result.isSuccess()) {
					ChatActivity.startWithChatId(ChooseLobbyActivity.this, String.valueOf(chat.getId()), chat.getPassword());
				} else {
					AppDialog dialog = new AppDialog(ChooseLobbyActivity.this, false);
					dialog.setFailed(result.getResultData());
				}
			}
		});
	}


}
