package com.notes.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Hades on 21/03/16.
 */
public class DBManager extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "felix";
    public static final int DATABASE_VERSION = 1;
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";
    public static final String ALTER_TABLE = "ALTER TABLE ";

    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        addBaseTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addBaseTables(SQLiteDatabase db) {
    }
}
