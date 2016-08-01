package com.neat.activity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.neat.entities.NotesDO;
import com.neat.provider.NeatDataContentProvider;

public class AddNewNoteActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mTitleEditText;
    private EditText mTextEditText;
    private Button mSaveButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_note);

        mTitleEditText = (EditText) findViewById(R.id.notes_title);
        mTextEditText = (EditText) findViewById(R.id.notes_text);
        mSaveButton = (Button) findViewById(R.id.save_note);

        mSaveButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_note:
                NotesDO notesDO = new NotesDO();
                notesDO.setText(mTextEditText.getText().toString());
                notesDO.setTitle(mTitleEditText.getText().toString());
                notesDO.setSoftDeleted(false);
                new SaveNotesTask().execute(notesDO);
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
                Uri row = getBaseContext().getContentResolver().insert(NeatDataContentProvider.NOTES_URI,
                        note.getContenValues(NotesDO.NotesDetailsCoulumns.NotesColumnNames));
                return row != null;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
        }

    }
}
