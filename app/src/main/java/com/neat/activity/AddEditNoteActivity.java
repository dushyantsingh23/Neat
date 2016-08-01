package com.neat.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.neat.entities.NotesDO;
import com.neat.provider.NeatDataContentProvider;

public class AddEditNoteActivity extends BaseActivity implements View.OnClickListener {

    private EditText mTitleEditText;
    private EditText mTextEditText;
    private Button mSaveButton;
    private NotesDO mNoteDO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_note);
        mTitleEditText = (EditText) findViewById(R.id.notes_title);
        mTextEditText = (EditText) findViewById(R.id.notes_text);
        mSaveButton = (Button) findViewById(R.id.save_note);

        mSaveButton.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent.hasExtra(NotesListActivity.NOTE_EXTRA)) {
            mNoteDO = intent.getParcelableExtra(NotesListActivity.NOTE_EXTRA);
            mTitleEditText.setText(mNoteDO.getTitle());
            mTextEditText.setText(mNoteDO.getText());
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_note:
                if (mNoteDO == null) {
                    mNoteDO = new NotesDO();
                }
                mNoteDO.setText(mTextEditText.getText().toString().trim());
                mNoteDO.setTitle(mTitleEditText.getText().toString().trim());
                mNoteDO.setSoftDeleted(false);
                new SaveNotesTask().execute(mNoteDO);
                break;
        }
    }

    private class SaveNotesTask extends AsyncTask<NotesDO, Void, Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(NotesDO... params) {
            try {
                NotesDO note = params[0];
                ContentValues contentValues = note.getContenValues(NotesDO.NotesDetailsCoulumns.NotesColumnNames);
                if (note.getId() != null) {
                    String selection = NotesDO.TABLE_NAME + "." + NotesDO.NotesDetailsCoulumns.ID + " = ? ";
                    String selArgs[] = new String[] {
                            note.getId() + ""
                    };
                    getBaseContext().getContentResolver().update(NeatDataContentProvider.NOTES_URI, contentValues, selection, selArgs);
                } else {
                    getBaseContext().getContentResolver().insert(NeatDataContentProvider.NOTES_URI, contentValues);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                finish();
                Toast.makeText(AddEditNoteActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AddEditNoteActivity.this, R.string.save_failure, Toast.LENGTH_SHORT).show();

            }
        }

    }
}
