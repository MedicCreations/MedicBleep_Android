package com.clover.spika.enterprise.chat;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.adapters.GroupAdapter;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class RoomsActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnSearchListener {

    public static void startActivity(String categoryId, String categoryName, @NonNull Context context) {
        Intent intent = new Intent(context, RoomsActivity.class);
        intent.putExtra(Const.CATEGORY_ID, categoryId);
        intent.putExtra(Const.CATEGORY_NAME, categoryName);
        context.startActivity(intent);
    }

    private TextView noItems;

    private PullToRefreshListView mainListView;
    public GroupAdapter adapter;

    private ImageButton searchBtn;
    private EditText searchEt;
    private ImageButton closeSearchBtn;

    private String mCategory = "0";
    private String mCategoryName = "";
    private String mSearchData;

    private int mCurrentIndex = 0;
    private int mTotalCount = 0;
    private int screenWidth;
    private int speedSearchAnimation = 300;
    private OnSearchListener mSearchListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

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
        mCategoryName = getIntent().getStringExtra(Const.CATEGORY_NAME);

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
            	closeSearchAnimation(searchBtn, (ImageButton)findViewById(R.id.goBack), 
            			closeSearchBtn, searchEt, (TextView) findViewById(R.id.screenTitle), 
            			screenWidth, speedSearchAnimation, (LinearLayout) findViewById(R.id.roomsOptions));
            }
        });
        
        findViewById(R.id.createRoomBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateRoomActivity.start(mCategory, mCategoryName, RoomsActivity.this);
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
    
    public void setSearch(OnSearchListener listener){
		mSearchListener = listener;
		setSearch(searchBtn, searchOnClickListener, searchEt, editorActionListener);
	}
	
	public void disableSearch(){
		disableSearch(searchBtn, searchEt, (ImageButton)findViewById(R.id.goBack), closeSearchBtn, 
				(TextView) findViewById(R.id.screenTitle), screenWidth, speedSearchAnimation, 
				(LinearLayout) findViewById(R.id.roomsOptions));
	}
	
	private OnClickListener searchOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (searchEt.getVisibility() == View.GONE) {
				openSearchAnimation(searchBtn, (ImageButton)findViewById(R.id.goBack), closeSearchBtn, searchEt, 
						(TextView) findViewById(R.id.screenTitle), screenWidth, speedSearchAnimation, 
						(LinearLayout) findViewById(R.id.roomsOptions));
			} else {
				if (mSearchListener != null) {
					String data = searchEt.getText().toString();
					hideKeyboard(searchEt);
					mSearchListener.onSearch(data);
				}
			}
		}
	};

	private OnEditorActionListener editorActionListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				hideKeyboard(searchEt);
				if (mSearchListener != null)
					mSearchListener.onSearch(v.getText().toString());
			}
			return false;
		}
	};

	@Override
	public void onBackPressed() {
		if (searchEt != null && searchEt.getVisibility() == View.VISIBLE) {
			closeSearchAnimation(searchBtn, (ImageButton)findViewById(R.id.goBack), closeSearchBtn, searchEt, 
					(TextView) findViewById(R.id.screenTitle), screenWidth, speedSearchAnimation, 
					(LinearLayout) findViewById(R.id.roomsOptions));
			return;
		}

		finish();
	}

    private void getRooms(int page, String search, final boolean toClear) {

    }
    
    @Override
	public void onResume() {
		super.onResume();
		setSearch(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		disableSearch();
	}
}
