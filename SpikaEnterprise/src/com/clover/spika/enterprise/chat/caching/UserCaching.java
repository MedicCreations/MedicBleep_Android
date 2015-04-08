package com.clover.spika.enterprise.chat.caching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.robospice.UserSpice;
import com.clover.spika.enterprise.chat.caching.utils.DaoUtils;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Organization;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UserDetail;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.models.greendao.OrganizationDao;
import com.clover.spika.enterprise.chat.models.greendao.UserDao;
import com.clover.spika.enterprise.chat.models.greendao.UserDetails;
import com.clover.spika.enterprise.chat.models.greendao.UserDetailsDao;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class UserCaching {

	/* start: Caching calls */
	public static UserWrapper GetProfile(final Activity activity, final SpiceManager spiceManager, String userId, final boolean getDetailValues,
			final OnUserGetDetailsDBChanged onDBChangeListener) {

		UserWrapper result = getDBData(activity, userId, new ArrayList<UserDetail>());

		UserSpice.GetProfile getProfile = new UserSpice.GetProfile(userId, getDetailValues, activity);
		spiceManager.execute(getProfile, new CustomSpiceListener<UserWrapper>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				super.onRequestFailure(ex);
				Utils.onFailedUniversal(null, activity);
			}

			@Override
			public void onRequestSuccess(UserWrapper result) {
				super.onRequestSuccess(result);

				if (result.getCode() == Const.API_SUCCESS) {

					HandleNewSearchData handleNewData = new HandleNewSearchData(activity, result, onDBChangeListener);
					spiceManager.execute(handleNewData, null);

				} else {
					String message = activity.getString(R.string.e_something_went_wrong);
					Utils.onFailedUniversal(message, activity, result.getCode(), false);
				}
			}
		});

		return result;
	}

	/* end: Caching calls */

	/* start: Interface callbacks */
	public interface OnUserGetDetailsDBChanged {
		public void onUserGetDetailsDBChanged(UserWrapper userWrapper);
	}

	/* end: Interface callbacks */

	/* start: HandleNewData */
	public static class HandleNewSearchData extends CustomSpiceRequest<Void> {

		private Activity activity;
		private UserWrapper userWrapper;
		private OnUserGetDetailsDBChanged onDBChangeListener;

		public HandleNewSearchData(Activity activity, UserWrapper userWrapper, OnUserGetDetailsDBChanged onDBChangeListener) {
			super(Void.class);

			this.activity = activity;
			this.userWrapper = userWrapper;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {

			handleNewData(activity, userWrapper);

			final UserWrapper finalData = getDBData(activity, String.valueOf(userWrapper.user.getId()), userWrapper.detail_values);

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (onDBChangeListener != null) {
						onDBChangeListener.onUserGetDetailsDBChanged(finalData);
					}
				}
			});

			return null;
		}
	}

	/* end: HandleNewData */

	/* start: Data handling */

	private static UserWrapper getDBData(Activity activity, String userId, List<UserDetail> wrapperData) {

		UserWrapper result = new UserWrapper();

		if (activity instanceof BaseActivity) {

			UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
			com.clover.spika.enterprise.chat.models.greendao.User daoUser = userDao.queryBuilder()
					.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(userId)).unique();

			if (daoUser != null) {

				result.user = DaoUtils.convertDaoUserToUserModel(daoUser);

				OrganizationDao organizationDao = ((BaseActivity) activity).getDaoSession().getOrganizationDao();
				com.clover.spika.enterprise.chat.models.greendao.Organization daoOrganization = organizationDao.queryBuilder()
						.where(com.clover.spika.enterprise.chat.models.greendao.OrganizationDao.Properties.Id.eq(daoUser.getOrganization_id())).unique();

				result.user.organization = DaoUtils.convertDaoOrganizationToOrganizationModel(daoOrganization);

				UserDetailsDao userDetailDao = ((BaseActivity) activity).getDaoSession().getUserDetailsDao();
				List<UserDetails> lista = userDetailDao.queryBuilder()
						.where(com.clover.spika.enterprise.chat.models.greendao.UserDetailsDao.Properties.User_id.eq(daoUser.getId())).build().list();

				List<Map<String, String>> details = new ArrayList<Map<String, String>>();

				if (lista != null) {

					for (UserDetails userDetails : lista) {

						Map<String, String> detailEntry = new HashMap<String, String>();
						com.clover.spika.enterprise.chat.models.UserDetail item = handleOldUserDetailsData(userDetails);

						if (item != null) {
							detailEntry.put(Const.PUBLIC, item.isPublicValue() ? "true" : "false");
							detailEntry.put(item.key, item.value);
							details.add(detailEntry);
						}
					}
				}

				result.user.setDetails(details);
				result.detail_values = wrapperData;
			}
		}

		return result;
	}

	private static com.clover.spika.enterprise.chat.models.UserDetail handleOldUserDetailsData(UserDetails userDetails) {
		return DaoUtils.convertDaoUserDetailToUserDetailModel(userDetails);
	}

	private static void handleNewData(Activity activity, UserWrapper networkData) {

		if (activity instanceof BaseActivity) {

			if (networkData.user != null) {

				UserDao userDao = ((BaseActivity) activity).getDaoSession().getUserDao();
				User user = networkData.user;

				if (userDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(user.getId())).count() > 0) {

					com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = userDao.queryBuilder()
							.where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(user.getId())).unique();
					finalUserModel = DaoUtils.convertUserModelToUserDao(finalUserModel, user);

					userDao.update(finalUserModel);

				} else {

					com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = DaoUtils.convertUserModelToUserDao(null, user);

					userDao.insert(finalUserModel);
				}
			}

			if (networkData.user.organization != null) {

				OrganizationDao organizationDao = ((BaseActivity) activity).getDaoSession().getOrganizationDao();
				Organization organization = networkData.user.organization;

				if (organizationDao.queryBuilder().where(com.clover.spika.enterprise.chat.models.greendao.OrganizationDao.Properties.Id.eq(Long.valueOf(organization.id))).count() > 0) {

					com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = organizationDao.queryBuilder()
							.where(com.clover.spika.enterprise.chat.models.greendao.OrganizationDao.Properties.Id.eq(Long.valueOf(organization.id))).unique();

					finalOrganizationModel = DaoUtils.convertOrganizationModelToOrganizationDao(finalOrganizationModel, organization);

					organizationDao.update(finalOrganizationModel);

				} else {

					com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = DaoUtils.convertOrganizationModelToOrganizationDao(null, organization);

					organizationDao.insert(finalOrganizationModel);
				}
			}

			if (networkData.user.getDetails() != null) {

				UserDetailsDao userDetailDao = ((BaseActivity) activity).getDaoSession().getUserDetailsDao();

				for (Map<String, String> networkDetail : networkData.user.getDetails()) {

					UserDetail currentDetailsValue = new UserDetail();

					for (Map.Entry<String, String> entry : networkDetail.entrySet()) {

						if (entry.getKey().equals(Const.PUBLIC)) {
							if (entry.getValue().equals("1") || entry.getValue().equals("true")) {
								currentDetailsValue.setPublicValue(true);
							} else {
								currentDetailsValue.setPublicValue(false);
							}
						} else {
							currentDetailsValue.setKey(entry.getKey());
							currentDetailsValue.setValue(entry.getValue());
						}
					}

					for (UserDetail item : networkData.detail_values) {
						if (item.key.equals(currentDetailsValue.key)) {
							currentDetailsValue.label = item.label;
							currentDetailsValue.keyboard_type = item.keyboard_type;
							break;
						}
					}

					if (userDetailDao
							.queryBuilder()
							.where(com.clover.spika.enterprise.chat.models.greendao.UserDetailsDao.Properties.User_id.eq(networkData.user.getId()),
									com.clover.spika.enterprise.chat.models.greendao.UserDetailsDao.Properties.Key.eq(currentDetailsValue.getKey())).count() > 0) {

						UserDetails userDetails = userDetailDao
								.queryBuilder()
								.where(com.clover.spika.enterprise.chat.models.greendao.UserDetailsDao.Properties.User_id.eq(networkData.user.getId()),
										com.clover.spika.enterprise.chat.models.greendao.UserDetailsDao.Properties.Key.eq(currentDetailsValue.getKey())).unique();

						userDetails.setPublic_value(currentDetailsValue.isPublicValue() ? 1 : 0);
						userDetails.setValue(currentDetailsValue.getValue());

						userDetailDao.update(userDetails);
					} else {

						UserDetails finalUserDetailsModel = new UserDetails();

						finalUserDetailsModel.setUser_id(networkData.user.getId());
						finalUserDetailsModel.setKey(currentDetailsValue.getKey());
						finalUserDetailsModel.setValue(currentDetailsValue.getValue());
						finalUserDetailsModel.setPublic_value(currentDetailsValue.isPublicValue() ? 1 : 0);

						userDetailDao.insert(finalUserDetailsModel);
					}
				}
			}
		}
	}

	/* end: Data handling */

}
