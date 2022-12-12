package com.example.mynote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mynote.adapters.NoteListAdapter;
import com.example.mynote.database.RoomDB;
import com.example.mynote.models.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LAYOUT = "layout";
    RecyclerView recyclerView;
    NoteListAdapter notesListAdapter;
    Toolbar toolbar;
    List<Notes> notes = new ArrayList<>();
    RoomDB database;
    FloatingActionButton fab_add;
    Notes selectedNote;
    private final NotesClickListener noteClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this, NoteTakerActivity.class);
            intent.putExtra("old_note", notes);
            startActivityForResult(intent, 102);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            selectedNote = new Notes();
            selectedNote = notes;
            showPopup(cardView);
        }
    };
    SearchView search_view_home;
    TextView textView_placeholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));

        recyclerView = findViewById(R.id.recycler_notes);
        toolbar = findViewById(R.id.toolbar_home);
        fab_add = findViewById(R.id.fab_add);
        search_view_home = findViewById(R.id.search_view_home);
        textView_placeholder = findViewById(R.id.textView_placeholder);

        toolbar.inflateMenu(R.menu.home_menu);

        database = RoomDB.getInstance(this);
        notes = database.mainDao().getAll();

        updateRecycler(loadLayoutStyle());


        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteTakerActivity.class);
                startActivityForResult(intent, 101);
            }
        });

        search_view_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.layout:
                        if (loadLayoutStyle().equals("linear")) {
                            saveLayoutStyle("grid");
                            updateRecycler(loadLayoutStyle());
                        } else {
                            saveLayoutStyle("linear");
                            updateRecycler(loadLayoutStyle());
                        }

                        Toast.makeText(MainActivity.this, "Đã cập nhật bố cục!", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.pinned:
                        Toast.makeText(MainActivity.this, "Đang phát triển!", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.settings:
                        Toast.makeText(MainActivity.this, "Đang phát triển!", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });
    }

    private void saveLayoutStyle(String layout) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(LAYOUT, layout);
        editor.apply();
    }

    private String loadLayoutStyle() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String layout = sharedPreferences.getString(LAYOUT, "");
        return layout;
    }

    private void updateRecycler(String layout) {
        if (!notes.isEmpty()) {
            textView_placeholder.setVisibility(View.GONE);
        }
        recyclerView.setHasFixedSize(true);
        if (layout.equals("linear")) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        }
        notesListAdapter = new NoteListAdapter(this, notes, noteClickListener);
        recyclerView.setAdapter(notesListAdapter);
        layoutAnimation(recyclerView);
    }

    private void filter(String newText) {
        List<Notes> filteredList = new ArrayList<>();
        for (Notes singleNote : notes) {
            if (singleNote.getTitle().toLowerCase().contains(newText.toLowerCase())
                    || singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(singleNote);
            }
        }
        notesListAdapter.filterList(filteredList, newText);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_note = (Notes) data.getSerializableExtra("note");
                database.mainDao().insert(new_note);
                notes.clear();
                notes.addAll(database.mainDao().getAll());
                notesListAdapter.notifyDataSetChanged();
                if (!notes.isEmpty()) {
                    textView_placeholder.setVisibility(View.GONE);
                }
            }
            return;
        } else if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_note = (Notes) data.getSerializableExtra("note");
                database.mainDao().update(new_note.getID(), new_note.getTitle(), new_note.getNotes());
                notes.clear();
                notes.addAll(database.mainDao().getAll());
                notesListAdapter.notifyDataSetChanged();
                if (!notes.isEmpty()) {
                    textView_placeholder.setVisibility(View.GONE);
                }
            }
            return;
        }
        Toast.makeText(MainActivity.this, "Đã hủy!", Toast.LENGTH_SHORT).show();

    }

    public void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    private void layoutAnimation(RecyclerView recyclerView) {
        Context context = recyclerView.getContext();
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_recycler_anim);
        recyclerView.setLayoutAnimation(animationController);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.pin:
                if (selectedNote.isStarred()) {
                    database.mainDao().pin(selectedNote.getID(), false);
                    Toast.makeText(MainActivity.this, "Unpinned!", Toast.LENGTH_SHORT).show();
                } else {
                    database.mainDao().pin(selectedNote.getID(), true);
                    Toast.makeText(MainActivity.this, "Pinned!", Toast.LENGTH_SHORT).show();
                }
                notes.clear();
                notes.addAll(database.mainDao().getAll());
                notesListAdapter.notifyDataSetChanged();
                return true;
            case R.id.delete:
                database.mainDao().delete(selectedNote);
                notes.remove(selectedNote);
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.home:
                Toast.makeText(MainActivity.this, "Will be available soon!", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

}