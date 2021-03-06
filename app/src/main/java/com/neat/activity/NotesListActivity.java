package com.neat.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.neat.entities.NotesDO;
import com.neat.provider.NeatDataContentProvider;

import org.chalup.microorm.MicroOrm;

import java.util.ArrayList;
import java.util.List;

public class NotesListActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private NotesAdapter mAdapter;
    private List<NotesDO> mNotesDOList;
    private TextView mEmptyView;

    public static final String NOTE_EXTRA = "noteDO";

    private int LOADER_ID = 1201;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NotesListActivity.this, AddEditNoteActivity.class);
                startActivity(intent);
            }
        });
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        mNotesDOList = new ArrayList<>();
        mAdapter = new NotesAdapter(mNotesDOList);
        mAdapter.mList = mNotesDOList;
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        startNotesLoader(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        startNotesLoader(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_about) {
            Toast.makeText(this, R.string.text_about, Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startNotesLoader(final boolean reset) {
        if (reset) {
            getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = null;
        String selection = null;
        String sortOrder = null;
        String[] selectionArgs = null;
        Uri uri = NeatDataContentProvider.NOTES_URI;

        selection = NotesDO.NotesDetailsCoulumns.SOFT_DELETED + " = ? ";
        selectionArgs = new String[]{
                String.valueOf(0)
        };
        return new CursorLoader(NotesListActivity.this, uri, projection, selection, selectionArgs, sortOrder);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            MicroOrm orm = new MicroOrm();
            try {
                if(cursor.moveToFirst()) {
                    mNotesDOList.clear();
                    do {
                        NotesDO item = orm.fromCursor(cursor, NotesDO.class);
                        mNotesDOList.add(item);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            cursor.close();
        }

        if(mNotesDOList.size() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
        mAdapter.mList = mNotesDOList;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class NotesAdapter extends RecyclerView.Adapter<NotesViewHolder> {
        private List<NotesDO> mList;
        NotesAdapter(List<NotesDO> notes) {
            mList = notes;
        }
        @Override
        public NotesViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.notes_view_holder, viewGroup, false);
            return new NotesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(NotesViewHolder holder, int position) {
            NotesDO notesDO = mNotesDOList.get(position);
            if(notesDO != null) {
                holder.text.setText(notesDO.getText());
                holder.title.setText(notesDO.getTitle());
                holder.notesDO = notesDO;
            }
        }

        @Override
        public int getItemCount() {
            if(mList == null) {
                return 0;
            } else {
                return mList.size();
            }
        }
    }

    private class NotesViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView text;
        private CardView card;
        private NotesDO notesDO;
        public NotesViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            text = (TextView) itemView.findViewById(R.id.text);
            card = (CardView) itemView.findViewById(R.id.card);
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(NotesListActivity.this, AddEditNoteActivity.class);
                    intent.putExtra(NOTE_EXTRA, notesDO);
                    startActivity(intent);
                }
            });
        }
    }

}
