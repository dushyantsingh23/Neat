package com.notes.provider;

import android.content.ContentResolver;
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

import in.encashea.sonic.entities.BaseColumns;
import in.encashea.sonic.entities.Invoice;
import in.encashea.sonic.entities.InvoiceItem;
import in.encashea.sonic.entities.PickupDetails;
import in.encashea.sonic.entities.PickupItem;
import in.encashea.sonic.entities.PickupTracker;
import in.encashea.sonic.entities.User;
import in.encashea.sonic.misc.Utils;

/**
 * Created by Hades on 21/03/16.
 */
public class SonicDataContentProvider extends SQLiteContentProvider {

    public static final String CONTENT_AUTHORITY = "in.encashea.sonic.provider";
    private static final String URI_FORMAT = "content://" + CONTENT_AUTHORITY + "/%s";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final Uri PICKUP_ITEMS_URI = PickupItem.CONTENT_URI;
    public static final Uri PICKUP_DETAILS_URI = PickupDetails.CONTENT_URI;
    public static final Uri USER_DETAILS_URI = User.CONTENT_URI;
    public static final Uri INVOICES_URI = Invoice.CONTENT_URI;
    public static final Uri INVOICE_ITEM_DETAILS_URI = InvoiceItem.CONTENT_URI;
    public static final Uri PICKUP_TRACKER_URI = PickupTracker.CONTENT_URI;

    public static final String PICKUP_PICKUPTRACKER_VIEW = "pickupview";
    public static final Uri PICKUP_PICKUPTRACKER_URI = SonicDataContentProvider.BASE_CONTENT_URI.buildUpon().appendPath(PICKUP_PICKUPTRACKER_VIEW).build();
    public static final String PICKUP_PICKUPTRACKER_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.encashea." + PICKUP_PICKUPTRACKER_VIEW;

    public static final String INVOICE_INVOICE_ITEMS_VIEW = "invoiceview";
    public static final Uri INVOICE_INVOICE_ITEMS_URI = SonicDataContentProvider.BASE_CONTENT_URI.buildUpon().appendPath(INVOICE_INVOICE_ITEMS_VIEW).build();
    public static final String INVOICE_INVOICE_ITEMS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.encashea." + INVOICE_INVOICE_ITEMS_VIEW;

    private Context mContext;
    private static final UriMatcher uriMatcher;

    private static final int PICKUP_ITEM_MATCH = 4201;
    private static final int PICKUP_DETAILS_MATCH = 4202;
    private static final int USER_DETAIL_MATCH = 4203;
    private static final int INVOICES_MATCH = 4204;
    private static final int INVOICES_ITEM_DETAILS_MATCH = 4205;
    private static final int PICKUP_TRACKER_MATCH = 4206;
    private static final int PICKUP_PICKUPTRACKER_MATCH = 4207;
    private static final int INVOICE_INVOICE_ITEMS_MATCH = 4208;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(CONTENT_AUTHORITY, PickupItem.TABLE_NAME, PICKUP_ITEM_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, PickupDetails.TABLE_NAME, PICKUP_DETAILS_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, User.TABLE_NAME, USER_DETAIL_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, Invoice.TABLE_NAME, INVOICES_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, InvoiceItem.TABLE_NAME, INVOICES_ITEM_DETAILS_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, PickupTracker.TABLE_NAME, PICKUP_TRACKER_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, PICKUP_PICKUPTRACKER_VIEW, PICKUP_PICKUPTRACKER_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, INVOICE_INVOICE_ITEMS_VIEW, INVOICE_INVOICE_ITEMS_MATCH);
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
            case PICKUP_ITEM_MATCH:
                uriToSend = PICKUP_ITEMS_URI;
                tableName = PickupItem.TABLE_NAME;
                conflictAlgorithm = SQLiteDatabase.CONFLICT_REPLACE;
                break;
            case PICKUP_DETAILS_MATCH:
                uriToSend = PICKUP_DETAILS_URI;
                tableName = PickupDetails.TABLE_NAME;
                conflictAlgorithm = SQLiteDatabase.CONFLICT_IGNORE;
                break;
            case USER_DETAIL_MATCH:
                uriToSend = USER_DETAILS_URI;
                tableName = User.TABLE_NAME;
                conflictAlgorithm = SQLiteDatabase.CONFLICT_REPLACE;
                break;
            case INVOICES_MATCH:
                uriToSend = INVOICES_URI;
                tableName = Invoice.TABLE_NAME;
                conflictAlgorithm = SQLiteDatabase.CONFLICT_REPLACE;
                break;
            case INVOICES_ITEM_DETAILS_MATCH:
                uriToSend = INVOICE_ITEM_DETAILS_URI;
                tableName = InvoiceItem.TABLE_NAME;
                conflictAlgorithm = SQLiteDatabase.CONFLICT_REPLACE;
                break;
            case PICKUP_TRACKER_MATCH:
                uriToSend = PICKUP_TRACKER_URI;
                tableName = PickupTracker.TABLE_NAME;
                conflictAlgorithm = SQLiteDatabase.CONFLICT_REPLACE;
                break;
            default:
                return null;
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
            case PICKUP_ITEM_MATCH:
                tableName = PickupItem.TABLE_NAME;
                selectionLocal = selection;
                break;
            case PICKUP_DETAILS_MATCH:
                tableName = PickupDetails.TABLE_NAME;
                selectionLocal = selection;
                break;
            case USER_DETAIL_MATCH:
                tableName = User.TABLE_NAME;
                selectionLocal = selection;
                break;
            case INVOICES_MATCH:
                tableName = Invoice.TABLE_NAME;
                selectionLocal = selection;
                break;
            case INVOICES_ITEM_DETAILS_MATCH:
                tableName = InvoiceItem.TABLE_NAME;
                selectionLocal = selection;
                break;
            case PICKUP_TRACKER_MATCH:
                tableName = PickupTracker.TABLE_NAME;
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
            case PICKUP_ITEM_MATCH:
                tableName = PickupItem.TABLE_NAME;
                selectionLocal = selection;
                break;
            case PICKUP_DETAILS_MATCH:
                tableName = PickupDetails.TABLE_NAME;
                selectionLocal = selection;
                break;
            case USER_DETAIL_MATCH:
                tableName = User.TABLE_NAME;
                selectionLocal = selection;
                break;
            case INVOICES_MATCH:
                tableName = Invoice.TABLE_NAME;
                selectionLocal = selection;
                break;
            case INVOICES_ITEM_DETAILS_MATCH:
                tableName = InvoiceItem.TABLE_NAME;
                selectionLocal = selection;
                break;
            case PICKUP_TRACKER_MATCH:
                tableName = PickupTracker.TABLE_NAME;
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
            case PICKUP_ITEM_MATCH:
                sqlBuilder.setTables(PickupItem.TABLE_NAME);
                break;
            case PICKUP_DETAILS_MATCH:
                sqlBuilder.setTables(PickupDetails.TABLE_NAME);
                break;
            case USER_DETAIL_MATCH:
                sqlBuilder.setTables(User.TABLE_NAME);
                break;
            case INVOICES_MATCH:
                sqlBuilder.setTables(Invoice.TABLE_NAME);
                break;
            case INVOICES_ITEM_DETAILS_MATCH:
                sqlBuilder.setTables(InvoiceItem.TABLE_NAME);
                break;
            case PICKUP_TRACKER_MATCH:
                sqlBuilder.setTables(PickupTracker.TABLE_NAME);
                break;
            case PICKUP_PICKUPTRACKER_MATCH:
                sb = new StringBuilder();
                sb.append(
                        PickupTracker.TABLE_NAME + " LEFT OUTER JOIN " + PickupDetails.TABLE_NAME + " ON " + " ( " +
                                PickupDetails.TABLE_NAME + Utils.DOT + PickupDetails.PickupDetailsCoulumns.ENCASHEA_ID
                                + " = " + PickupTracker.TABLE_NAME + Utils.DOT + PickupTracker.PickupTrackerCoulumns.PICKUP_ID
                                + " )");
                sqlBuilder.setTables(sb.toString());
                break;
            case INVOICE_INVOICE_ITEMS_MATCH:
                sb = new StringBuilder();
                sb.append(
                        Invoice.TABLE_NAME + " LEFT OUTER JOIN " + InvoiceItem.TABLE_NAME + " ON " + " ( " +
                                Invoice.TABLE_NAME + Utils.DOT + Invoice.InvoiceCoulumns.LOCAL_ID
                                + " = " + InvoiceItem.TABLE_NAME + Utils.DOT + InvoiceItem.InvoiceItemDetailsCoulumns.LOCAL_INVOICE_ID
                                + " )");
                sqlBuilder.setTables(sb.toString());
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
            case PICKUP_ITEM_MATCH:
                return PickupItem.CONTENT_TYPE;
            case PICKUP_DETAILS_MATCH:
                return PickupDetails.CONTENT_TYPE;
            case USER_DETAIL_MATCH:
                return User.CONTENT_TYPE;
            case INVOICES_MATCH:
                return Invoice.CONTENT_TYPE;
            case INVOICES_ITEM_DETAILS_MATCH:
                return InvoiceItem.CONTENT_TYPE;
            case PICKUP_TRACKER_MATCH:
                return PickupTracker.CONTENT_TYPE;
            case PICKUP_PICKUPTRACKER_MATCH:
                return PICKUP_PICKUPTRACKER_CONTENT_TYPE;
            case INVOICE_INVOICE_ITEMS_MATCH:
                return INVOICE_INVOICE_ITEMS_CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case PICKUP_ITEM_MATCH:
            case PICKUP_DETAILS_MATCH:
            case USER_DETAIL_MATCH:
            case INVOICES_MATCH:
            case INVOICES_ITEM_DETAILS_MATCH:
            case PICKUP_TRACKER_MATCH:
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
