package com.clover.spika.enterprise.chat.models.greendao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.clover.spika.enterprise.chat.models.greendao.Category;
import com.clover.spika.enterprise.chat.models.greendao.Group;
import com.clover.spika.enterprise.chat.models.greendao.Organization;
import com.clover.spika.enterprise.chat.models.greendao.ListUserDetails;
import com.clover.spika.enterprise.chat.models.greendao.MapKeyValueUserDetails;
import com.clover.spika.enterprise.chat.models.greendao.User;
import com.clover.spika.enterprise.chat.models.greendao.Message;
import com.clover.spika.enterprise.chat.models.greendao.Chat;

import com.clover.spika.enterprise.chat.models.greendao.CategoryDao;
import com.clover.spika.enterprise.chat.models.greendao.GroupDao;
import com.clover.spika.enterprise.chat.models.greendao.OrganizationDao;
import com.clover.spika.enterprise.chat.models.greendao.ListUserDetailsDao;
import com.clover.spika.enterprise.chat.models.greendao.MapKeyValueUserDetailsDao;
import com.clover.spika.enterprise.chat.models.greendao.UserDao;
import com.clover.spika.enterprise.chat.models.greendao.MessageDao;
import com.clover.spika.enterprise.chat.models.greendao.ChatDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig categoryDaoConfig;
    private final DaoConfig groupDaoConfig;
    private final DaoConfig organizationDaoConfig;
    private final DaoConfig listUserDetailsDaoConfig;
    private final DaoConfig mapKeyValueUserDetailsDaoConfig;
    private final DaoConfig userDaoConfig;
    private final DaoConfig messageDaoConfig;
    private final DaoConfig chatDaoConfig;

    private final CategoryDao categoryDao;
    private final GroupDao groupDao;
    private final OrganizationDao organizationDao;
    private final ListUserDetailsDao listUserDetailsDao;
    private final MapKeyValueUserDetailsDao mapKeyValueUserDetailsDao;
    private final UserDao userDao;
    private final MessageDao messageDao;
    private final ChatDao chatDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        categoryDaoConfig = daoConfigMap.get(CategoryDao.class).clone();
        categoryDaoConfig.initIdentityScope(type);

        groupDaoConfig = daoConfigMap.get(GroupDao.class).clone();
        groupDaoConfig.initIdentityScope(type);

        organizationDaoConfig = daoConfigMap.get(OrganizationDao.class).clone();
        organizationDaoConfig.initIdentityScope(type);

        listUserDetailsDaoConfig = daoConfigMap.get(ListUserDetailsDao.class).clone();
        listUserDetailsDaoConfig.initIdentityScope(type);

        mapKeyValueUserDetailsDaoConfig = daoConfigMap.get(MapKeyValueUserDetailsDao.class).clone();
        mapKeyValueUserDetailsDaoConfig.initIdentityScope(type);

        userDaoConfig = daoConfigMap.get(UserDao.class).clone();
        userDaoConfig.initIdentityScope(type);

        messageDaoConfig = daoConfigMap.get(MessageDao.class).clone();
        messageDaoConfig.initIdentityScope(type);

        chatDaoConfig = daoConfigMap.get(ChatDao.class).clone();
        chatDaoConfig.initIdentityScope(type);

        categoryDao = new CategoryDao(categoryDaoConfig, this);
        groupDao = new GroupDao(groupDaoConfig, this);
        organizationDao = new OrganizationDao(organizationDaoConfig, this);
        listUserDetailsDao = new ListUserDetailsDao(listUserDetailsDaoConfig, this);
        mapKeyValueUserDetailsDao = new MapKeyValueUserDetailsDao(mapKeyValueUserDetailsDaoConfig, this);
        userDao = new UserDao(userDaoConfig, this);
        messageDao = new MessageDao(messageDaoConfig, this);
        chatDao = new ChatDao(chatDaoConfig, this);

        registerDao(Category.class, categoryDao);
        registerDao(Group.class, groupDao);
        registerDao(Organization.class, organizationDao);
        registerDao(ListUserDetails.class, listUserDetailsDao);
        registerDao(MapKeyValueUserDetails.class, mapKeyValueUserDetailsDao);
        registerDao(User.class, userDao);
        registerDao(Message.class, messageDao);
        registerDao(Chat.class, chatDao);
    }
    
    public void clear() {
        categoryDaoConfig.getIdentityScope().clear();
        groupDaoConfig.getIdentityScope().clear();
        organizationDaoConfig.getIdentityScope().clear();
        listUserDetailsDaoConfig.getIdentityScope().clear();
        mapKeyValueUserDetailsDaoConfig.getIdentityScope().clear();
        userDaoConfig.getIdentityScope().clear();
        messageDaoConfig.getIdentityScope().clear();
        chatDaoConfig.getIdentityScope().clear();
    }

    public CategoryDao getCategoryDao() {
        return categoryDao;
    }

    public GroupDao getGroupDao() {
        return groupDao;
    }

    public OrganizationDao getOrganizationDao() {
        return organizationDao;
    }

    public ListUserDetailsDao getListUserDetailsDao() {
        return listUserDetailsDao;
    }

    public MapKeyValueUserDetailsDao getMapKeyValueUserDetailsDao() {
        return mapKeyValueUserDetailsDao;
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

}
