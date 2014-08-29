package com.clover.spika.enterprise.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.clover.spika.enterprise.chat.adapters.ThreadsAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseChatActivity;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.TreeNode;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.utils.Const;

public class ThreadsActivity extends BaseChatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
        ApiCallback<Integer> {

    public static final String EXTRA_ROOT_ID = "com.clover.spika.enterprise.extra_root_id";
    public static final String EXTRA_CHAT_ID = "com.clover.spika.enterprise.extra_chat_id";
    public static final String EXTRA_MESSAGE_ID = "com.clover.spika.enterprise.extra_message_id";
    public static final String EXTRA_PHOTO_THUMB = "com.clover.spika.enterprise.extra_photo_thumb";
    public static final String EXTRA_CHAT_NAME = "com.clover.spika.enterprise.extra_chat_name";

    public static void start(Activity activity, String root, String chatId, String messageId, String photoThumb, String chatName) {
        Intent threadIntent = new Intent(activity, ThreadsActivity.class);
        threadIntent.putExtra(EXTRA_ROOT_ID, root);
        threadIntent.putExtra(EXTRA_CHAT_ID, chatId);
        threadIntent.putExtra(EXTRA_MESSAGE_ID, messageId);
        threadIntent.putExtra(EXTRA_PHOTO_THUMB, photoThumb);
        threadIntent.putExtra(EXTRA_CHAT_NAME, chatName);
        activity.startActivity(threadIntent);
    }

    private TreeNode threads;

    private String mRootId;
    private String mMessageId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            mRootId = getIntent().getStringExtra(EXTRA_ROOT_ID);
            chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);
            mMessageId = getIntent().getStringExtra(EXTRA_MESSAGE_ID);
            chatImage = getIntent().getStringExtra(EXTRA_PHOTO_THUMB);
            chatName = getIntent().getStringExtra(EXTRA_CHAT_NAME);
            setTitle(chatName);

            chatListView.setOnItemClickListener(this);
            chatListView.setOnItemLongClickListener(this);
            chatListView.setAdapter(new ThreadsAdapter(this));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadImage();
        getThreads();
    }

    private void getThreads() {
        new ChatApi().getThreads(mRootId, true, this, new ApiCallback<Chat>() {
            @Override
            public void onApiResponse(Result<Chat> result) {
                if (result.isSuccess()) {
                    threads = new TreeNode(result.getResultData().getMessagesList());
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
        });
    }

    private void sendMessage(String text) {
        new ChatApi().sendMessage(Const.MSG_TYPE_DEFAULT, chatId, text, null, null, null, null, mRootId, mMessageId, this, this);
    }

    private void sendFile(String fileName, String fileId) {
        new ChatApi().sendMessage(Const.MSG_TYPE_FILE, chatId, fileName, fileId, null, null, null,
                mRootId, mMessageId, this, this);
    }

    @Override
    protected void leaveChat() {

    }

    @Override
    protected void onEditorSendEvent(String text) {
        sendMessage(text);
    }

    @Override
    protected void onChatPushUpdated() {
        getThreads();
    }

    @Override
    protected void onMessageDeleted() {
        getThreads();
    }

    @Override
    protected void onFileSelected(int result, final String fileName, String filePath) {
        if (result == RESULT_OK) {
            new FileManageApi().uploadFile(filePath, this, true, new ApiCallback<UploadFileModel>() {
                @Override
                public void onApiResponse(Result<UploadFileModel> result) {
                    if (result.isSuccess()) {
                        sendFile(fileName, result.getResultData().getFileId());
                    } else {
                        AppDialog dialog = new AppDialog(ThreadsActivity.this, false);
                        if (result.hasResultData()) {
                            dialog.setFailed(result.getResultData().getMessage());
                        } else {
                            dialog.setFailed("");
                        }
                    }
                }
            });
        }
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ThreadsAdapter threadsAdapter = (ThreadsAdapter) chatListView.getAdapter();
        threadsAdapter.setSelectedItem(position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getAdapter() != null) {
            ThreadsAdapter threadsAdapter = (ThreadsAdapter) parent.getAdapter();
            if (threadsAdapter.getItem(position).getMessage().isMe()) {
                deleteMessage(threadsAdapter.getItem(position).getMessage().getId());
            }
        }
        return true;
    }

    public void onApiResponse(Result<Integer> result) {
        if (result.isSuccess()) {
            etMessage.setText("");
            hideKeyboard(etMessage);

            getThreads();
        } else {
            AppDialog dialog = new AppDialog(this, false);
            dialog.setFailed(result.getResultData());
        }
    }
}
