package com.clover.spika.enterprise.chat.extendables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.GroupListActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.fragments.SidebarFragment;
import com.clover.spika.enterprise.chat.models.Push;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class BaseActivity extends SlidingFragmentActivity {

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	public static BaseActivity instance = null;

	List<Push> qPush = new ArrayList<Push>();
	boolean isPushShowing = false;

	public GoogleCloudMessaging gcm;

	public ImageView tabGames;
	public ImageView tabTalk;
	public ImageView tabFriends;
	public ImageView tabSettings;

	public final static int slidingDuration = 160;

	private SlidingMenu slidingMenu;
	private ImageButton sidebarBtn;
	
	private ImageButton searchBtn;
	private TextView screenTitle;
	private EditText searchEt;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setBehindContentView(R.layout.sidebar_layout_empty);

		// set the Behind View Fragment
		getFragmentManager().beginTransaction().replace(R.id.emptyLayout, new SidebarFragment()).commit();

		slidingMenu = getSlidingMenu();
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setBehindScrollScale(0.35f);
		slidingMenu.setShadowDrawable(null);
		slidingMenu.setFadeDegree(0.35f);
		// Value 950 is not used, library method has been changed
		slidingMenu.setBehindWidth(80);
	}

	@Override
	public void setContentView(int id) {
		super.setContentView(id);

		sidebarBtn = (ImageButton) findViewById(R.id.sidebarBtn);

		if (sidebarBtn != null) {
			sidebarBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					slidingMenu.toggle(true);
				}
			});
		}
	}
	
	public void setSearch(){
		searchBtn = (ImageButton) findViewById(R.id.searchBtn);
		searchEt = (EditText) findViewById(R.id.searchEt);
		screenTitle = (TextView) findViewById(R.id.screenTitle);
		if(searchBtn == null || searchEt == null) return;
		
		searchBtn.setOnClickListener(searchOnClickListener);
	}
	
	private OnClickListener searchOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(searchEt.getVisibility() == View.GONE){
				//open search view
			}else {
				//search  
			}
		}
	};
	
	private void openSearchAnimation(){
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		instance = this;
	}

	@Override
	protected void onPause() {
		super.onPause();
		instance = null;
	}

	public static BaseActivity getInstance() {
		return instance;
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	public boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				finish();
			}
			return false;
		}
		return true;
	}

	public String getPushToken() {

		String regId = "";

		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regId = getRegistrationId(this);

			Logger.info("PUSH_TOKEN: " + regId);

			if (regId.isEmpty()) {
				registerInBackground();
			}

			return regId;
		} else {
			Logger.info("Google Play Services are missing");

			return null;
		}
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	public String getRegistrationId(Context context) {
		String registrationId = SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.REGISTRATION_ID);
		if (registrationId == null || registrationId.isEmpty()) {
			Logger.info("GCM registration ID not found");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		if (Helper.isUpdated(context)) {
			Logger.info("App has been updated, we need to register GCM again.");
			return "";
		}

		return registrationId;
	}

	public void showPopUp(final String msg, final String groupId, final int type) {

		if (type == Const.PT_MESSAGE) {
			SpikaEnterpriseApp.getSharedPreferences(this).setCustomBoolean(groupId, true);
		}

		if (isPushShowing) {
			Push push = new Push();
			push.setId(groupId);
			push.setMessage(msg);

			qPush.add(push);

			return;
		}

		new BaseAsyncTask<Void, Void, Integer>(this, false) {

			protected void onPreExecute() {
				isPushShowing = true;
			};

			protected Integer doInBackground(Void... paramss) {

				return Const.E_FAILED;
			};

			protected void onPostExecute(Integer result) {
				ViewGroup contentRoot = ((ViewGroup) findViewById(android.R.id.content).getRootView());
				final View view = LayoutInflater.from(context).inflate(R.layout.in_app_notification_layout, contentRoot);
				TextView text = (TextView) view.findViewById(R.id.msgPop);
				text.setText(msg);

				int pix = 0;
				int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
				if (resourceId > 0) {
					pix = getResources().getDimensionPixelSize(resourceId);
				}

				final RelativeLayout notificationLayout = (RelativeLayout) view.findViewById(R.id.notificationLayout);

				View paddingTopView = (View) view.findViewById(R.id.paddingTopView);
				RelativeLayout.LayoutParams paramsV = (RelativeLayout.LayoutParams) paddingTopView.getLayoutParams();
				paramsV.height = pix;
				paddingTopView.setLayoutParams(paramsV);

				final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

				contentRoot.addView(view, params);

				final Handler handler = new Handler();

				final Animation notAnimIn = AnimationUtils.loadAnimation(context, R.anim.in_app_notification_anim_in);
				final Animation notAnimOut = AnimationUtils.loadAnimation(context, R.anim.in_app_notification_anim_out);

				notAnimIn.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						notificationLayout.setVisibility(View.VISIBLE);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								view.startAnimation(notAnimOut);
							}
						}, 3000);
					}
				});

				notAnimOut.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						((ViewGroup) findViewById(android.R.id.content).getRootView()).removeView(view);
						isPushShowing = false;

						if (qPush.size() > 0) {
							showPopUp(qPush.get(0).getMessage(), qPush.get(0).getId(), type);
							qPush.remove(0);
						}
					}
				});

				view.startAnimation(notAnimIn);

				if (GroupListActivity.instance != null && GroupListActivity.instance.adapter != null) {
					if (type == Const.PT_MESSAGE) {
						GroupListActivity.instance.adapter.notifyDataSetChanged();
					} else if (type == Const.PT_GROUP_CREATED) {
						GroupListActivity.instance.getGroup(0, null);
					}
				}
			};
		}.execute();
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	// TODO
	public void registerInBackground() {
		new BaseAsyncTask<Void, Void, String>(this, false) {

			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					String regId = gcm.register(Const.GCM_SENDER_ID);
					msg = "Device registered, registration ID=" + regId;

					// You should send the registration ID to your server over
					// HTTP,
					// so it can use GCM/HTTP or CCS to send messages to your
					// app.
					// The request to your server should be authenticated if
					// your app
					// is using accounts.
					// sendRegistrationIdToBackend();
					// TODO
					// try {
					// HashMap<String, String> postParams = new HashMap<String,
					// String>();
					// postParams.put(Const.PUSH_TOKEN, regId);
					//
					// JSONObject result =
					// Helper.jObjectFromString(NetworkManagement.httpPostRequest(Api.SET_PUSH_ID,
					// postParams));
					//
					// if (result != null) {
					// } else {
					// }
					//
					// } catch (Exception e) {
					// e.printStackTrace();
					// }

					storeRegistrationId(context, regId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}

				return msg;
			};
		}.execute();
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		Helper.updateAppVersion(context);
		SpikaEnterpriseApp.getSharedPreferences(this).setCustomString(Const.REGISTRATION_ID, regId);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	public void hideKeyboard(EditText et) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
	}

	public void showKeyboard(EditText et) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(et, 0);
	}

}
