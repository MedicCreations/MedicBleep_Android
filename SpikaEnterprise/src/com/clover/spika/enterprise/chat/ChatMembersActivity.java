package com.clover.spika.enterprise.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;

public class ChatMembersActivity extends BaseActivity {

	ListView mainList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_members);

		findViewById(R.id.goBack).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});

		mainList = (ListView) findViewById(R.id.main_list_view);
	}

}
