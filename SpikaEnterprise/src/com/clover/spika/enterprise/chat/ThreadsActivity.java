package com.clover.spika.enterprise.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.clover.spika.enterprise.chat.adapters.ThreadsAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseChatActivity;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.TreeNode;
import com.clover.spika.enterprise.chat.utils.Const;

public class ThreadsActivity extends BaseChatActivity {

    public static final String EXTRA_ROOT_ID = "com.clover.spika.enterprise.extra_root_id";
    public static final String EXTRA_CHAT_ID = "com.clover.spika.enterprise.extra_chat_id";
    public static final String EXTRA_MESSAGE_ID = "com.clover.spika.enterprise.extra_message_id";
    public static final String EXTRA_PHOTO_THUMB = "com.clover.spika.enterprise.extra_photo_thumb";

    public static void start(Activity activity, String root, String chatId, String messageId, String photoThumb) {
        Intent threadIntent = new Intent(activity, ThreadsActivity.class);
        threadIntent.putExtra(EXTRA_ROOT_ID, root);
        threadIntent.putExtra(EXTRA_CHAT_ID, chatId);
        threadIntent.putExtra(EXTRA_MESSAGE_ID, messageId);
        threadIntent.putExtra(EXTRA_PHOTO_THUMB, photoThumb);
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
            getThreads();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadImage();
    }

    private void getThreads() {
        new ChatApi().getThreads(mRootId, true, this, new ApiCallback<Chat>() {
            @Override
            public void onApiResponse(Result<Chat> result) {
                if (result.isSuccess()) {
                    threads = new TreeNode(result.getResultData().getMessagesList());
                    chatListView.setAdapter(new ThreadsAdapter(ThreadsActivity.this, threads.asList()));
                }
            }
        });
    }

    private void sendMessage(String text) {
        new ChatApi().sendMessage(Const.MSG_TYPE_DEFAULT, chatId, text, null, null, null, null, mRootId, mMessageId, this, new ApiCallback<Integer>() {
            @Override
            public void onApiResponse(Result<Integer> result) {
                if (result.isSuccess()) {
                    etMessage.setText("");
                    hideKeyboard(etMessage);

                    getThreads();
                } else {
                    AppDialog dialog = new AppDialog(ThreadsActivity.this, false);
                    dialog.setFailed(result.getResultData());
                }
            }
        });
    }

    @Override
    protected void leaveChat() {

    }

    @Override
    protected void onEditorSendEvent(String text) {
        sendMessage(text);
    }
}
