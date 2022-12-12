package com.example.mynote.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mynote.models.Notes;

@Database(entities = {Notes.class}, version =  1, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {
    private  static  RoomDB db;
    private static  String DATABASE_NAME = "NoteApp";

    public synchronized static  RoomDB getInstance(Context context){
        if(db==null){
            db= Room.databaseBuilder(context.getApplicationContext(),
                    RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }


        return db;
    }

    public abstract  MainDao mainDao();
}
