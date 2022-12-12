package com.example.mynote.database;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.example.mynote.models.Notes;

import java.util.List;

@Dao
public interface MainDao {
    @Insert(onConflict = REPLACE)
    void insert(Notes notes);

    @Delete
    void delete(Notes notes);

    @Delete
    void reset(List<Notes> notesList);

    @Query("UPDATE notes SET title = :title, notes = :note WHERE ID = :id")
    void update(int id, String title, String note);

    @Query("UPDATE notes SET pin = :pin WHERE ID = :id")
    void pin(int id, boolean pin);

    @Query("SELECT * FROM notes ORDER BY ID DESC")
    List<Notes> getAll();
}
