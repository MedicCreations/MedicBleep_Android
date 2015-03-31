package com.clover.spika.enterprise.chat.models.greendao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.clover.spika.enterprise.chat.models.greendao.Category;
import com.clover.spika.enterprise.chat.models.greendao.Groups;
import com.clover.spika.enterprise.chat.models.greendao.Organization;
import com.clover.spika.enterprise.chat.models.greendao.User;
import com.clover.spika.enterprise.chat.models.greendao.Message;
import com.clover.spika.enterprise.chat.models.greendao.Chat;
import com.clover.spika.enterprise.chat.models.greendao.Stickers;
import com.clover.spika.enterprise.chat.models.greendao.UserDetails;

import com.clover.spika.enterprise.chat.models.greendao.CategoryDao;
import com.clover.spika.enterprise.chat.models.greendao.GroupsDao;
import com.clover.spika.enterprise.chat.models.greendao.OrganizationDao;
import com.clover.spika.enterprise.chat.models.greendao.UserDao;
import com.clover.spika.enterprise.chat.models.greendao.MessageDao;
import com.clover.spika.enterprise.chat.models.greendao.ChatDao;
import com.clover.spika.enterprise.chat.models.greendao.StickersDao;
import com.clover.spika.enterprise.chat.models.greendao.UserDetailsDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig categoryDaoConfig;
    private final DaoConfig groupsDaoConfig;
    private final DaoConfig organizationDaoConfig;
    private final DaoConfig userDaoConfig;
    private final DaoConfig messageDaoConfig;
    private final DaoConfig chatDaoConfig;
    private final DaoConfig stickersDaoConfig;
    private final DaoConfig userDetailsDaoConfig;

    private final CategoryDao categoryDao;
    private final GroupsDao groupsDao;
    private final OrganizationDao organizationDao;
    private final UserDao userDao;
    private final MessageDao messageDao;
    private final ChatDao chatDao;
    private final StickersDao stickersDao;
    private final UserDetailsDao userDetailsDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        categoryDaoConfig = daoConfigMap.get(CategoryDao.class).clone();
        categoryDaoConfig.initIdentityScope(type);

        groupsDaoConfig = daoConfigMap.get(GroupsDao.class).clone();
        groupsDaoConfig.initIdentityScope(type);

        organizationDaoConfig = daoConfigMap.get(OrganizationDao.class).clone();
        organizationDaoConfig.initIdentityScope(type);

        userDaoConfig = daoConfigMap.get(UserDao.class).clone();
        userDaoConfig.initIdentityScope(type);

        messageDaoConfig = daoConfigMap.get(MessageDao.class).clone();
        messageDaoConfig.initIdentityScope(type);

        chatDaoConfig = daoConfigMap.get(ChatDao.class).clone();
        chatDaoConfig.initIdentityScope(type);

        stickersDaoConfig = daoConfigMap.get(StickersDao.class).clone();
        stickersDaoConfig.initIdentityScope(type);

        userDetailsDaoConfig = daoConfigMap.get(UserDetailsDao.class).clone();
        userDetailsDaoConfig.initIdentityScope(type);

        categoryDao = new CategoryDao(categoryDaoConfig, this);
        groupsDao = new GroupsDao(groupsDaoConfig, this);
        organizationDao = new OrganizationDao(organizationDaoConfig, this);
        userDao = new UserDao(userDaoConfig, this);
        messageDao = new MessageDao(messageDaoConfig, this);
        chatDao = new ChatDao(chatDaoConfig, this);
        stickersDao = new StickersDao(stickersDaoConfig, this);
        userDetailsDao = new UserDetailsDao(userDetailsDaoConfig, this);

        registerDao(Category.class, categoryDao);
        registerDao(Groups.class, groupsDao);
        registerDao(Organization.class, organizationDao);
        registerDao(User.class, userDao);
        registerDao(Message.class, messageDao);
        registerDao(Chat.class, chatDao);
        registerDao(Stickers.class, stickersDao);
        registerDao(UserDetails.class, userDetailsDao);
    }
    
    public void clear() {
        categoryDaoConfig.getIdentityScope().clear();
        groupsDaoConfig.getIdentityScope().clear();
        organizationDaoConfig.getIdentityScope().clear();
        userDaoConfig.getIdentityScope().clear();
        messageDaoConfig.getIdentityScope().clear();
        chatDaoConfig.getIdentityScope().clear();
        stickersDaoConfig.getIdentityScope().clear();
        userDetailsDaoConfig.getIdentityScope().clear();
    }

    public CategoryDao getCategoryDao() {
        return categoryDao;
    }

    public GroupsDao getGroupsDao() {
        return groupsDao;
    }

    public OrganizationDao getOrganizationDao() {
        return organizationDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public MessageDao getMessageDao() {
        return messageDao;
    }

    public ChatDao getChatDao() {
        return chatDao;
    }

    public StickersDao getStickersDao() {
        return stickersDao;
    }

    public UserDetailsDao getUserDetailsDao() {
        return userDetailsDao;
    }

}
