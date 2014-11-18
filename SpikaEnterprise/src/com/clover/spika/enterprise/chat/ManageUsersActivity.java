package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ToggleButton;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UsersApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.InviteUsersFragment;
import com.clover.spika.enterprise.chat.fragments.RemoveUsersFragment;
import com.clover.spika.enterprise.chat.listeners.OnInviteClickListener;
import com.clover.spika.enterprise.chat.listeners.OnRemoveClickListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchManageUsersListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UsersList;
import com.clover.spika.enterprise.chat.utils.Const;

public class ManageUsersActivity extends BaseActivity implements ViewPager.OnPageChangeListener, InviteUsersFragment.Callbacks, RemoveUsersFragment.Callbacks, OnClickListener {

	private TextView mTitleTextView;

	/* Search bar */
	private ImageButton searchBtn;
	private EditText searchEt;
	private ImageButton closeSearchBtn;
	private ImageButton mInviteBtn;

	private UsersApi api;
	private ManageUsersFragmentAdapter mPagerAdapter;
	private ViewPager mViewPager;

	private String chatId = "";

	int screenWidth;
	int speedSearchAnimation = 300;// android.R.integer.config_shortAnimTime;
	private OnSearchManageUsersListener mSearchListener;
	private OnInviteClickListener mOnInviteClickListener;
	private OnRemoveClickListener mOnRemoveClickListener;

	private ToggleButton inviteTab;
	private ToggleButton removeTab;

	private Chat chatModelNew = null;

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

		inviteTab = (ToggleButton) findViewById(R.id.inviteTab);
		inviteTab.setOnClickListener(this);
		removeTab = (ToggleButton) findViewById(R.id.removeTab);
		removeTab.setOnClickListener(this);

		api = new UsersApi();

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		mViewPager = (ViewPager) findViewById(R.id.viewPagerUserManagement);
		mPagerAdapter = new ManageUsersFragmentAdapter();
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(this);

		searchBtn = (ImageButton) findViewById(R.id.searchBtn);
		searchEt = (EditText) findViewById(R.id.searchEt);
		closeSearchBtn = (ImageButton) findViewById(R.id.close_search);
		mInviteBtn = (ImageButton) findViewById(R.id.inviteBtn);

		screenWidth = getResources().getDisplayMetrics().widthPixels;

		mTitleTextView = (TextView) findViewById(R.id.screenTitle);

		closeSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				closeSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, searchEt, mInviteBtn, mTitleTextView, screenWidth, speedSearchAnimation,
						(LinearLayout) findViewById(R.id.invitationOptions));
			}
		});

		handleIntent(getIntent());

		mInviteBtn.setOnClickListener(onInviteClick);
		setTabsStates(mViewPager.getCurrentItem());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			chatId = intent.getExtras().getString(Const.CHAT_ID);
			getUsers(0, null, true, false);
			getMembers(0, false);
		}
	}

	@Override
	public void getUsers(int currentIndex, String search, final boolean toClear, final boolean toUpdateMember) {
		if (search == null) {
			api.getUsersWithPage(this, currentIndex, chatId, true, new ApiCallback<UsersList>() {

				@Override
				public void onApiResponse(Result<UsersList> result) {
					if (result.isSuccess()) {
						mPagerAdapter.setUserTotalCount(result.getResultData().getTotalCount());
						mPagerAdapter.setInviteUsers(result.getResultData().getUserList(), toClear);
						if (toUpdateMember) {
							mPagerAdapter.resetMembers();
							getMembers(0, false);
						}
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
	public void getMembers(int currentIndex, final boolean toUpdateInviteMember) {
		api.getChatMembersWithPage(this, chatId, currentIndex, true, new ApiCallback<UsersList>() {
			@Override
			public void onApiResponse(Result<UsersList> result) {
				if (result.isSuccess()) {
					mPagerAdapter.setMemberTotalCount(result.getResultData().getTotalCount());
					mPagerAdapter.setMembers(result.getResultData().getMembersList());
					if (toUpdateInviteMember) {
						getUsers(0, null, true, false);
					}
				}
			}
		});
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		setTabsStates(position);
		if (0 == position) {
			// invite users selected
			mTitleTextView.setText(getString(R.string.invite));
			setSearch(mSearchListener);
		} else if (1 == position) {
			// remove users selected
			mTitleTextView.setText(getString(R.string.remove));
			disableSearch();
		}
	}

	private class ManageUsersFragmentAdapter extends FragmentStatePagerAdapter {

		private List<Fragment> mFragmentList = new ArrayList<Fragment>();

		public ManageUsersFragmentAdapter() {
			super(getSupportFragmentManager());
			mFragmentList.add(InviteUsersFragment.newInstance());
			mFragmentList.add(RemoveUsersFragment.newInstance());
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
				if (fragment instanceof RemoveUsersFragment) {
					((RemoveUsersFragment) fragment).setTotalCount(totalCount);
				}
			}
		}

		public void setMembers(List<User> members) {
			for (Fragment fragment : mFragmentList) {
				if (fragment instanceof RemoveUsersFragment) {
					((RemoveUsersFragment) fragment).setMembers(members);
				}
			}
		}

		public void resetMembers() {
			for (Fragment fragment : mFragmentList) {
				if (fragment instanceof RemoveUsersFragment) {
					((RemoveUsersFragment) fragment).resetMembers();
				}
			}
		}
	}

	private OnClickListener onInviteClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mViewPager.getCurrentItem() == 0) {
				if (mOnInviteClickListener != null)
					mOnInviteClickListener.onInvite(chatId);
			} else {
				if (mOnRemoveClickListener != null)
					mOnRemoveClickListener.onRemove(chatId);
			}
		}
	};

	public void setOnInviteClickListener(OnInviteClickListener lis) {
		mOnInviteClickListener = lis;
	}

	public void setOnRemoveClickListener(OnRemoveClickListener lis) {
		mOnRemoveClickListener = lis;
	}

	// ****************SEARCH HANDLING
	public void setSearch(OnSearchManageUsersListener listener) {
		mSearchListener = listener;
		setSearch(searchBtn, searchOnClickListener, searchEt, editorActionListener);
	}

	public void disableSearch() {
		disableSearch(searchBtn, searchEt, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, mTitleTextView, screenWidth, speedSearchAnimation, mInviteBtn,
				(LinearLayout) findViewById(R.id.invitationOptions));
	}

	private OnClickListener searchOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (searchEt.getVisibility() == View.GONE) {
				openSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, searchEt, mInviteBtn, mTitleTextView, screenWidth, speedSearchAnimation,
						(LinearLayout) findViewById(R.id.invitationOptions));
			} else {
				if (mSearchListener != null) {
					String data = searchEt.getText().toString();
					hideKeyboard(searchEt);
					if (mViewPager.getCurrentItem() == 0) {
						mSearchListener.onSearchInInvite(data);
					}
				}
			}
		}
	};

	private OnEditorActionListener editorActionListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				hideKeyboard(searchEt);
				if (mSearchListener != null) {
					String data = v.getText().toString();
					if (mViewPager.getCurrentItem() == 0) {
						mSearchListener.onSearchInInvite(data);
					}
				}
			}
			return false;
		}
	};

	public void setNewChat(Chat chat) {
		chatModelNew = chat;
	}

	@Override
	public void onBackPressed() {
		if (searchEt != null && searchEt.getVisibility() == View.VISIBLE) {
			closeSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, searchEt, mInviteBtn, mTitleTextView, screenWidth, speedSearchAnimation,
					(LinearLayout) findViewById(R.id.invitationOptions));
			return;
		}
		if (chatModelNew != null) {
			Intent intent = new Intent(ManageUsersActivity.this, ChatActivity.class);
			intent.putExtra(Const.CHAT_ID, String.valueOf(chatModelNew.getChat_id()));
			intent.putExtra(Const.CHAT_NAME, chatModelNew.getChat_name());
			intent.putExtra(Const.IMAGE, chatModelNew.getImage());
			intent.putExtra(Const.IMAGE_THUMB, chatModelNew.getImageThumb());
			intent.putExtra(Const.TYPE, chatModelNew.getType());
			intent.putExtra(Const.IS_ACTIVE, chatModelNew.isActive());
			intent.putExtra(Const.ADMIN_ID, chatModelNew.getAdminId());
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
		}

		finish();

	}

	@Override
	public void onClick(View view) {
		if (view == inviteTab) {
			setTabsStates(0);
			mViewPager.setCurrentItem(0, true);
		} else if (view == removeTab) {
			setTabsStates(1);
			mViewPager.setCurrentItem(1, true);
		}
	}

	void setTabsStates(int position) {
		if (position == 0) {
			inviteTab.setChecked(true);
			removeTab.setChecked(false);
		} else {
			inviteTab.setChecked(false);
			removeTab.setChecked(true);
		}
	}

}
