package com.neat.entities;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import com.neat.provider.NeatDataContentProvider;

import org.chalup.microorm.annotations.Column;

public class NotesDO extends BaseEntity implements BaseColumns, Parcelable{

    public static final String TABLE_NAME = "t_notes";
    public static final String PATH = TABLE_NAME;
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.neat." + TABLE_NAME;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.neat." + TABLE_NAME;
    public static final Uri CONTENT_URI = NeatDataContentProvider.BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();


    @Column("title")
    private String title;
    @Column("text")
    private String text;
    @Column("soft_deleted")
    private Boolean softDeleted;

    public NotesDO() {

    }

    public Boolean getSoftDeleted() {
        return softDeleted;
    }

    public void setSoftDeleted(Boolean softDeleted) {
        this.softDeleted = softDeleted;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public NotesDO(Parcel in) {
        title = in.readString();
        text = in.readString();
    }

    private static final SparseArray<String> notesDetailsColumnsMap = new SparseArray<>();

    static {
        notesDetailsColumnsMap.put(0, NotesDetailsCoulumns.ID);
        notesDetailsColumnsMap.put(1, NotesDetailsCoulumns.TITLE);
        notesDetailsColumnsMap.put(2, NotesDetailsCoulumns.TEXT);
        notesDetailsColumnsMap.put(3, NotesDetailsCoulumns.SOFT_DELETED);
    }

    public static final Creator<NotesDO> CREATOR = new Creator<NotesDO>() {
        @Override
        public NotesDO createFromParcel(Parcel in) {
            return new NotesDO(in);
        }

        @Override
        public NotesDO[] newArray(int size) {
            return new NotesDO[size];
        }
    };

    @Override
    public Object get(String value) {
        switch (notesDetailsColumnsMap.indexOfValue(value)) {
            case 1:
                return title;
            case 2:
                return text;
            case 3:
                return softDeleted;
            default:
                return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(text);
    }

    public static class NotesDetailsCoulumns {
        public static final String ID = android.provider.BaseColumns._ID;
        public static final String TITLE = "title";
        public static final String TEXT = "text";
        public static final String SOFT_DELETED = "soft_deleted";

        public static final String[] NotesColumnNames = {
                ID,
                TITLE,
                TEXT,
                SOFT_DELETED
        };
    }

    public static final String CREATE_TABLE = " ( " +
        NotesDetailsCoulumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        NotesDetailsCoulumns.TITLE + " TEXT , " +
        NotesDetailsCoulumns.TEXT + " TEXT, " +
        NotesDetailsCoulumns.SOFT_DELETED + " BOOLEAN " +
        " ) ";
}
