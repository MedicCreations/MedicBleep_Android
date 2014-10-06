package com.clover.spika.enterprise.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.GroupAdapter;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;

public class RoomsActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnSearchListener {

    public static void startActivity(String categoryId, @NonNull Context context) {
        Intent intent = new Intent(context, RoomsActivity.class);
        intent.putExtra(Const.CATEGORY_ID, categoryId);
        context.startActivity(intent);
    }

    private TextView noItems;

    private PullToRefreshListView mainListView;
    public GroupAdapter adapter;

    private ImageButton searchBtn;
    private EditText searchEt;
    private ImageButton closeSearchBtn;

    private String mCategory = "0";
    private String mSearchData;

    private int mCurrentIndex = 0;
    private int mTotalCount = 0;
    private int screenWidth;
    private int speedSearchAnimation = 300;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        adapter = new GroupAdapter(this, new ArrayList<Group>());

        mCurrentIndex = 0;

        noItems = (TextView) findViewById(R.id.noItems);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        searchBtn = (ImageButton) findViewById(R.id.searchBtn);
        searchEt = (EditText) findViewById(R.id.searchEt);
        closeSearchBtn = (ImageButton) findViewById(R.id.close_search);

        mainListView = (PullToRefreshListView) findViewById(R.id.mainListView);
        mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
        mainListView.setOnItemClickListener(this);

        mainListView.setAdapter(adapter);
        mainListView.setOnRefreshListener(refreshListener2);

        mCategory = getIntent().getStringExtra(Const.CATEGORY_ID);

        ((TextView)findViewById(R.id.screenTitle)).setText(getString(R.string.rooms));

        getRooms(mCurrentIndex, null, false);

        findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        closeSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearchAnimation(searchBtn, (ImageButton)findViewById(R.id.goBack), closeSearchBtn, searchEt,
                        (TextView) findViewById(R.id.screenTitle), screenWidth, speedSearchAnimation);
            }
        });
    }

    @Override
    public void onSearch(String data) {
        mCurrentIndex = 0;
        if (TextUtils.isEmpty(data)) {
            mSearchData = null;
        } else {
            mSearchData = data;
        }
        getRooms(mCurrentIndex, mSearchData, true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    PullToRefreshBase.OnRefreshListener2 refreshListener2 = new PullToRefreshBase.OnRefreshListener2() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {
            // mCurrentIndex--; don't need this for now
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            mCurrentIndex++;
            getRooms(mCurrentIndex, mSearchData, false);
        }
    };

    private void getRooms(int page, String search, final boolean toClear) {

    }
}
