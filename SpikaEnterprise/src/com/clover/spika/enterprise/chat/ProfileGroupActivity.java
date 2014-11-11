package com.clover.spika.enterprise.chat;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.UsersApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.fragments.MembersFragment;
import com.clover.spika.enterprise.chat.fragments.ProfileGroupFragment;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UsersList;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;

public class ProfileGroupActivity extends BaseActivity implements OnPageChangeListener, OnClickListener, MembersFragment.Callbacks {

	ViewPager viewPager;
	ToggleButton profileTab;
	ToggleButton membersTab;
	
	UsersApi api;
	String chatId;
	ProfileFragmentPagerAdapter profileFragmentPagerAdapter;
	
	private boolean fromChatAct = false;
	private boolean updateImage = false;
	private String newImage = "";
	private String newThumbImage = "";
		
	public static void openProfile(Context context, String fileId, String chatName, String chatId, boolean isAdmin) {

		Intent intent = new Intent(context, ProfileGroupActivity.class);

		intent.putExtra(Const.IMAGE, fileId);
		intent.putExtra(Const.CHAT_NAME, chatName);
		intent.putExtra(Const.IS_ADMIN, isAdmin);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		context.startActivity(intent);
	}
	
	public static void openProfile(Context context, String fileId, String chatName, String chatId, boolean isAdmin, boolean fromChat, int isPrivate, String chatPassword) {

		Intent intent = new Intent(context, ProfileGroupActivity.class);

		intent.putExtra(Const.IMAGE, fileId);
		intent.putExtra(Const.CHAT_NAME, chatName);
		intent.putExtra(Const.IS_ADMIN, isAdmin);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.putExtra(Const.FROM_CHAT, fromChat);
		intent.putExtra(Const.IS_PRIVATE, isPrivate);
		intent.putExtra(Const.PASSWORD, chatPassword);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		context.startActivity(intent);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_group);

		api = new UsersApi();
		
		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		findViewById(R.id.saveRoomProfile).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveSettings();
			}
		});
		
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		
		profileFragmentPagerAdapter = new ProfileFragmentPagerAdapter();
		viewPager.setAdapter(profileFragmentPagerAdapter);
		viewPager.setOnPageChangeListener(this);

		profileTab = (ToggleButton) findViewById(R.id.profileTab);
		profileTab.setOnClickListener(this);
		membersTab = (ToggleButton) findViewById(R.id.membersTab);
		membersTab.setOnClickListener(this);
		
		chatId = getIntent().getExtras().getString(Const.CHAT_ID, "");
		
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
		
		public ProfileFragmentPagerAdapter() {
            super(getSupportFragmentManager());
			mFragmentList.add(new ProfileGroupFragment(getIntent()));
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

        public void setMembers(List<User> members) {
            for (Fragment fragment : mFragmentList) {
                if (fragment instanceof MembersFragment) {
                    ((MembersFragment) fragment).setMembers(members);
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
	
	@Override
    public void getMembers(int page,  final boolean toUpdateInviteMember) {
        api.getChatMembersWithPage(this, chatId, page, true, new ApiCallback<UsersList>() {
            @Override
            public void onApiResponse(Result<UsersList> result) {
                if (result.isSuccess()) {
                	profileFragmentPagerAdapter.setMemberTotalCount(result.getResultData().getTotalCount());
                	profileFragmentPagerAdapter.setMembers(result.getResultData().getMembersList());
                }
            }
        });
    }
	
	public void setChangeImage(String image, String imageThumb){
		newImage = image;
		newThumbImage = imageThumb;
		updateImage = true;
	}
	
	@Override
	public void finish() {
		if(fromChatAct && updateImage && !TextUtils.isEmpty(newImage)){
			Intent chat = new Intent(this, ChatActivity.class);
			chat.putExtra(Const.IMAGE, newImage);
			chat.putExtra(Const.IMAGE_THUMB, newThumbImage);
			chat.putExtra(Const.UPDATE_PICTURE, true);
			chat.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(chat);
		}
		super.finish();
	}
	
	public void saveSettings(){
		
		HashMap<String, String> requestParams = new HashMap<String, String>();
		
		Switch switchPrivate = (Switch) findViewById(R.id.switch_private_room);
		
		RobotoRegularTextView tvPassword = (RobotoRegularTextView) findViewById(R.id.tvPassword);
		String newPassword = tvPassword.getText().toString();
		
		if (!newPassword.equals("")){
			byte[] digest = null;
			try {
				digest = MessageDigest.getInstance("MD5").digest(newPassword.getBytes("UTF-8"));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	        String hashPassword = Utils.convertByteArrayToHexString(digest);
	        
	        requestParams.put(Const.PASSWORD, hashPassword);
		}
		
		requestParams.put(Const.CHAT_ID, chatId);
		requestParams.put(Const.IS_PRIVATE, switchPrivate.isChecked()? "1" : "0");
		
		
		new ChatApi().updateChatAll(requestParams, true, this, new ApiCallback<BaseModel>() {

			@Override
			public void onApiResponse(Result<BaseModel> result) {
				if (result.isSuccess()) {
					finish();
				} else {
					AppDialog dialog = new AppDialog(ProfileGroupActivity.this, false);
					dialog.setFailed(null);
				}
			}
		});	
		
	}
	
}
