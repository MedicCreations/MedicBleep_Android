package com.clover.spika.enterprise.chat.models.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.clover.spika.enterprise.chat.models.greendao.Stickers;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table STICKERS.
*/
public class StickersDao extends AbstractDao<Stickers, Long> {

    public static final String TABLENAME = "STICKERS";

    /**
     * Properties of entity Stickers.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "ID");
        public final static Property Filename = new Property(1, String.class, "filename", false, "FILENAME");
        public final static Property Is_deleted = new Property(2, Integer.class, "is_deleted", false, "IS_DELETED");
        public final static Property Created = new Property(3, Long.class, "created", false, "CREATED");
        public final static Property Url = new Property(4, String.class, "url", false, "URL");
        public final static Property Organization_id = new Property(5, Integer.class, "organization_id", false, "ORGANIZATION_ID");
        public final static Property UsedTimes = new Property(6, Integer.class, "usedTimes", false, "USED_TIMES");
    };


    public StickersDao(DaoConfig config) {
        super(config);
    }
    
    public StickersDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'STICKERS' (" + //
                "'ID' INTEGER PRIMARY KEY NOT NULL UNIQUE ," + // 0: id
                "'FILENAME' TEXT," + // 1: filename
                "'IS_DELETED' INTEGER," + // 2: is_deleted
                "'CREATED' INTEGER," + // 3: created
                "'URL' TEXT," + // 4: url
                "'ORGANIZATION_ID' INTEGER," + // 5: organization_id
                "'USED_TIMES' INTEGER);"); // 6: usedTimes
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'STICKERS'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Stickers entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String filename = entity.getFilename();
        if (filename != null) {
            stmt.bindString(2, filename);
        }
 
        Integer is_deleted = entity.getIs_deleted();
        if (is_deleted != null) {
            stmt.bindLong(3, is_deleted);
        }
 
        Long created = entity.getCreated();
        if (created != null) {
            stmt.bindLong(4, created);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(5, url);
        }
 
        Integer organization_id = entity.getOrganization_id();
        if (organization_id != null) {
            stmt.bindLong(6, organization_id);
        }
 
        Integer usedTimes = entity.getUsedTimes();
        if (usedTimes != null) {
            stmt.bindLong(7, usedTimes);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Stickers readEntity(Cursor cursor, int offset) {
        Stickers entity = new Stickers( //
            cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // filename
            cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // is_deleted
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // created
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // url
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // organization_id
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6) // usedTimes
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Stickers entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setFilename(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setIs_deleted(cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2));
        entity.setCreated(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setUrl(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setOrganization_id(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setUsedTimes(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Stickers entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Stickers entity) {
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