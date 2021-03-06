package com.clover.spika.enterprise.chat.models.greendao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import com.clover.spika.enterprise.chat.models.greendao.Message;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table MESSAGE.
*/
public class MessageDao extends AbstractDao<Message, Long> {

    public static final String TABLENAME = "MESSAGE";

    /**
     * Properties of entity Message.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "ID");
        public final static Property Chat_id = new Property(1, Long.class, "chat_id", false, "CHAT_ID");
        public final static Property User_id = new Property(2, Long.class, "user_id", false, "USER_ID");
        public final static Property Firstname = new Property(3, String.class, "firstname", false, "FIRSTNAME");
        public final static Property Lastname = new Property(4, String.class, "lastname", false, "LASTNAME");
        public final static Property Image = new Property(5, String.class, "image", false, "IMAGE");
        public final static Property Text = new Property(6, String.class, "text", false, "TEXT");
        public final static Property File_id = new Property(7, String.class, "file_id", false, "FILE_ID");
        public final static Property Thumb_id = new Property(8, String.class, "thumb_id", false, "THUMB_ID");
        public final static Property Longitude = new Property(9, String.class, "longitude", false, "LONGITUDE");
        public final static Property Latitude = new Property(10, String.class, "latitude", false, "LATITUDE");
        public final static Property Created = new Property(11, String.class, "created", false, "CREATED");
        public final static Property Modified = new Property(12, String.class, "modified", false, "MODIFIED");
        public final static Property Child_list = new Property(13, String.class, "child_list", false, "CHILD_LIST");
        public final static Property Image_thumb = new Property(14, String.class, "image_thumb", false, "IMAGE_THUMB");
        public final static Property Type = new Property(15, Integer.class, "type", false, "TYPE");
        public final static Property Root_id = new Property(16, Integer.class, "root_id", false, "ROOT_ID");
        public final static Property Parent_id = new Property(17, Integer.class, "parent_id", false, "PARENT_ID");
        public final static Property IsMe = new Property(18, Boolean.class, "isMe", false, "IS_ME");
        public final static Property IsFailed = new Property(19, Boolean.class, "isFailed", false, "IS_FAILED");
        public final static Property Attributes = new Property(20, String.class, "attributes", false, "ATTRIBUTES");
        public final static Property Country_code = new Property(21, String.class, "country_code", false, "COUNTRY_CODE");
        public final static Property Seen_timestamp = new Property(22, Integer.class, "seen_timestamp", false, "SEEN_TIMESTAMP");
        public final static Property ChatIdProperty = new Property(23, Long.class, "chatIdProperty", false, "CHAT_ID_PROPERTY");
    };

    private DaoSession daoSession;

    private Query<Message> chat_MessageListQuery;

    public MessageDao(DaoConfig config) {
        super(config);
    }
    
    public MessageDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'MESSAGE' (" + //
                "'ID' INTEGER PRIMARY KEY NOT NULL UNIQUE ," + // 0: id
                "'CHAT_ID' INTEGER," + // 1: chat_id
                "'USER_ID' INTEGER," + // 2: user_id
                "'FIRSTNAME' TEXT," + // 3: firstname
                "'LASTNAME' TEXT," + // 4: lastname
                "'IMAGE' TEXT," + // 5: image
                "'TEXT' TEXT," + // 6: text
                "'FILE_ID' TEXT," + // 7: file_id
                "'THUMB_ID' TEXT," + // 8: thumb_id
                "'LONGITUDE' TEXT," + // 9: longitude
                "'LATITUDE' TEXT," + // 10: latitude
                "'CREATED' TEXT," + // 11: created
                "'MODIFIED' TEXT," + // 12: modified
                "'CHILD_LIST' TEXT," + // 13: child_list
                "'IMAGE_THUMB' TEXT," + // 14: image_thumb
                "'TYPE' INTEGER," + // 15: type
                "'ROOT_ID' INTEGER," + // 16: root_id
                "'PARENT_ID' INTEGER," + // 17: parent_id
                "'IS_ME' INTEGER," + // 18: isMe
                "'IS_FAILED' INTEGER," + // 19: isFailed
                "'ATTRIBUTES' TEXT," + // 20: attributes
                "'COUNTRY_CODE' TEXT," + // 21: country_code
                "'SEEN_TIMESTAMP' INTEGER," + // 22: seen_timestamp
                "'CHAT_ID_PROPERTY' INTEGER);"); // 23: chatIdProperty
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'MESSAGE'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Message entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        Long chat_id = entity.getChat_id();
        if (chat_id != null) {
            stmt.bindLong(2, chat_id);
        }
 
        Long user_id = entity.getUser_id();
        if (user_id != null) {
            stmt.bindLong(3, user_id);
        }
 
        String firstname = entity.getFirstname();
        if (firstname != null) {
            stmt.bindString(4, firstname);
        }
 
        String lastname = entity.getLastname();
        if (lastname != null) {
            stmt.bindString(5, lastname);
        }
 
        String image = entity.getImage();
        if (image != null) {
            stmt.bindString(6, image);
        }
 
        String text = entity.getText();
        if (text != null) {
            stmt.bindString(7, text);
        }
 
        String file_id = entity.getFile_id();
        if (file_id != null) {
            stmt.bindString(8, file_id);
        }
 
        String thumb_id = entity.getThumb_id();
        if (thumb_id != null) {
            stmt.bindString(9, thumb_id);
        }
 
        String longitude = entity.getLongitude();
        if (longitude != null) {
            stmt.bindString(10, longitude);
        }
 
        String latitude = entity.getLatitude();
        if (latitude != null) {
            stmt.bindString(11, latitude);
        }
 
        String created = entity.getCreated();
        if (created != null) {
            stmt.bindString(12, created);
        }
 
        String modified = entity.getModified();
        if (modified != null) {
            stmt.bindString(13, modified);
        }
 
        String child_list = entity.getChild_list();
        if (child_list != null) {
            stmt.bindString(14, child_list);
        }
 
        String image_thumb = entity.getImage_thumb();
        if (image_thumb != null) {
            stmt.bindString(15, image_thumb);
        }
 
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(16, type);
        }
 
        Integer root_id = entity.getRoot_id();
        if (root_id != null) {
            stmt.bindLong(17, root_id);
        }
 
        Integer parent_id = entity.getParent_id();
        if (parent_id != null) {
            stmt.bindLong(18, parent_id);
        }
 
        Boolean isMe = entity.getIsMe();
        if (isMe != null) {
            stmt.bindLong(19, isMe ? 1l: 0l);
        }
 
        Boolean isFailed = entity.getIsFailed();
        if (isFailed != null) {
            stmt.bindLong(20, isFailed ? 1l: 0l);
        }
 
        String attributes = entity.getAttributes();
        if (attributes != null) {
            stmt.bindString(21, attributes);
        }
 
        String country_code = entity.getCountry_code();
        if (country_code != null) {
            stmt.bindString(22, country_code);
        }
 
        Integer seen_timestamp = entity.getSeen_timestamp();
        if (seen_timestamp != null) {
            stmt.bindLong(23, seen_timestamp);
        }
 
        Long chatIdProperty = entity.getChatIdProperty();
        if (chatIdProperty != null) {
            stmt.bindLong(24, chatIdProperty);
        }
    }

    @Override
    protected void attachEntity(Message entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Message readEntity(Cursor cursor, int offset) {
        Message entity = new Message( //
            cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // chat_id
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // user_id
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // firstname
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // lastname
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // image
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // text
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // file_id
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // thumb_id
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // longitude
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // latitude
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // created
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // modified
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // child_list
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // image_thumb
            cursor.isNull(offset + 15) ? null : cursor.getInt(offset + 15), // type
            cursor.isNull(offset + 16) ? null : cursor.getInt(offset + 16), // root_id
            cursor.isNull(offset + 17) ? null : cursor.getInt(offset + 17), // parent_id
            cursor.isNull(offset + 18) ? null : cursor.getShort(offset + 18) != 0, // isMe
            cursor.isNull(offset + 19) ? null : cursor.getShort(offset + 19) != 0, // isFailed
            cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20), // attributes
            cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21), // country_code
            cursor.isNull(offset + 22) ? null : cursor.getInt(offset + 22), // seen_timestamp
            cursor.isNull(offset + 23) ? null : cursor.getLong(offset + 23) // chatIdProperty
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Message entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setChat_id(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setUser_id(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setFirstname(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setLastname(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setImage(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setText(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setFile_id(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setThumb_id(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setLongitude(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setLatitude(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setCreated(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setModified(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setChild_list(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setImage_thumb(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setType(cursor.isNull(offset + 15) ? null : cursor.getInt(offset + 15));
        entity.setRoot_id(cursor.isNull(offset + 16) ? null : cursor.getInt(offset + 16));
        entity.setParent_id(cursor.isNull(offset + 17) ? null : cursor.getInt(offset + 17));
        entity.setIsMe(cursor.isNull(offset + 18) ? null : cursor.getShort(offset + 18) != 0);
        entity.setIsFailed(cursor.isNull(offset + 19) ? null : cursor.getShort(offset + 19) != 0);
        entity.setAttributes(cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20));
        entity.setCountry_code(cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21));
        entity.setSeen_timestamp(cursor.isNull(offset + 22) ? null : cursor.getInt(offset + 22));
        entity.setChatIdProperty(cursor.isNull(offset + 23) ? null : cursor.getLong(offset + 23));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Message entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Message entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "messageList" to-many relationship of Chat. */
    public List<Message> _queryChat_MessageList(Long chatIdProperty) {
        synchronized (this) {
            if (chat_MessageListQuery == null) {
                QueryBuilder<Message> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ChatIdProperty.eq(null));
                chat_MessageListQuery = queryBuilder.build();
            }
        }
        Query<Message> query = chat_MessageListQuery.forCurrentThread();
        query.setParameter(0, chatIdProperty);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getChatDao().getAllColumns());
            builder.append(" FROM MESSAGE T");
            builder.append(" LEFT JOIN CHAT T0 ON T.'CHAT_ID_PROPERTY'=T0.'ID'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Message loadCurrentDeep(Cursor cursor, boolean lock) {
        Message entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Chat chat = loadCurrentOther(daoSession.getChatDao(), cursor, offset);
        entity.setChat(chat);

        return entity;    
    }

    public Message loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<Message> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Message> list = new ArrayList<Message>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<Message> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Message> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
