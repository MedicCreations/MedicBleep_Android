package com.clover.spika.enterprise.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.clover.spika.enterprise.chat.adapters.ThreadsAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.TreeNode;

public class ThreadsActivity extends BaseActivity {

    public static final String EXTRA_MESSAGE_ID = "com.clover.spika.enterprise.extra_message_id";

    public static void start(Activity activity, int messageId) {
        Intent threadIntent = new Intent(activity, ThreadsActivity.class);
        threadIntent.putExtra(EXTRA_MESSAGE_ID, messageId);
        activity.startActivity(threadIntent);
    }

    private ListView listView;
    private TreeNode threads;

    private int mRootId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (getIntent().hasExtra(EXTRA_MESSAGE_ID)) {
            mRootId = getIntent().getIntExtra(EXTRA_MESSAGE_ID, 0);
            listView = (ListView) findViewById(R.id.main_list_view);

            new ChatApi().getThreads(mRootId, true, this, new ApiCallback<Chat>() {
                @Override
                public void onApiResponse(Result<Chat> result) {
                    if (result.isSuccess()) {
                        threads = new TreeNode(result.getResultData().getMessagesList());
                        listView.setAdapter(new ThreadsAdapter(ThreadsActivity.this, threads.asList()));
                    }
                }
            });
        }

    }
}
