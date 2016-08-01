package com.notes.provider;

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

import com.mikepenz.iconics.utils.Utils;
import com.notes.entities.BaseColumns;


public class NeatDataContentProvider extends SQLiteContentProvider {

    public static final String CONTENT_AUTHORITY = "in.encashea.sonic.provider";
    private static final String URI_FORMAT = "content://" + CONTENT_AUTHORITY + "/%s";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private Context mContext;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
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

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        switch (uriMatcher.match(uri)) {
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
