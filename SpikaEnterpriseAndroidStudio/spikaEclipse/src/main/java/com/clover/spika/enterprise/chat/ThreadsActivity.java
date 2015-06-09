package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.clover.spika.enterprise.chat.adapters.ThreadsAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.api.robospice.ChatSpice;
import com.clover.spika.enterprise.chat.caching.ThreadCaching.OnThreadDBChanged;
import com.clover.spika.enterprise.chat.caching.ThreadCaching.OnThreadNetworkResult;
import com.clover.spika.enterprise.chat.caching.robospice.ThreadCacheSpice;
import com.clover.spika.enterprise.chat.caching.utils.DaoUtils;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseChatActivity;
import com.clover.spika.enterprise.chat.listeners.OnCheckEncryptionListener;
import com.clover.spika.enterprise.chat.listeners.OnInternetErrorListener;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.SendMessageResponse;
import com.clover.spika.enterprise.chat.models.Stickers;
import com.clover.spika.enterprise.chat.models.TreeNode;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.emoji.SelectEmojiListener;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class ThreadsActivity extends BaseChatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, OnThreadDBChanged, OnThreadNetworkResult {

	public static final String EXTRA_USER_ID = "com.clover.spika.enterprise.extra_user_id";
	public static final String EXTRA_ROOT_ID = "com.clover.spika.enterprise.extra_root_id";
	public static final String EXTRA_CHAT_ID = "com.clover.spika.enterprise.extra_chat_id";
	public static final String EXTRA_MESSAGE_ID = "com.clover.spika.enterprise.extra_message_id";
	public static final String EXTRA_PHOTO_THUMB = "com.clover.spika.enterprise.extra_photo_thumb";
	public static final String EXTRA_PHOTO = "com.clover.spika.enterprise.extra_photo";
	public static final String EXTRA_CHAT_NAME = "com.clover.spika.enterprise.extra_chat_name";

	public static void start(Activity activity, String root, String chatId, String messageId, String photoThumb, String photo, String chatName, String userId) {
		Intent threadIntent = new Intent(activity, ThreadsActivity.class);
		threadIntent.putExtra(EXTRA_USER_ID, userId);
		threadIntent.putExtra(EXTRA_ROOT_ID, root);
		threadIntent.putExtra(EXTRA_CHAT_ID, chatId);
		threadIntent.putExtra(EXTRA_MESSAGE_ID, messageId);
		threadIntent.putExtra(EXTRA_PHOTO_THUMB, photoThumb);
		threadIntent.putExtra(EXTRA_PHOTO, photo);
		threadIntent.putExtra(EXTRA_CHAT_NAME, chatName);
		activity.startActivity(threadIntent);
	}

	private TreeNode threads;

	private String mRootId;
	private String mMessageId;
	private String mUserId;

	private List<Message> activeData = new ArrayList<Message>();
	private List<Message> tempDataForSend = new ArrayList<Message>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getIntent().getExtras() != null) {
			mUserId = getIntent().getStringExtra(EXTRA_USER_ID);
			mRootId = getIntent().getStringExtra(EXTRA_ROOT_ID);
			chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);
			mMessageId = getIntent().getStringExtra(EXTRA_MESSAGE_ID);
			chatImageThumb = getIntent().getStringExtra(EXTRA_PHOTO_THUMB);
			chatImage = getIntent().getStringExtra(EXTRA_PHOTO);
			chatName = getIntent().getStringExtra(EXTRA_CHAT_NAME);
			setTitle(chatName);

			chatListView.setOnItemClickListener(this);
			chatListView.setOnItemLongClickListener(this);
			ThreadsAdapter adapter = new ThreadsAdapter(spiceManager, this);
			chatListView.setAdapter(adapter);

			setEmojiListener(new SelectEmojiListener() {

				@Override
				public void onEmojiSelect(Stickers selectedStickers) {
					sendEmoji(selectedStickers.getUrl());
				}
			});
			
			findViewById(R.id.settingsBtn).setVisibility(View.INVISIBLE);
			setMenuByChatType(true);

            SwipeRefreshLayout swipeControll = (SwipeRefreshLayout) findViewById(R.id.swipeControll);
            swipeControll.setEnabled(false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getThreads();
	}

	@SuppressWarnings("rawtypes")
	private void getThreads() {
		
		ThreadCacheSpice.GetData threadCacheSpice = new ThreadCacheSpice.GetData(this, spiceManager, mRootId,  this, this);
		offlineSpiceManager.execute(threadCacheSpice, new CustomSpiceListener<List>() {

			@SuppressWarnings({ "unchecked" })
			@Override
			public void onRequestSuccess(List result) {
				for(Object item : result){
					((Message)item).setIsCodeTextStyle();
				}
				if(result.size() < 1) return;
				activeData.clear();
				activeData.addAll(result);
				
				threads = new TreeNode(result);
				((ThreadsAdapter) chatListView.getAdapter()).updateContent(threads.asList());
				
				ThreadsAdapter threadsAdapter = (ThreadsAdapter) chatListView.getAdapter();
				for (int i = 0; i < threadsAdapter.getCount(); i++) {
					if (threadsAdapter.getItem(i).getMessage().getId().equals(mMessageId)) {
						threadsAdapter.setSelectedItem(i);
						chatListView.setSelection(i);
						break;
					}
				}
				
			}
		});
		
	}

	
	private void sendMessage(String text) {
		
		ThreadsAdapter threadsAdapter = (ThreadsAdapter) chatListView.getAdapter();
		
		Message tempMess = new Message();
		tempMess.setText(text);
		tempMess.type = Const.MSG_TYPE_TEMP_MESS;
		tempMess.isMe = true;
		tempMess.created = String.valueOf((int)(System.currentTimeMillis() / 1000));
		tempMess.parent_id = Integer.valueOf(mMessageId);
		tempMess.root_id = Integer.valueOf(mRootId);
		tempMess.user_id = Helper.getUserId();
		try {
			tempMess.id = String.valueOf(Long.valueOf(activeData.get(activeData.size() -2).id) + 10);
		} catch (Exception e) {
			tempMess.id = String.valueOf(mMessageId + 10);
		}
		
		final Message decryptMess = Message.decryptContent(this, tempMess);
		activeData.add(decryptMess);
		tempDataForSend.add(decryptMess);
		
		List<Message> newList = new ArrayList<Message>();
		newList.addAll(activeData);
		
		threads = new TreeNode(newList);
		threadsAdapter.updateContentNoDecrypt(threads.asList());
		
		etMessage.setText("");
		for (int i = 0; i < threadsAdapter.getCount(); i++) {
			if (threadsAdapter.getItem(i).getMessage().getId().equals(tempMess.id)) {
				chatListView.setSelection(i);
				break;
			}
		}
		for (int i = 0; i < threadsAdapter.getCount(); i++) {
			if (threadsAdapter.getItem(i).getMessage().getId().equals(mMessageId)) {
				threadsAdapter.setSelectedItem(i);
				break;
			}
		}
		
		ChatSpice.SendMessage sendMessage = new ChatSpice.SendMessage(Const.MSG_TYPE_DEFAULT, chatId, text, null, null, null, null, mRootId, mMessageId);
		spiceManager.execute(sendMessage, new CustomSpiceListener<SendMessageResponse>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				if(ex instanceof NoNetworkException){
					setViewNoInternetConnection(R.id.rootView, R.id.actionBarLayout);
				}
				decryptMess.type = Const.MSG_TYPE_TEMP_MESS_ERROR;
				setErrorMessage(decryptMess);
			}

			@Override
			public void onRequestSuccess(SendMessageResponse result) {
				onApiResponse(result);
			}
		});
		
	}
	
	protected void setErrorMessage(Message decryptMess) {
		ThreadsAdapter threadsAdapter = (ThreadsAdapter) chatListView.getAdapter();
		threadsAdapter.notifyDataSetChanged();
	}
	
	protected void showResendDialog(final Message message) {
		AppDialog dialog = new AppDialog(this, false);
		dialog.setYesNo(getString(R.string.resend_message), getString(R.string.resend), getString(R.string.cancel));
		dialog.setOnPositiveButtonClick(new AppDialog.OnPositiveButtonClickListener() {
			
			@Override
			public void onPositiveButtonClick(View v, Dialog d) {
				resendMessage(message);
			}
		});
	}
	
	private void resendMessage(Message message) {
		activeData.remove(message);
		tempDataForSend.remove(message);
		
		sendMessage(message.getText());
	}

	private void addNewMessage(SendMessageResponse result) {
		ThreadsAdapter threadsAdapter = (ThreadsAdapter) chatListView.getAdapter();
		
		com.clover.spika.enterprise.chat.models.greendao.Message messDao = DaoUtils.convertMessageModelToMessageDao(null, result.message_model, Integer.valueOf(result.message_model.chat_id));
		getDaoSession().getMessageDao().insert(messDao);
		
		Message newMess = Message.decryptContent(this, result.message_model);
		
		for(Message item : tempDataForSend){
			if(item.text.equals(newMess.text)){
				tempDataForSend.remove(item);
				activeData.remove(item);
				break;
			}
		}
		
		activeData.add(newMess);
		
		List<Message> newList = new ArrayList<Message>();
		newList.addAll(activeData);
		
		threads = new TreeNode(newList);
		threadsAdapter.updateContentNoDecrypt(threads.asList());
		
		for (int i = 0; i < threadsAdapter.getCount(); i++) {
			if (threadsAdapter.getItem(i).getMessage().getId().equals(mMessageId)) {
				threadsAdapter.setSelectedItem(i);
				break;
			}
		}
	}

	private void sendFile(boolean toCrypt, String fileName, String fileId) {
		handleProgress(true);
		
		String attributes = null;
		if(!toCrypt){
			attributes = "{\"encrypted\":\"0\"}";
		}
		
		ChatSpice.SendMessage sendMessage = new ChatSpice.SendMessage(attributes, Const.MSG_TYPE_FILE, chatId, fileName, fileId, null, null, null, mRootId, mMessageId);
		spiceManager.execute(sendMessage, new CustomSpiceListener<SendMessageResponse>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, ThreadsActivity.this);
			}

			@Override
			public void onRequestSuccess(SendMessageResponse result) {
				handleProgress(false);
				onApiResponse(result);
			}
		});
	}

	private void sendEmoji(String text) {
		
		handleProgress(true);
		ChatSpice.SendMessage sendMessage = new ChatSpice.SendMessage(Const.MSG_TYPE_GIF, chatId, text, null, null, null, null, mRootId, mMessageId);
		spiceManager.execute(sendMessage, new CustomSpiceListener<SendMessageResponse>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, ThreadsActivity.this);
			}

			@Override
			public void onRequestSuccess(SendMessageResponse result) {
				handleProgress(false);
				onApiResponse(result);
			}
		});
	}

	@Override
	protected void leaveChat() {
	}

	@Override
	protected void onEditorSendEvent(final String text) {
		new Handler().post(new Runnable() {

			@Override
			public void run() {
				sendMessage(text);
			}
		});
	}

	@Override
	protected void onChatPushUpdated() {
		getThreads();
	}

	@Override
	protected void onMessageDeleted(Message message) {
		getThreads();
	}

	@Override
	protected void onFileSelected(int result, final String fileName, String filePath) {
		if (result == RESULT_OK) {
			checkForEncryption(filePath, fileName);
		} else if (result == RESULT_CANCELED) {
		} else {
			AppDialog dialog = new AppDialog(this, false);
			dialog.setFailed(getResources().getString(R.string.e_while_encrypting));
		}
	}
	
	private void checkForEncryption(final String mFilePath2, final String fileName) {
		Utils.checkForEncryption(this, mFilePath2, new OnCheckEncryptionListener() {
			
			@Override
			public void onCheckFinish(String path, boolean toCrypt) {
				uploadFile(mFilePath2, fileName, toCrypt);
			}
		});
	}
	
	private void uploadFile(final String filePath, final String fileName, final boolean toCrypt){
		new FileManageApi().uploadFile(toCrypt, filePath, this, true, new ApiCallback<UploadFileModel>() {

			@Override
			public void onApiResponse(Result<UploadFileModel> result) {
				if (result.isSuccess()) {
					sendFile(toCrypt, fileName, result.getResultData().getFileId());
				} else {
					AppDialog dialog = new AppDialog(ThreadsActivity.this, false);
					if (result.hasResultData()) {
						dialog.setFailed(result.getResultData().getMessage());
					} else {
						dialog.setFailed(Helper.errorDescriptions(getApplicationContext(), result.getResultData().getCode()));
					}
				}
			}
		});
	}

	@Override
	protected String getRootId() {
		return this.mRootId;
	}

	@Override
	protected String getMessageId() {
		return this.mMessageId;
	}

	@Override
	protected int getUserId() {
		return Integer.valueOf(mUserId);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ThreadsAdapter threadsAdapter = (ThreadsAdapter) chatListView.getAdapter();
		if(threadsAdapter.getItem(position).getMessage().type == Const.MSG_TYPE_TEMP_MESS_ERROR){
			showResendDialog(threadsAdapter.getItem(position).getMessage());
		}else if(threadsAdapter.getItem(position).getMessage().type != Const.MSG_TYPE_TEMP_MESS){
			threadsAdapter.setSelectedItem(position);
			mMessageId = threadsAdapter.getItem(position).getMessage().getId();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getAdapter() != null) {
			ThreadsAdapter threadsAdapter = (ThreadsAdapter) parent.getAdapter();
			mMessageId = threadsAdapter.getItem(position).getMessage().getId();
			if (threadsAdapter.getItem(position).getMessage().isMe() && threadsAdapter.getItem(position).getMessage().type != Const.MSG_TYPE_DELETED
					&& threadsAdapter.getItem(position).getMessage().type != Const.MSG_TYPE_TEMP_MESS
					&& threadsAdapter.getItem(position).getMessage().type != Const.MSG_TYPE_TEMP_MESS_ERROR) {
				deleteMessage(threadsAdapter.getItem(position).getMessage());
			}
		}
		return true;
	}

	public void onApiResponse(SendMessageResponse result) {
		if (result.getCode() == Const.API_SUCCESS) {

			if (result.message_model.type != Const.MSG_TYPE_DEFAULT){
				forceClose();
			}else{
			}
			addNewMessage(result);

		} else {
			AppDialog dialog = new AppDialog(this, false);
			dialog.setFailed(result.getCode());
		}
	}

	@Override
	protected void deactivateChat() {

	}

	@Override
	protected void deleteChat() {

	}

	@Override
	protected void activateChat() {

	}

	@Override
	public void onThreadNetworkResult() {}

	@Override
	public void onThreadDBChanged(List<Message> usableData) {
		if(activeData.equals(usableData)){
			Log.d("LOG", "SAME IN THREAD");
		}else{
			Log.d("LOG", "NOT SAME IN THREAD");
			activeData.clear();
			activeData.addAll(usableData);
			threads = new TreeNode(usableData);
			((ThreadsAdapter) chatListView.getAdapter()).updateContent(threads.asList());
			
			ThreadsAdapter threadsAdapter = (ThreadsAdapter) chatListView.getAdapter();
			for (int i = 0; i < threadsAdapter.getCount(); i++) {
				if (threadsAdapter.getItem(i).getMessage().getId().equals(mMessageId)) {
					threadsAdapter.setSelectedItem(i);
					chatListView.setSelection(i);
					break;
				}
			}
		}
		
	}
	
	public OnInternetErrorListener getInternetErrorListener(){
		return onInternetErrorListener;
	}
	
	protected OnInternetErrorListener onInternetErrorListener = new OnInternetErrorListener() {
		
		@Override
		public void onInternetError() {
			setViewNoInternetConnection(R.id.rootView, R.id.actionBarLayout);
		}
	};
}
