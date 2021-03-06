package com.clover.spika.enterprise.chat.models.greendao;

import com.clover.spika.enterprise.chat.models.greendao.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table MESSAGE.
 */
public class Message {

    private long id;
    private Long chat_id;
    private Long user_id;
    private String firstname;
    private String lastname;
    private String image;
    private String text;
    private String file_id;
    private String thumb_id;
    private String longitude;
    private String latitude;
    private String created;
    private String modified;
    private String child_list;
    private String image_thumb;
    private Integer type;
    private Integer root_id;
    private Integer parent_id;
    private Boolean isMe;
    private Boolean isFailed;
    private String attributes;
    private Long chatIdProperty;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient MessageDao myDao;

    private Chat chat;
    private Long chat__resolvedKey;


    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Message() {
    }

    public Message(long id) {
        this.id = id;
    }

    public Message(long id, Long chat_id, Long user_id, String firstname, String lastname, String image, String text, String file_id, String thumb_id, String longitude, String latitude, String created, String modified, String child_list, String image_thumb, Integer type, Integer root_id, Integer parent_id, Boolean isMe, Boolean isFailed, String attributes, Long chatIdProperty) {
        this.id = id;
        this.chat_id = chat_id;
        this.user_id = user_id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.image = image;
        this.text = text;
        this.file_id = file_id;
        this.thumb_id = thumb_id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.created = created;
        this.modified = modified;
        this.child_list = child_list;
        this.image_thumb = image_thumb;
        this.type = type;
        this.root_id = root_id;
        this.parent_id = parent_id;
        this.isMe = isMe;
        this.isFailed = isFailed;
        this.attributes = attributes;
        this.chatIdProperty = chatIdProperty;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMessageDao() : null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getChat_id() {
        return chat_id;
    }

    public void setChat_id(Long chat_id) {
        this.chat_id = chat_id;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFile_id() {
        return file_id;
    }

    public void setFile_id(String file_id) {
        this.file_id = file_id;
    }

    public String getThumb_id() {
        return thumb_id;
    }

    public void setThumb_id(String thumb_id) {
        this.thumb_id = thumb_id;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getChild_list() {
        return child_list;
    }

    public void setChild_list(String child_list) {
        this.child_list = child_list;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getRoot_id() {
        return root_id;
    }

    public void setRoot_id(Integer root_id) {
        this.root_id = root_id;
    }

    public Integer getParent_id() {
        return parent_id;
    }

    public void setParent_id(Integer parent_id) {
        this.parent_id = parent_id;
    }

    public Boolean getIsMe() {
        return isMe;
    }

    public void setIsMe(Boolean isMe) {
        this.isMe = isMe;
    }

    public Boolean getIsFailed() {
        return isFailed;
    }

    public void setIsFailed(Boolean isFailed) {
        this.isFailed = isFailed;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public Long getChatIdProperty() {
        return chatIdProperty;
    }

    public void setChatIdProperty(Long chatIdProperty) {
        this.chatIdProperty = chatIdProperty;
    }

    /** To-one relationship, resolved on first access. */
    public Chat getChat() {
        Long __key = this.chatIdProperty;
        if (chat__resolvedKey == null || !chat__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ChatDao targetDao = daoSession.getChatDao();
            Chat chatNew = targetDao.load(__key);
            synchronized (this) {
                chat = chatNew;
            	chat__resolvedKey = __key;
            }
        }
        return chat;
    }

    public void setChat(Chat chat) {
        synchronized (this) {
            this.chat = chat;
            chatIdProperty = chat == null ? null : chat.getId();
            chat__resolvedKey = chatIdProperty;
        }
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
