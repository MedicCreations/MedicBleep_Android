package com.clover.spika.enterprise.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.TreeNode;

import java.util.List;

public class ThreadsActivity extends BaseActivity {

    public static void start(Activity activity) {
        Intent threadIntent = new Intent(activity, ThreadsActivity.class);
        activity.startActivity(threadIntent);
    }

    private ListView listView;
    private TreeNode threads;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        listView = (ListView) findViewById(R.id.main_list_view);

        new ChatApi().getThreads(966, true, this, new ApiCallback<Chat>() {
            @Override
            public void onApiResponse(Result<Chat> result) {
                if (result.isSuccess()) {
                    threads = new TreeNode(result.getResultData().getMessagesList());
                    List<Message> messages = threads.toArrayList();
                    Log.d("PORUKE", messages.toString());
                }
            }
        });
    }
}
