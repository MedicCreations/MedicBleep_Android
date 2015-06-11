package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
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

import com.clover.spika.enterprise.chat.caching.ChatMembersCaching.OnChatMembersDBChanged;
import com.clover.spika.enterprise.chat.caching.GlobalCaching.OnGlobalMemberDBChanged;
import com.clover.spika.enterprise.chat.caching.GlobalCaching.OnGlobalMemberNetworkResult;
import com.clover.spika.enterprise.chat.caching.GlobalCaching.OnGlobalSearchDBChanged;
import com.clover.spika.enterprise.chat.caching.GlobalCaching.OnGlobalSearchNetworkResult;
import com.clover.spika.enterprise.chat.caching.robospice.ChatMembersCacheSpice;
import com.clover.spika.enterprise.chat.caching.robospice.GlobalCacheSpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.InviteUsersFragment;
import com.clover.spika.enterprise.chat.fragments.RemoveUsersFragment;
import com.clover.spika.enterprise.chat.listeners.OnInviteClickListener;
import com.clover.spika.enterprise.chat.listeners.OnRemoveClickListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchManageUsersListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;

public class ManageUsersActivity extends BaseActivity implements ViewPager.OnPageChangeListener, InviteUsersFragment.Callbacks, RemoveUsersFragment.Callbacks, OnClickListener,
		OnGlobalSearchDBChanged, OnGlobalSearchNetworkResult, OnGlobalMemberDBChanged, OnGlobalMemberNetworkResult, OnChatMembersDBChanged {

	private TextView mTitleTextView;

	/* Search bar */
	private ImageButton mInviteBtn;

	private ManageUsersFragmentAdapter mPagerAdapter;
	private ViewPager mViewPager;

	private String chatId = "";

	private OnInviteClickListener mOnInviteClickListener;
	private OnRemoveClickListener mOnRemoveClickListener;

	private ToggleButton inviteTab;
	private ToggleButton removeTab;

	private Chat chatModelNew = null;
	
	private List<GlobalModel> activeMembers = new ArrayList<GlobalModel>();

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
		
		//hide search from actionbar
		findViewById(R.id.searchBtn).setVisibility(View.GONE);

		inviteTab = (ToggleButton) findViewById(R.id.inviteTab);
		inviteTab.setOnClickListener(this);
		removeTab = (ToggleButton) findViewById(R.id.removeTab);
		removeTab.setOnClickListener(this);

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

		mInviteBtn = (ImageButton) findViewById(R.id.inviteBtn);

		mTitleTextView = (TextView) findViewById(R.id.screenTitle);

		handleIntent(getIntent());

		mInviteBtn.setOnClickListener(onInviteClick);
		setTabsStates(mViewPager.getCurrentItem());
		
	}
	
	public String getChatId(){
		return chatId;
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

		GlobalCacheSpice.GlobalSearch globalSearch = new GlobalCacheSpice.GlobalSearch(this, spiceManager, currentIndex, chatId, null, Type.USER, search, toClear, this, this);
		spiceManager.execute(globalSearch, new CustomSpiceListener<List>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onRequestSuccess(List result) {
				super.onRequestSuccess(result);

				for (int i = 0; i < result.size(); i++) {
					if (((GlobalModel) result.get(i)).isMember()) {
						((GlobalModel) result.get(i)).setSelected(true);
					}
				}

				mPagerAdapter.setInviteUsers(result, toClear);

				if (toUpdateMember) {
					mPagerAdapter.resetMembers();
					getMembers(0, false);
				}
			}
		});
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void getMembers(int currentIndex, final boolean toUpdateInviteMember) {

		ChatMembersCacheSpice.GetChatMembers chatMembers = new ChatMembersCacheSpice.GetChatMembers(this, spiceManager, chatId, this);
		spiceManager.execute(chatMembers, new CustomSpiceListener<List>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onRequestSuccess(List result) {
				super.onRequestSuccess(result);

				activeMembers.clear();
				activeMembers.addAll(result);
				mPagerAdapter.setMembers(result);

				if (toUpdateInviteMember) {
					getUsers(0, null, true, false);
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
			mTitleTextView.setText(getString(R.string.invite));
		} else if (1 == position) {
			mTitleTextView.setText(getString(R.string.remove));
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

		public void setInviteUsers(List<GlobalModel> userList, boolean toClear) {
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

		public void setMembers(List<GlobalModel> members) {
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
				if (mOnInviteClickListener != null) {
					mOnInviteClickListener.onInvite(chatId);
				}
			} else {
				if (mOnRemoveClickListener != null) {
					mOnRemoveClickListener.onRemove(chatId);
				}
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

	public void disableSearch() {
//		disableSearch(searchBtn, searchEt, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, mTitleTextView, screenWidth, speedSearchAnimation, mInviteBtn,
//				(LinearLayout) findViewById(R.id.invitationOptions));
	}

	public void setNewChat(Chat chat) {
		chatModelNew = chat;
	}

	@Override
	public void onBackPressed() {

		if (chatModelNew != null) {
			ChatActivity.startWithChatId(this, chatModelNew, chatModelNew.user);
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

	@Override
	public void onGlobalSearchNetworkResult(int totalCount) {
		mPagerAdapter.setUserTotalCount(totalCount);
	}

	@Override
	public void onGlobalSearchDBChanged(List<GlobalModel> usableData, boolean isClear) {
		mPagerAdapter.setInviteUsers(usableData, isClear);
	}

	@Override
	public void onGlobalMemberNetworkResult(int totalCount) {
		mPagerAdapter.setMemberTotalCount(totalCount);
	}

	@Override
	public void onGlobalMemberDBChanged(List<GlobalModel> usableData, boolean isClear) {

		mPagerAdapter.setMembers(usableData);

		if (isClear) {
			getUsers(0, null, true, false);
		}
	}

	@Override
	public void onChatMembersDBChanged(List<GlobalModel> usableData) {
		if(!usableData.equals(activeMembers)){
			activeMembers.clear();
			activeMembers.addAll(usableData);
			mPagerAdapter.setMembers(usableData);
		}else{
			Log.d("LOG", "EQUALS");
		}
	}

}
