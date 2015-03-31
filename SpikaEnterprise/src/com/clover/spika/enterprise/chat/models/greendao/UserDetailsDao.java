package com.clover.spika.enterprise.chat.models.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.clover.spika.enterprise.chat.models.greendao.UserDetails;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table USER_DETAILS.
*/
public class UserDetailsDao extends AbstractDao<UserDetails, Long> {

    public static final String TABLENAME = "USER_DETAILS";

    /**
     * Properties of entity UserDetails.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property User_id = new Property(1, long.class, "user_id", false, "USER_ID");
        public final static Property Key = new Property(2, String.class, "key", false, "KEY");
        public final static Property Label = new Property(3, String.class, "label", false, "LABEL");
        public final static Property Keyboard_type = new Property(4, Integer.class, "keyboard_type", false, "KEYBOARD_TYPE");
        public final static Property Value = new Property(5, String.class, "value", false, "VALUE");
        public final static Property Public_value = new Property(6, Integer.class, "public_value", false, "PUBLIC_VALUE");
    };


    public UserDetailsDao(DaoConfig config) {
        super(config);
    }
    
    public UserDetailsDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'USER_DETAILS' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'USER_ID' INTEGER NOT NULL ," + // 1: user_id
                "'KEY' TEXT," + // 2: key
                "'LABEL' TEXT," + // 3: label
                "'KEYBOARD_TYPE' INTEGER," + // 4: keyboard_type
                "'VALUE' TEXT," + // 5: value
                "'PUBLIC_VALUE' INTEGER);"); // 6: public_value
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'USER_DETAILS'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, UserDetails entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getUser_id());
 
        String key = entity.getKey();
        if (key != null) {
            stmt.bindString(3, key);
        }
 
        String label = entity.getLabel();
        if (label != null) {
            stmt.bindString(4, label);
        }
 
        Integer keyboard_type = entity.getKeyboard_type();
        if (keyboard_type != null) {
            stmt.bindLong(5, keyboard_type);
        }
 
        String value = entity.getValue();
        if (value != null) {
            stmt.bindString(6, value);
        }
 
        Integer public_value = entity.getPublic_value();
        if (public_value != null) {
            stmt.bindLong(7, public_value);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public UserDetails readEntity(Cursor cursor, int offset) {
        UserDetails entity = new UserDetails( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // user_id
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // key
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // label
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // keyboard_type
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // value
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6) // public_value
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, UserDetails entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUser_id(cursor.getLong(offset + 1));
        entity.setKey(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setLabel(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setKeyboard_type(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setValue(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setPublic_value(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(UserDetails entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(UserDetails entity) {
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
