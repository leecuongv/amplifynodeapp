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

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


public class MainActivity3 extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    String classs = "com.mysql.jdbc.Driver";
    String url = "jdbc:mysql://192.168.1.40:3306/notes";
    String un = "root";
    String password = "abc123";
    Statement st;
    ResultSet rs;
    ResultSetMetaData rsmd;
    Connection conn = null;



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
            Intent intent = new Intent(MainActivity3.this, NoteTakerActivity.class);
            intent.putExtra("old_note", notes);
            startActivityForResult(intent, 102);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            //selectedNote = new Notes();
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

        notes = loaddb();
        try {
            conn = DriverManager.getConnection(url, un,password);



        }
        catch (SQLException e){
            Toast.makeText(this, "Lỗi 2" + e.getMessage(), Toast.LENGTH_SHORT).show();
            System.out.println("Lỗi: " + e.getMessage());
        }

        //database = RoomDB.getInstance(this);
        //notes = (List<Notes>) rsmd;





        updateRecycler(loadLayoutStyle());


        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity3.this, NoteTakerActivity.class);
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

                        Toast.makeText(MainActivity3.this, "Đã cập nhật bố cục!", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.pinned:
                        Toast.makeText(MainActivity3.this, "Đang phát triển!", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.settings:
                        Toast.makeText(MainActivity3.this, "Đang phát triển!", Toast.LENGTH_SHORT).show();
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
                notes = loaddb();
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Loaded", Toast.LENGTH_SHORT).show();
                if (!notes.isEmpty()) {
                    textView_placeholder.setVisibility(View.GONE);
                }

            }
            return;
        } else if (requestCode == 102) {
            if (resultCode == Activity.RESULT_OK) {
                Notes new_note = (Notes) data.getSerializableExtra("note");

                String sqlUpdate = "update notes set title='"+ new_note.getTitle() + "', notes='" + new_note.getNotes() + "' where id ='" + new_note.getID() +"'";
                notes.clear();

                notes = excuteUpdate(sqlUpdate);
                if (!notes.isEmpty()) {
                    textView_placeholder.setVisibility(View.GONE);
                }

            }
            return;
        }
        Toast.makeText(MainActivity3.this, "Đã hủy!", Toast.LENGTH_SHORT).show();

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
                    //database.mainDao().pin(selectedNote.getID(), false);
                    String sqlUpdate = "update notes set pinned='"+ false + "' where id ='" + selectedNote.getID() +"'";
                    notes.clear();

                    notes = excuteUpdate(sqlUpdate);


                    Toast.makeText(MainActivity3.this, "Unpinned!", Toast.LENGTH_SHORT).show();
                } else {
                    String sqlUpdate = "update notes set pinned='"+ true + "' where id ='" + selectedNote.getID() +"'";
                    notes.clear();

                    notes = excuteUpdate(sqlUpdate);
                    Toast.makeText(MainActivity3.this, "Pinned!", Toast.LENGTH_SHORT).show();
                }

                notesListAdapter.notifyDataSetChanged();
                return true;
            case R.id.delete:

                String sqlUpdate = "delete from notes where id=" + Integer.valueOf(selectedNote.getID());
                notes.remove(selectedNote);
                Toast.makeText(MainActivity3.this, "Deleted!", Toast.LENGTH_SHORT).show();

                return true;
            case R.id.home:
                Toast.makeText(MainActivity3.this, "Will be available soon!", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }

    public List<Notes> loaddb(){
        String classs = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://127.0.0.1:3306/notes?useUnicode=yes&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true";
        String un = "root";
        String password = "abc123";
        Statement st;
        ResultSet rs;
        ResultSetMetaData rsmd;
        Connection conn = null;
        int numberOfColumns;
        List row = null;
        List<Notes> table = new ArrayList<>();
        try {
            Class.forName(classs);
            conn = DriverManager.getConnection(url, un,password);
            System.out.println("Connected?");

            System.err.println("Hearesss");
            st = conn.createStatement();
            rs = st.executeQuery("select * from notes");
            rsmd = rs.getMetaData();
           numberOfColumns = rsmd.getColumnCount();



            while (rs.next()) {
                int i = rs.getInt("id");
                String str = rs.getString("title");
                String date = rs.getString("date");
                String color_code = rs.getString("color");
               //Boolean pin = rs.getString("pin");

                //Assuming you have a user object
                Notes user = new Notes(i, str, date, color_code, false);

                notes.add(user);
            }
        }
        catch (SQLException e){} catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("=-------------------------------------------\n"+table);
        return table;
    }


    public List<Notes> excuteUpdate(String query){
        String classs = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://127.0.0.1:3306/notes?useUnicode=yes&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true";
        String un = "root";
        String password = "abc123";
        Statement st;
        ResultSet rs;
        ResultSetMetaData rsmd;
        Connection conn = null;
        int numberOfColumns;
        List row = null;
        List<Notes> table = new ArrayList<>();
        try {
            Class.forName(classs);
            conn = DriverManager.getConnection(url, un,password);
            System.out.println("Connected?");

            System.err.println("Hearesss");
            st = conn.createStatement();
            st.executeUpdate(query);

        }
        catch (SQLException e){} catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("=-------------------------------------------\n"+table);
        return loaddb();
    }


}