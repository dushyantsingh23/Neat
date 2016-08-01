package com.notes.provider;

/**
 * Created by Hades on 21/03/16.
 */

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;
import android.os.Binder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public abstract class SQLiteContentProvider extends ContentProvider
        implements SQLiteTransactionListener {

    private static final String TAG = "SQLiteContentProvider";
    public static final String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";

    private SQLiteOpenHelper mOpenHelper;
    private volatile boolean mNotifyChange;
    protected SQLiteDatabase mDb;

    private final ThreadLocal<Boolean> mApplyingBatch = new ThreadLocal<Boolean>();
    private static final int SLEEP_AFTER_YIELD_DELAY = 4000;

    /**
     * Maximum number of operations allowed in a batch between yield points.
     */
    private static final int MAX_OPERATIONS_PER_YIELD_POINT = 500;

    private Boolean mIsCallerSyncAdapter;

    private Set<Uri> mChangedUris;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mOpenHelper = getDatabaseHelper(context);
        mChangedUris = new HashSet<>();
        return true;
    }

    protected abstract SQLiteOpenHelper getDatabaseHelper(Context context);

    /**
     * The equivalent of the {@link #insert} method, but invoked within a transaction.
     */
    protected abstract Uri insertInTransaction(Uri uri, ContentValues values,
                                               boolean callerIsSyncAdapter);

    /**
     * The equivalent of the {@link #update} method, but invoked within a transaction.
     */
    protected abstract int updateInTransaction(Uri uri, ContentValues values, String selection,
                                               String[] selectionArgs, boolean callerIsSyncAdapter);

    /**
     * The equivalent of the {@link #delete} method, but invoked within a transaction.
     */
    protected abstract int deleteInTransaction(Uri uri, String selection, String[] selectionArgs,
                                               boolean callerIsSyncAdapter);

    protected abstract void notifyChange(boolean syncToNetwork);

    /**
     * Call this to add a URI to the list of URIs to be notified when the transaction
     * is committed.
     */
    protected void postNotifyUri(Uri uri) {
        synchronized (mChangedUris) {
            mChangedUris.add(uri);
        }
    }

    public boolean isCallerSyncAdapter(Uri uri) {
        return false;
    }

    protected SQLiteOpenHelper getDatabaseHelper() {
        return mOpenHelper;
    }

    private boolean applyingBatch() {
        return mApplyingBatch.get() != null && mApplyingBatch.get();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result = null;
        boolean applyingBatch = applyingBatch();
        boolean isCallerSyncAdapter = getIsCallerSyncAdapter(uri);
        if (!applyingBatch) {
            mDb = mOpenHelper.getWritableDatabase();
            mDb.beginTransactionWithListener(this);
            final long identity = Binder.clearCallingIdentity();
            try {
                result = insertInTransaction(uri, values, isCallerSyncAdapter);
                if (result != null) {
                    mNotifyChange = true;
                }
                mDb.setTransactionSuccessful();
            } finally {
                Binder.restoreCallingIdentity(identity);
                mDb.endTransaction();
            }

            onEndTransaction(isCallerSyncAdapter);
        } else {
            result = insertInTransaction(uri, values, isCallerSyncAdapter);
            if (result != null) {
                mNotifyChange = true;
            }
        }
        return result;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int numValues = values.length;
        boolean isCallerSyncAdapter = getIsCallerSyncAdapter(uri);
        mDb = mOpenHelper.getWritableDatabase();
        mDb.beginTransactionWithListener(this);
        final long identity = Binder.clearCallingIdentity();
        try {
            for (int i = 0; i < numValues; i++) {
                Uri result = insertInTransaction(uri, values[i], isCallerSyncAdapter);
                if (result != null) {
                    mNotifyChange = true;
                }
                mDb.yieldIfContendedSafely();
            }
            mDb.setTransactionSuccessful();
        } finally {
            Binder.restoreCallingIdentity(identity);
            mDb.endTransaction();
        }

        onEndTransaction(isCallerSyncAdapter);
        return numValues;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        boolean applyingBatch = applyingBatch();
        boolean isCallerSyncAdapter = getIsCallerSyncAdapter(uri);
        if (!applyingBatch) {
            mDb = mOpenHelper.getWritableDatabase();
            mDb.beginTransactionWithListener(this);
            final long identity = Binder.clearCallingIdentity();
            try {
                count = updateInTransaction(uri, values, selection, selectionArgs,
                        isCallerSyncAdapter);
                if (count > 0) {
                    mNotifyChange = true;
                }
                mDb.setTransactionSuccessful();
            } finally {
                Binder.restoreCallingIdentity(identity);
                mDb.endTransaction();
            }

            onEndTransaction(isCallerSyncAdapter);
        } else {
            count = updateInTransaction(uri, values, selection, selectionArgs,
                    isCallerSyncAdapter);
            if (count > 0) {
                mNotifyChange = true;
            }
        }

        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        boolean applyingBatch = applyingBatch();
        boolean isCallerSyncAdapter = getIsCallerSyncAdapter(uri);
        if (!applyingBatch) {
            mDb = mOpenHelper.getWritableDatabase();
            mDb.beginTransactionWithListener(this);
            final long identity = Binder.clearCallingIdentity();
            try {
                count = deleteInTransaction(uri, selection, selectionArgs, isCallerSyncAdapter);
                if (count > 0) {
                    mNotifyChange = true;
                }
                mDb.setTransactionSuccessful();
            } finally {
                Binder.restoreCallingIdentity(identity);
                mDb.endTransaction();
            }

            onEndTransaction(isCallerSyncAdapter);
        } else {
            count = deleteInTransaction(uri, selection, selectionArgs, isCallerSyncAdapter);
            if (count > 0) {
                mNotifyChange = true;
            }
        }
        return count;
    }

    protected boolean getIsCallerSyncAdapter(Uri uri) {
        boolean isCurrentSyncAdapter = QueryParameterUtils.readBooleanQueryParameter(uri,
                CALLER_IS_SYNCADAPTER, false);
        if (mIsCallerSyncAdapter == null || mIsCallerSyncAdapter) {
            mIsCallerSyncAdapter = isCurrentSyncAdapter;
        }
        return isCurrentSyncAdapter;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final int numOperations = operations.size();
        if (numOperations == 0) {
            return new ContentProviderResult[0];
        }
        int ypCount = 0;
        int opCount = 0;
        mDb = mOpenHelper.getWritableDatabase();
        mDb.beginTransactionWithListener(this);
        final boolean isCallerSyncAdapter = getIsCallerSyncAdapter(operations.get(0).getUri());
        final long identity = Binder.clearCallingIdentity();
        try {
            mApplyingBatch.set(true);
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                if (opCount + 1 >= MAX_OPERATIONS_PER_YIELD_POINT) {
                    opCount = 0;
                    if (i > 0 && mDb.yieldIfContendedSafely()) {
                        ypCount ++;
                    }
                }
                final ContentProviderOperation operation = operations.get(i);
                if (i > 0 && operation.isYieldAllowed()) {
                    opCount = 0;
                    if (mDb.yieldIfContendedSafely()) {
                        ypCount++;
                    }
                }
                results[i] = operation.apply(this, results, i);
            }
            mDb.setTransactionSuccessful();
            return results;
        } finally {
            mApplyingBatch.set(false);
            mDb.endTransaction();
            onEndTransaction(isCallerSyncAdapter);
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void onBegin() {
        mIsCallerSyncAdapter = null;
        onBeginTransaction();
    }

    public void onCommit() {
        beforeTransactionCommit();
    }

    public void onRollback() {
        // not used
    }

    protected void onBeginTransaction() {
    }

    protected void beforeTransactionCommit() {
    }

    protected void onEndTransaction(boolean isCallerSyncAdapter) {
        /*if (mNotifyChange) {
            mNotifyChange = false;
            // We sync to network if the caller was not the sync adapter
            notifyChange(!isCallerSyncAdapter);
        }*/

        Set<Uri> changed;
        synchronized (mChangedUris) {
            changed = new HashSet<>(mChangedUris);
            mChangedUris.clear();
        }
        ContentResolver resolver = getContext().getContentResolver();
        for (Uri uri : changed) {
            if (!isCallerSyncAdapter) {
                resolver.notifyChange(uri, null, syncToNetwork());
            }
        }
    }

    protected boolean syncToNetwork() {
        return false;
    }

    public static class QueryParameterUtils {

        public static boolean readBooleanQueryParameter(Uri uri, String name,
                                                        boolean defaultValue) {
            final String flag = getQueryParameter(uri, name);
            return flag == null
                    ? defaultValue
                    : (!"false".equals(flag.toLowerCase()) && !"0".equals(flag.toLowerCase()));
        }

        /**
         * A fast re-implementation of {@link android.net.Uri#getQueryParameter}
         */
        public static String getQueryParameter(Uri uri, String parameter) {
            String query = uri.getEncodedQuery();
            if (query == null) {
                return null;
            }

            int queryLength = query.length();
            int parameterLength = parameter.length();

            String value;
            int index = 0;
            while (true) {
                index = query.indexOf(parameter, index);
                if (index == -1) {
                    return null;
                }

                index += parameterLength;

                if (queryLength == index) {
                    return null;
                }

                if (query.charAt(index) == '=') {
                    index++;
                    break;
                }
            }

            int ampIndex = query.indexOf('&', index);
            if (ampIndex == -1) {
                value = query.substring(index);
            } else {
                value = query.substring(index, ampIndex);
            }

            return Uri.decode(value);
        }

        public static Uri setUriAsCalledFromSyncAdapter(Uri uri) {
            return uri.buildUpon().appendQueryParameter(CALLER_IS_SYNCADAPTER, "true")
                    .build();
        }
    }
}