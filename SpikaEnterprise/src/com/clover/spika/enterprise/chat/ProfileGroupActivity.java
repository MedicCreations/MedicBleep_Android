package com.clover.spika.enterprise.chat;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.clover.spika.enterprise.chat.api.robospice.ChatSpice;
import com.clover.spika.enterprise.chat.caching.ChatMembersCaching.OnChatMembersDBChanged;
import com.clover.spika.enterprise.chat.caching.robospice.ChatMembersCacheSpice;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.MembersFragment;
import com.clover.spika.enterprise.chat.fragments.ProfileGroupFragment;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class ProfileGroupActivity extends BaseActivity implements OnPageChangeListener, OnClickListener, MembersFragment.Callbacks,
		OnChatMembersDBChanged {

	ViewPager viewPager;
	ToggleButton profileTab;
	ToggleButton membersTab;

	RobotoRegularTextView tvSaveRoom;

	String chatId;
	ProfileFragmentPagerAdapter profileFragmentPagerAdapter;

	private boolean fromChatAct = false;
	private boolean updateImage = false;
	private String newImage = "";
	private String newThumbImage = "";
	private boolean isAdmin = false;
	private String categoryName = null;
	private String categoryId = null;

	public static void openProfile(Context context, String fileId, String chatName, String chatId, boolean isAdmin, String categoryId,
			String categoryName, String chatPassword) {

		Intent intent = new Intent(context, ProfileGroupActivity.class);

		intent.putExtra(Const.IMAGE, fileId);
		intent.putExtra(Const.CHAT_NAME, chatName);
		intent.putExtra(Const.IS_ADMIN, isAdmin);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.putExtra(Const.CATEGORY_ID, categoryId);
		intent.putExtra(Const.PASSWORD, chatPassword);
		intent.putExtra(Const.CATEGORY_NAME, categoryName);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		context.startActivity(intent);
	}

	public static void openProfile(Context context, String fileId, String chatName, String chatId, boolean isAdmin, boolean fromChat, int isPrivate,
			String chatPassword, String categoryId, String categoryName) {

		Intent intent = new Intent(context, ProfileGroupActivity.class);

		intent.putExtra(Const.IMAGE, fileId);
		intent.putExtra(Const.CHAT_NAME, chatName);
		intent.putExtra(Const.IS_ADMIN, isAdmin);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.putExtra(Const.FROM_CHAT, fromChat);
		intent.putExtra(Const.IS_PRIVATE, isPrivate);
		intent.putExtra(Const.PASSWORD, chatPassword);
		intent.putExtra(Const.CATEGORY_ID, categoryId);
		intent.putExtra(Const.CATEGORY_NAME, categoryName);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_group);

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		tvSaveRoom = (RobotoRegularTextView) findViewById(R.id.saveRoomProfile);
		isAdmin = getIntent().getBooleanExtra(Const.IS_ADMIN, false);
		if (isAdmin) {
			tvSaveRoom.setVisibility(View.VISIBLE);
			tvSaveRoom.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					saveSettings();
				}
			});
		} else {
			tvSaveRoom.setVisibility(View.GONE);
		}

		viewPager = (ViewPager) findViewById(R.id.viewPager);

		profileFragmentPagerAdapter = new ProfileFragmentPagerAdapter(getIntent());
		viewPager.setAdapter(profileFragmentPagerAdapter);
		viewPager.setOnPageChangeListener(this);

		profileTab = (ToggleButton) findViewById(R.id.profileTab);
		profileTab.setOnClickListener(this);
		membersTab = (ToggleButton) findViewById(R.id.membersTab);
		membersTab.setOnClickListener(this);

		chatId = getIntent().getExtras().getString(Const.CHAT_ID, "");
		categoryId = getIntent().getExtras().getString(Const.CATEGORY_ID, null);
		categoryName = getIntent().getExtras().getString(Const.CATEGORY_NAME, null);

		fromChatAct = getIntent().getBooleanExtra(Const.FROM_CHAT, false);

		getMembers(0, false);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		getMembers(0, false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setTabsStates(viewPager.getCurrentItem());
	}

	public class ProfileFragmentPagerAdapter extends FragmentPagerAdapter {

		private List<Fragment> mFragmentList = new ArrayList<Fragment>();

		public ProfileFragmentPagerAdapter(Intent intent) {
			super(getSupportFragmentManager());
			mFragmentList.add(new ProfileGroupFragment(intent));
			mFragmentList.add(MembersFragment.newInstance());
		}

		@Override
		public int getCount() {
			return mFragmentList.size();
		}

		@Override
		public Fragment getItem(int position) {
			return mFragmentList.get(position);
		}

		public void setMemberTotalCount(int totalCount) {
			for (Fragment fragment : mFragmentList) {
				if (fragment instanceof MembersFragment) {
					((MembersFragment) fragment).setTotalCount(totalCount);
				}
			}
		}

		public void setMembers(List<GlobalModel> members) {
			for (Fragment fragment : mFragmentList) {
				if (fragment instanceof MembersFragment) {
					((MembersFragment) fragment).setMembers(members);
				}
			}
		}

		public void setAdminData(Intent intent) {
			for (Fragment fragment : mFragmentList) {
				if (fragment instanceof ProfileGroupFragment) {
					((ProfileGroupFragment) fragment).setData(intent);
					((ProfileGroupFragment) fragment).setVisual();
				}
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		setTabsStates(position);
	}

	@Override
	public void onClick(View view) {
		if (view == profileTab) {
			setTabsStates(0);
			viewPager.setCurrentItem(0, true);
		} else if (view == membersTab) {
			setTabsStates(1);
			viewPager.setCurrentItem(1, true);
		}
	}

	void setTabsStates(int position) {
		if (position == 0) {
			profileTab.setChecked(true);
			membersTab.setChecked(false);
		} else {
			profileTab.setChecked(false);
			membersTab.setChecked(true);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void getMembers(int page, final boolean toUpdateInviteMember) {

		ChatMembersCacheSpice.GetChatMembers chatMembers = new ChatMembersCacheSpice.GetChatMembers(this, spiceManager, chatId, this);
		spiceManager.execute(chatMembers, new CustomSpiceListener<List>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onRequestSuccess(List result) {
				super.onRequestSuccess(result);
				profileFragmentPagerAdapter.setMembers(result);
			}
		});
	}

	public void setChangeImage(String image, String imageThumb) {
		newImage = image;
		newThumbImage = imageThumb;
		updateImage = true;
	}

	@Override
	public void finish() {
		if (fromChatAct && updateImage && !TextUtils.isEmpty(newImage)) {
			ChatActivity.startUpdateImage(this, newImage, newThumbImage, null);
		}

		super.finish();
	}

	public void saveSettings() {

		HashMap<String, String> requestParams = new HashMap<String, String>();

		final Switch switchPrivate = (Switch) findViewById(R.id.switch_private_room);

		Button tvPassword = (Button) findViewById(R.id.tvPassword);
		final String newPassword = tvPassword.getText().toString();

		if (!TextUtils.isEmpty(newPassword) && !newPassword.equals(getString(R.string.password))) {
			try {
				String hashPassword = Utils.getHexString(newPassword);
				requestParams.put(Const.PASSWORD, hashPassword);
				Helper.storeChatPassword(ProfileGroupActivity.this, hashPassword, chatId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		requestParams.put(Const.CHAT_ID, chatId);
		requestParams.put(Const.IS_PRIVATE, switchPrivate.isChecked() ? "1" : "0");
		requestParams.put(Const.CATEGORY_ID, categoryId);

		handleProgress(true);
		ChatSpice.UpdateChatAll updateChatAll = new ChatSpice.UpdateChatAll(requestParams);
		spiceManager.execute(updateChatAll, new CustomSpiceListener<Chat>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, ProfileGroupActivity.this);
			}

			@Override
			public void onRequestSuccess(Chat result) {
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {
					Intent intent = new Intent();
					intent.setAction(Const.IS_ADMIN);

					intent.putExtra(Const.IS_UPDATE_PRIVATE_PASSWORD, true);
					intent.putExtra(Const.IS_PRIVATE, switchPrivate.isChecked() ? 1 : 0);
					if (!TextUtils.isEmpty(newPassword) && !newPassword.equals(getString(R.string.password))) {
						try {
							String hashPassword = Utils.getHexString(newPassword);
							intent.putExtra(Const.PASSWORD, hashPassword);
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}

					intent.putExtra(Const.IS_UPDATE_ADMIN, true);
					intent.putExtra(Const.IS_UPDATE_CATEGORY, true);
					intent.putExtra(Const.CATEGORY_ID, categoryId);
					intent.putExtra(Const.CATEGORY_NAME, categoryName);

					LocalBroadcastManager.getInstance(ProfileGroupActivity.this).sendBroadcast(intent);

					finish();
				} else {
					AppDialog dialog = new AppDialog(ProfileGroupActivity.this, false);
					dialog.setFailed(null);
				}
			}
		});
	}

	public void changeCategory(String categoryId, String categoryName) {
		this.categoryId = categoryId;
		this.categoryName = categoryName;

		Intent intent = getIntent();
		intent.setAction(Const.IS_ADMIN);
		intent.putExtra(Const.IS_UPDATE_CATEGORY, true);
		intent.putExtra(Const.IS_ADMIN, isAdmin);
		intent.putExtra(Const.CATEGORY_ID, categoryId);
		intent.putExtra(Const.CATEGORY_NAME, categoryName);
		profileFragmentPagerAdapter.setAdminData(intent);

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	public void changeAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
		if (!isAdmin) {
			tvSaveRoom.setVisibility(View.GONE);
		}

		getMembers(-1, false);

		Intent intent = getIntent();
		intent.setAction(Const.IS_ADMIN);
		intent.putExtra(Const.IS_UPDATE_ADMIN, true);
		intent.getExtras().remove(Const.IS_ADMIN);
		intent.putExtra(Const.IS_ADMIN, isAdmin);
		profileFragmentPagerAdapter.setAdminData(intent);

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	@Override
	public void onChatMembersDBChanged(List<GlobalModel> usableData) {
		profileFragmentPagerAdapter.setMembers(usableData);
	}

}
