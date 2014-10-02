package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UsersApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.InviteUsersFragment;
import com.clover.spika.enterprise.chat.fragments.MembersFragment;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UsersList;
import com.clover.spika.enterprise.chat.utils.Const;


public class ManageUsersActivity extends BaseActivity implements ViewPager.OnPageChangeListener,
        InviteUsersFragment.Callbacks,
        MembersFragment.Callbacks {

    private TextView mTitleTextView;
    
    /* Search bar */
	private ImageButton searchBtn;
	private EditText searchEt;
	private ImageButton closeSearchBtn;
	private ImageButton mInviteBtn;

	private UsersApi api;
    private ManageUsersFragmentAdapter mPagerAdapter;

	private String chatId = "";
	
	int screenWidth;
	int speedSearchAnimation = 300;// android.R.integer.config_shortAnimTime;
	private OnSearchListener mSearchListener;

    public static void startActivity(String chatId, Context context) {
		Intent intent = new Intent(context, ManageUsersActivity.class);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_members);

		api = new UsersApi();

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewPager userManagementViewPager = (ViewPager) findViewById(R.id.viewPagerUserManagement);
        mPagerAdapter = new ManageUsersFragmentAdapter();
		userManagementViewPager.setAdapter(mPagerAdapter);
		userManagementViewPager.setOnPageChangeListener(this);

		searchBtn = (ImageButton) findViewById(R.id.searchBtn);
		searchEt = (EditText) findViewById(R.id.searchEt);
		closeSearchBtn = (ImageButton) findViewById(R.id.close_search);
		mInviteBtn = (ImageButton) findViewById(R.id.inviteBtn);
		
		screenWidth = getResources().getDisplayMetrics().widthPixels;

		mTitleTextView = (TextView) findViewById(R.id.screenTitle);
		
		closeSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				closeSearchAnimation(searchBtn, (ImageButton)findViewById(R.id.goBack), closeSearchBtn, searchEt, mInviteBtn,
						mTitleTextView, screenWidth, speedSearchAnimation);
			}
		});

		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			chatId = intent.getExtras().getString(Const.CHAT_ID);
			getUsers(0, null, true);
			getMembers(0);
		}
	}

    @Override
	public void getUsers(int currentIndex, String search, final boolean toClear) {
		if (search == null) {
			api.getUsersWithPage(this, currentIndex, chatId, true, new ApiCallback<UsersList>() {

				@Override
				public void onApiResponse(Result<UsersList> result) {
					if (result.isSuccess()) {
						mPagerAdapter.setUserTotalCount(result.getResultData().getTotalCount());
						mPagerAdapter.setInviteUsers(result.getResultData().getUserList(), toClear);
					}
				}
			});
		} else {
			api.getUsersByName(currentIndex, chatId, search, this, true, new ApiCallback<UsersList>() {

				@Override
				public void onApiResponse(Result<UsersList> result) {
					if (result.isSuccess()) {
						mPagerAdapter.setUserTotalCount(result.getResultData().getTotalCount());
						mPagerAdapter.setInviteUsers(result.getResultData().getUserList(), toClear);
					}
				}
			});
		}
    	
	}

    @Override
    public void getMembers(int page) {
        api.getChatMembersWithPage(this, chatId, page, true, new ApiCallback<UsersList>() {
            @Override
            public void onApiResponse(Result<UsersList> result) {
                if (result.isSuccess()) {
                    mPagerAdapter.setMemberTotalCount(result.getResultData().getTotalCount());
                    mPagerAdapter.setMembers(result.getResultData().getMembersList());
                }
            }
        });
    }

    @Override public void onPageScrollStateChanged(int state) { }
    @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    @Override
    public void onPageSelected(int position) {
        if (0 == position) {
            // invite users selected
            mTitleTextView.setText(getString(R.string.invite));
        } else if (1 == position) {
            // remove users selected
            mTitleTextView.setText(getString(R.string.remove));
        }
    }

    private class ManageUsersFragmentAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> mFragmentList = new ArrayList<Fragment>();

        public ManageUsersFragmentAdapter() {
            super(getFragmentManager());
            mFragmentList.add(InviteUsersFragment.newInstance());
            mFragmentList.add(MembersFragment.newInstance());
        }

        @Override
        public Fragment getItem(int i) {
            return mFragmentList.get(i);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void setInviteUsers(List<User> userList, boolean toClear) {
            for (Fragment fragment : mFragmentList) {
                if (fragment instanceof InviteUsersFragment) {
                    ((InviteUsersFragment) fragment).setData(userList, toClear);
                }
            }
        }

        public void setUserTotalCount(int totalCount) {
            for (Fragment fragment : mFragmentList) {
                if (fragment instanceof InviteUsersFragment) {
                    ((InviteUsersFragment) fragment).setTotalCount(totalCount);
                }
            }
        }

        public void setMemberTotalCount(int totalCount) {
            for (Fragment fragment : mFragmentList) {
                if (fragment instanceof MembersFragment) {
                    ((MembersFragment) fragment).setTotalCount(totalCount);
                }
            }
        }

        public void setMembers(List<User> members) {
            for (Fragment fragment : mFragmentList) {
                if (fragment instanceof MembersFragment) {
                    ((MembersFragment) fragment).setMembers(members);
                }
            }
        }
    }
    
    //****************SEARCH HANDLING
    public void setSearch(OnSearchListener listener){
		mSearchListener = listener;
		setSearch(searchBtn, searchOnClickListener, searchEt, editorActionListener);
	}
	
	public void disableSearch(){
		disableSearch(searchBtn, searchEt, (ImageButton)findViewById(R.id.goBack), closeSearchBtn,
				mTitleTextView, screenWidth, speedSearchAnimation, mInviteBtn);
	}
	
	private OnClickListener searchOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (searchEt.getVisibility() == View.GONE) {
				openSearchAnimation(searchBtn, (ImageButton)findViewById(R.id.goBack), closeSearchBtn, 
						searchEt, mInviteBtn, mTitleTextView, screenWidth, speedSearchAnimation);
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
					mInviteBtn, mTitleTextView, screenWidth, speedSearchAnimation);
			return;
		}

		finish();
	}

}
