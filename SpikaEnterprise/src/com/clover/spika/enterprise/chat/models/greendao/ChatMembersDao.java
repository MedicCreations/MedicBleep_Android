package com.clover.spika.enterprise.chat.models.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.clover.spika.enterprise.chat.models.greendao.ChatMembers;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table CHAT_MEMBERS.
*/
public class ChatMembersDao extends AbstractDao<ChatMembers, Long> {

    public static final String TABLENAME = "CHAT_MEMBERS";

    /**
     * Properties of entity ChatMembers.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "ID");
        public final static Property ChatMembers = new Property(1, String.class, "chatMembers", false, "CHAT_MEMBERS");
    };


    public ChatMembersDao(DaoConfig config) {
        super(config);
    }
    
    public ChatMembersDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'CHAT_MEMBERS' (" + //
                "'ID' INTEGER PRIMARY KEY NOT NULL UNIQUE ," + // 0: id
                "'CHAT_MEMBERS' TEXT);"); // 1: chatMembers
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'CHAT_MEMBERS'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, ChatMembers entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String chatMembers = entity.getChatMembers();
        if (chatMembers != null) {
            stmt.bindString(2, chatMembers);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public ChatMembers readEntity(Cursor cursor, int offset) {
        ChatMembers entity = new ChatMembers( //
            cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1) // chatMembers
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, ChatMembers entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setChatMembers(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(ChatMembers entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(ChatMembers entity) {
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
    
}
