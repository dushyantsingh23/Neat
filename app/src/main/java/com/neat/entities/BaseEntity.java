package com.neat.entities;

import android.content.ContentValues;
import android.provider.BaseColumns;

public abstract class BaseEntity {

    public final ContentValues getContenValues(String[] columnNames) {
        ContentValues contentValues = new ContentValues();
        for (String string : columnNames) {
            Object value = get(string);
            if (null == value) {
                if (string.compareToIgnoreCase(BaseColumns._ID) == 0) {
                    continue;
                } else {
                    contentValues.putNull(string);
                }
            } else if (value instanceof String) {
                contentValues.put(string, (String) value);
            } else if (value instanceof Integer) {
                contentValues.put(string, (Integer) value);
            } else if (value instanceof Long) {
                contentValues.put(string, (Long) value);
            } else if (value instanceof Double) {
                contentValues.put(string, (Double) value);
            } else if (value instanceof Boolean) {
                contentValues.put(string, (Boolean) value);
            }
        }
        return contentValues;
    }

    public abstract Object get(String value);
}
