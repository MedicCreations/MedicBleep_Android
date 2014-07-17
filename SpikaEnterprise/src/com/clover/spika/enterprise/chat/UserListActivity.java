package com.clover.spika.enterprise.chat;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.adapters.UserAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UsersApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserModel;

public class UserListActivity extends BaseActivity implements OnClickListener, OnItemClickListener, ApiCallback<UserModel> {

    private RelativeLayout noItemsLayout;
    private ListView userListView;

    private int mPage;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_character_list);

        noItemsLayout = (RelativeLayout) findViewById(R.id.noItemsLayout);

        userListView = (ListView) findViewById(R.id.main_list_view);
        userListView.setOnItemClickListener(this);

        mPage = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        new UsersApi().getUsersWithPage(this, mPage, true, this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.headerEditBack:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onApiResponse(Result<UserModel> result) {
        if (result.isSuccess() && result.hasResultData()) {
            if (result.getResultData().getUserList().isEmpty()) {
                if (noItemsLayout != null) {
                    noItemsLayout.setVisibility(View.VISIBLE);
                }
            } else {
                if (noItemsLayout != null) {
                    noItemsLayout.setVisibility(View.INVISIBLE);
                }
                if (userListView != null) {
                    userListView.setAdapter(new UserAdapter(this, result.getResultData().getUserList()));
                }
            }
        }
    }
}