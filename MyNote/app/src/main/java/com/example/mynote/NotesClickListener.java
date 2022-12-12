package com.example.mynote;

import androidx.cardview.widget.CardView;

import com.example.mynote.models.Notes;

public interface NotesClickListener {
    void  onClick(Notes note);
    void onLongClick(Notes note, CardView cardView);


}
