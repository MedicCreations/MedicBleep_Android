package com.clover.spika.enterprise.chat.caching;

import com.clover.spika.enterprise.chat.api.robospice.BackgroundDataChatSpice;
import com.clover.spika.enterprise.chat.caching.utils.DaoUtils;
import com.clover.spika.enterprise.chat.models.GetBackroundDataResponse;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.greendao.CategoryDao;
import com.clover.spika.enterprise.chat.models.greendao.ChatDao;
import com.clover.spika.enterprise.chat.models.greendao.ChatDao.Properties;
import com.clover.spika.enterprise.chat.models.greendao.DaoSession;
import com.clover.spika.enterprise.chat.models.greendao.MessageDao;
import com.clover.spika.enterprise.chat.models.greendao.OrganizationDao;
import com.clover.spika.enterprise.chat.models.greendao.UserDao;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.octo.android.robospice.SpiceManager;

public class BackgroundChatCaching {

	public static Integer getData(final DaoSession daoSession, final SpiceManager spiceManager, final String chatId, String msgId, 
			final OnChatDBChanged onDBChangeListener) {
		
		BackgroundDataChatSpice.GetMessages spice = new BackgroundDataChatSpice.GetMessages(chatId, msgId);
		spiceManager.execute(spice, new CustomSpiceListener<GetBackroundDataResponse>(){
			
			@Override
			public void onRequestSuccess(GetBackroundDataResponse result) {
				super.onRequestSuccess(result);
				
				if (result.getCode() == Const.API_SUCCESS) {

					HandleNewData handleNewData = new HandleNewData(daoSession, result, onDBChangeListener);
					spiceManager.execute(handleNewData, null);

				} 
				
			}
			
		});
		
		return 1;
	}
	
	public static class HandleNewData extends CustomSpiceRequest<Void> {

		private DaoSession daoSession;
		private GetBackroundDataResponse result;
		private OnChatDBChanged onDBChangeListener;

		public HandleNewData(DaoSession daoSession, GetBackroundDataResponse res, OnChatDBChanged onDBChangeListener) {
			super(Void.class);

			this.daoSession = daoSession;
			this.result = res;
			this.onDBChangeListener = onDBChangeListener;
		}

		@Override
		public Void loadDataFromNetwork() throws Exception {

			/* if chat.messages == null or size == 0, don't save chat in database */
			if (result.messages != null && result.messages.size() > 0) {
				handleNewData(daoSession, result);
			} else {
				// Log.d("LOG", "dont save chat to database");
			}
			
			if (onDBChangeListener != null) {
				onDBChangeListener.onChatDBChanged(result);
			}

			return null;
		}
	}
	
	private static void handleNewData(DaoSession daoSession, GetBackroundDataResponse networkData) {
		
		if (daoSession != null) {

            CategoryDao categoryDao = daoSession.getCategoryDao();
            OrganizationDao organizationDao = daoSession.getOrganizationDao();
            UserDao userDao = daoSession.getUserDao();
            MessageDao messageDao = daoSession.getMessageDao();
            ChatDao chatDao = daoSession.getChatDao();

            Long finalCategoryModelId = 0L;
            if (networkData.chat != null && networkData.chat.category != null && networkData.chat.category.id != null && networkData.chat.category.name != null) {

                if (categoryDao.queryBuilder()
                        .where(com.clover.spika.enterprise.chat.models.greendao.CategoryDao.Properties.Id.eq(networkData.chat.category.id)).count() > 0) {

                    com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = categoryDao.queryBuilder()
                            .where(com.clover.spika.enterprise.chat.models.greendao.CategoryDao.Properties.Id.eq(networkData.chat.category.id))
                            .unique();

                    categoryDao.update(finalCategoryModel);
                    finalCategoryModelId = finalCategoryModel.getId();

                } else {

                    com.clover.spika.enterprise.chat.models.greendao.Category finalCategoryModel = DaoUtils.convertCategoryModelToCategoryDao(null,
                            networkData.chat.category);

                    categoryDao.insert(finalCategoryModel);
                    finalCategoryModelId = finalCategoryModel.getId();
                }
            }

            Long finalUserModelId = 0L;
            if (networkData.user != null) {

                if (networkData.user.organization != null) {

                    if (organizationDao
                            .queryBuilder()
                            .where(com.clover.spika.enterprise.chat.models.greendao.OrganizationDao.Properties.Id
                                    .eq(networkData.user.organization.id)).count() > 0) {

                        com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = organizationDao
                                .queryBuilder()
                                .where(com.clover.spika.enterprise.chat.models.greendao.OrganizationDao.Properties.Id
                                        .eq(networkData.user.organization.id)).unique();
                        finalOrganizationModel = DaoUtils.convertOrganizationModelToOrganizationDao(finalOrganizationModel,
                                networkData.user.organization);

                        organizationDao.update(finalOrganizationModel);

                    } else {
                        com.clover.spika.enterprise.chat.models.greendao.Organization finalOrganizationModel = DaoUtils
                                .convertOrganizationModelToOrganizationDao(null, networkData.user.organization);
                        organizationDao.insert(finalOrganizationModel);
                    }
                }

                if (userDao.queryBuilder()
                        .where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(networkData.user.id)).count() > 0) {

                    com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = userDao.queryBuilder()
                            .where(com.clover.spika.enterprise.chat.models.greendao.UserDao.Properties.Id.eq(networkData.user.id))
                            .unique();
                    finalUserModel = DaoUtils.convertUserModelToUserDao(finalUserModel, networkData.user);

                    userDao.update(finalUserModel);
                    finalUserModelId = finalUserModel.getId();

                } else {

                    com.clover.spika.enterprise.chat.models.greendao.User finalUserModel = DaoUtils.convertUserModelToUserDao(null, networkData.user);

                    userDao.insert(finalUserModel);
                    finalUserModelId = finalUserModel.getId();
                }
            }

            for (Message mess : networkData.messages) {

                long chatId = 0l;
                if(networkData.chat != null){
                    chatId = (long) networkData.chat.getId();
                }else{
                    chatId = Long.valueOf(mess.chat_id);
                }

                com.clover.spika.enterprise.chat.models.greendao.Message finalMessageModel = new com.clover.spika.enterprise.chat.models.greendao.Message(
                        Long.valueOf(mess.id), Long.valueOf(mess.chat_id), Long.valueOf(mess.user_id), mess.firstname, mess.lastname, mess.image,
                        mess.text, mess.file_id, mess.thumb_id, mess.longitude, mess.latitude, mess.created, mess.modified, mess.child_list,
                        mess.image_thumb, mess.type, mess.root_id, mess.parent_id, mess.isMe, mess.isFailed, mess.attributes, mess.country_code, mess.seen_timestamp, chatId);

                messageDao.insertOrReplace(finalMessageModel);
            }

            if(networkData.chat != null){
                if (chatDao.queryBuilder().where(Properties.Id.eq(networkData.chat.getId())).count() > 0) {

                    com.clover.spika.enterprise.chat.models.greendao.Chat usedChatModel = chatDao.queryBuilder()
                            .where(Properties.Id.eq(networkData.chat.getId())).unique();

                    usedChatModel.setId(Long.valueOf(networkData.chat.getId()));
                    if (networkData.chat.chat_name != null)
                        usedChatModel.setChat_name(networkData.chat.chat_name);
                    if (networkData.chat.seen_by != null)
                        usedChatModel.setSeen_by(networkData.chat.seen_by);
                    if ((Integer) networkData.total_count != null)
                        usedChatModel.setTotal_count(networkData.total_count);
                    if (networkData.chat.image_thumb != null)
                        usedChatModel.setImage_thumb(networkData.chat.image_thumb);
                    if (networkData.chat.image != null)
                        usedChatModel.setImage(networkData.chat.image);
                    if (networkData.chat.admin_id != null)
                        usedChatModel.setAdmin_id(networkData.chat.admin_id);
                    if ((Integer) networkData.chat.is_active != null)
                        usedChatModel.setIs_active(networkData.chat.is_active);
                    if ((Integer) networkData.chat.type != null)
                        usedChatModel.setType(networkData.chat.type);
                    if ((Integer) networkData.chat.is_private != null)
                        usedChatModel.setIs_private(networkData.chat.is_private);
                    if (networkData.chat.password != null)
                        usedChatModel.setPassword(networkData.chat.password);
                    if ((Integer) networkData.chat.is_member != null)
                        usedChatModel.setIs_member(networkData.chat.is_member);
                    if ((Long) networkData.chat.modified != null)
                        usedChatModel.setModified(networkData.chat.modified);

                    if (finalCategoryModelId != 0) {
                        usedChatModel.setCategoryId(finalCategoryModelId);
                    }
                    if (finalUserModelId != 0) {
                        usedChatModel.setUserIdProperty(finalUserModelId);
                    }
                    usedChatModel.setUnread("0");

                    chatDao.update(usedChatModel);
                } else {

                    com.clover.spika.enterprise.chat.models.greendao.Chat finalChatModel = new com.clover.spika.enterprise.chat.models.greendao.Chat(
                            Long.valueOf(networkData.chat.getId()), networkData.chat.chat_name, networkData.chat.seen_by, networkData.total_count,
                            networkData.chat.image_thumb, networkData.chat.image, networkData.chat.admin_id, networkData.chat.is_active, networkData.chat.type,
                            networkData.chat.is_private, networkData.chat.password, networkData.chat.unread, networkData.chat.is_member,
                            networkData.chat.modified, true, finalCategoryModelId, finalUserModelId, 0L);
                    chatDao.insert(finalChatModel);
                }
            }
			
		}
	}

	public interface OnChatDBChanged {
		public void onChatDBChanged(GetBackroundDataResponse response);
	}

}
