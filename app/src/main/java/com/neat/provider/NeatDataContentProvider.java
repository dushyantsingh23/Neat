package com.neat.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.support.annotation.Nullable;

import com.neat.entities.BaseColumns;
import com.neat.entities.NotesDO;


public class NeatDataContentProvider extends SQLiteContentProvider {

    public static final String CONTENT_AUTHORITY = "com.neat.provider";
    private static final String URI_FORMAT = "content://" + CONTENT_AUTHORITY + "/%s";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final Uri NOTES_URI = NotesDO.CONTENT_URI;

    private Context mContext;
    private static final UriMatcher uriMatcher;
    private static final int NOTES_MATCH = 4201;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, NotesDO.TABLE_NAME, NOTES_MATCH);
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        return super.onCreate();
    }

    @Override
    protected SQLiteOpenHelper getDatabaseHelper(Context context) {
        return new DBManager(mContext);
    }

    @Override
    protected Uri insertInTransaction(Uri uri, ContentValues values, boolean callerIsSyncAdapter) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        long row;
        Uri uriToSend;
        String tableName;
        int conflictAlgorithm;

        switch (uriMatcher.match(uri)) {
            case NOTES_MATCH:
                uriToSend = NOTES_URI;
                tableName = NotesDO.TABLE_NAME;
                conflictAlgorithm = SQLiteDatabase.CONFLICT_REPLACE;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Uri finalUri = null;
        try {
            row = db.insertWithOnConflict(tableName, "", values, conflictAlgorithm);
            postNotifyUri(uri);
            finalUri = row == -1 ? null : ContentUris.withAppendedId(uriToSend, row);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return finalUri;
    }

    @Override
    protected int updateInTransaction(Uri uri, ContentValues values, String selection, String[] selectionArgs, boolean callerIsSyncAdapter) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        int count;
        String id;
        String selectionLocal = null;
        String tableName = null;

        switch (uriMatcher.match(uri)) {
            case NOTES_MATCH:
                tableName = NotesDO.TABLE_NAME;
                selectionLocal = selection;
                break;
        }
        count = db.update(tableName, values, selectionLocal, selectionArgs);
        postNotifyUri(uri);
        return count;
    }

    @Override
    protected int deleteInTransaction(Uri uri, String selection, String[] selectionArgs, boolean callerIsSyncAdapter) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        int count;
        String id = uri.getPathSegments().get(0);
        String selectionLocal;
        String tableName;

        switch (uriMatcher.match(uri)) {
            case NOTES_MATCH:
                tableName = NotesDO.TABLE_NAME;
                selectionLocal = selection;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        count = db.delete(tableName, selectionLocal, selectionArgs);
        postNotifyUri(uri);
        return count;
    }

    @Override
    protected void notifyChange(boolean syncToNetwork) {

    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        StringBuilder sb;
        String groupBy = null;
        switch (uriMatcher.match(uri)) {
            case NOTES_MATCH:
            sqlBuilder.setTables(NotesDO.TABLE_NAME);
            break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);

        }
        Cursor c = sqlBuilder.query(getDatabaseHelper().getWritableDatabase(), projection, selection, selectionArgs,
                groupBy, null, sortOrder);
        c.setNotificationUri(mContext.getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case NOTES_MATCH:
                return NotesDO.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case NOTES_MATCH:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        return insertBulk(db, uri, values);
    }

    private int insertBulk(SQLiteDatabase db, Uri uri, ContentValues[] allValues) {
        int rowsAdded = 0;
        Uri uriInsert;
        ContentValues values;
        boolean isCallerSyncAdapter = getIsCallerSyncAdapter(uri);
        db.beginTransactionWithListener(this);
        final long identity = Binder.clearCallingIdentity();
        try {
            for (ContentValues initialValues : allValues) {
                values = initialValues == null ? new ContentValues() : new ContentValues(initialValues);
                uriInsert = insertInTransaction(uri, values, getIsCallerSyncAdapter(uri));
                if (null == uriInsert) {
                    rowsAdded += updateInTransaction(uri,
                            values,
                            BaseColumns.ID + " IS ? ",
                            new String[]{initialValues.getAsString(BaseColumns.ID)},
                            getIsCallerSyncAdapter(uri));
                } else {
                    rowsAdded++;
                }
                db.yieldIfContendedSafely();
            }
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            Binder.restoreCallingIdentity(identity);
            db.endTransaction();
        }

        onEndTransaction(isCallerSyncAdapter);
        return rowsAdded;
    }
}
