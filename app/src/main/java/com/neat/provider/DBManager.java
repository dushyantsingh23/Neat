package com.neat.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.neat.entities.NotesDO;

public class DBManager extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "felix";
    public static final int DATABASE_VERSION = 1;
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";
    public static final String ALTER_TABLE = "ALTER TABLE ";

    public static final String CREATE_TABLE_NOTES =
            CREATE_TABLE + NotesDO.TABLE_NAME + NotesDO.CREATE_TABLE;

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
        db.execSQL(CREATE_TABLE_NOTES);
    }
}
