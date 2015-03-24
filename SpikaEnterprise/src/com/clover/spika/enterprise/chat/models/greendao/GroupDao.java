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

import com.clover.spika.enterprise.chat.models.greendao.Group;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table GROUP.
*/
public class GroupDao extends AbstractDao<Group, String> {

    public static final String TABLENAME = "GROUP";

    /**
     * Properties of entity Group.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, String.class, "id", true, "ID");
        public final static Property Type = new Property(1, String.class, "type", false, "TYPE");
        public final static Property Groupname = new Property(2, String.class, "groupname", false, "GROUPNAME");
        public final static Property Image = new Property(3, String.class, "image", false, "IMAGE");
        public final static Property Image_thumb = new Property(4, String.class, "image_thumb", false, "IMAGE_THUMB");
        public final static Property Is_member = new Property(5, Integer.class, "is_member", false, "IS_MEMBER");
        public final static Property CategoryId = new Property(6, Long.class, "categoryId", false, "CATEGORY_ID");
    };

    private DaoSession daoSession;


    public GroupDao(DaoConfig config) {
        super(config);
    }
    
    public GroupDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'GROUP' (" + //
                "'ID' TEXT PRIMARY KEY NOT NULL UNIQUE ," + // 0: id
                "'TYPE' TEXT," + // 1: type
                "'GROUPNAME' TEXT," + // 2: groupname
                "'IMAGE' TEXT," + // 3: image
                "'IMAGE_THUMB' TEXT," + // 4: image_thumb
                "'IS_MEMBER' INTEGER," + // 5: is_member
                "'CATEGORY_ID' INTEGER);"); // 6: categoryId
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'GROUP'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Group entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getId());
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(2, type);
        }
 
        String groupname = entity.getGroupname();
        if (groupname != null) {
            stmt.bindString(3, groupname);
        }
 
        String image = entity.getImage();
        if (image != null) {
            stmt.bindString(4, image);
        }
 
        String image_thumb = entity.getImage_thumb();
        if (image_thumb != null) {
            stmt.bindString(5, image_thumb);
        }
 
        Integer is_member = entity.getIs_member();
        if (is_member != null) {
            stmt.bindLong(6, is_member);
        }
 
        Long categoryId = entity.getCategoryId();
        if (categoryId != null) {
            stmt.bindLong(7, categoryId);
        }
    }

    @Override
    protected void attachEntity(Group entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Group readEntity(Cursor cursor, int offset) {
        Group entity = new Group( //
            cursor.getString(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // type
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // groupname
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // image
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // image_thumb
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // is_member
            cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6) // categoryId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Group entity, int offset) {
        entity.setId(cursor.getString(offset + 0));
        entity.setType(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setGroupname(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setImage(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setImage_thumb(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setIs_member(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setCategoryId(cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(Group entity, long rowId) {
        return entity.getId();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(Group entity) {
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
    
    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getCategoryDao().getAllColumns());
            builder.append(" FROM GROUP T");
            builder.append(" LEFT JOIN CATEGORY T0 ON T.'CATEGORY_ID'=T0.'ID'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Group loadCurrentDeep(Cursor cursor, boolean lock) {
        Group entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Category category = loadCurrentOther(daoSession.getCategoryDao(), cursor, offset);
        entity.setCategory(category);

        return entity;    
    }

    public Group loadDeep(Long key) {
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
    public List<Group> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Group> list = new ArrayList<Group>(count);
        
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
    
    protected List<Group> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Group> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
