package com.dghan.vomeo.Database;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class DatabaseHandler {
    private static DatabaseHandler instance = null;
    public static ArrayList<Word> words = null;
    public static Database data = null;
    static FirebaseDatabase database = null;
    static DatabaseReference myRef = null;
    int currentLevel;
    int points[];
    boolean shouldGetData = true;
    public static ArrayList<Word> words1 = new ArrayList<>();
    public static ArrayList<String> getTerms() {
        ArrayList<String> terms = new ArrayList<>();
        for(Word word : words){
            terms.add(word.term);
        }
        return terms;
    }

    StreamAudio streamAudio = new StreamAudio();

    public void getLevel(int level){
        char c;
        switch (level){
            case 1: c = 'A'; // basic
                break;
            case 2: c = 'B'; // intermediate
                break;
            case 3: c = 'C'; // advance
                break;
            case 0:
            default:    words = data.words;
                return;
        }
        words1.clear();
        // can simply get whole array by copying from the level start to end index...
        for(Word word : data.words){
            if(word.level.charAt(0) == c) {
                words1.add(word);
                Log.e("level", word.level);
            }
        }
        words = words1;
    }

    public void playAudio(String term){
        streamAudio.play(term);
    }

    public void delete(){
        instance = null; words = null;
        data = null;
        database = null; myRef = null;
    }

    public static DatabaseHandler getInstance() {
        return instance;
    }
    public static void setInstance(String username) {
        if(instance == null)
            instance = new DatabaseHandler(username);
    }

    private DatabaseHandler(String username){
        data = new Database();
        data.username = username;

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();//.child(data.username);
        //myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
/*                ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("Welcome to VoMeo");
                progressDialog.setMessage("Please be patient, we are setting the environment for you");
                progressDialog.show();*/
                if(shouldGetData) {
                    if (dataSnapshot.hasChild("0")) {
                        words = new ArrayList<>();
                        DataSnapshot tmps = dataSnapshot.child("0").child("data");
                        int index = 0;
                        for (DataSnapshot tmp : tmps.getChildren()) {
                            Word word = new Word();
                            word.index = index++;
                            word.term = tmp.child("term").getValue(String.class);
                            word.definition = tmp.child("definition").getValue(String.class);
                            word.level = tmp.child("level").getValue(String.class);
                            if (dataSnapshot.child("0").child(data.username).child("favorite").child(String.valueOf(index)).exists())
                                word.favorite = true;
                            words.add(word);
                        }
                        data.words = words;
                    } else {
                        Log.e("data", "create new word list");
                    }
                /*if(progressDialog.isShowing())
                    progressDialog.dismiss();*/
                    data.words = words;
                } else {
                    shouldGetData = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
        Date date= new Date();
        myRef.child("last_access").setValue(date.getTime());
    }

    public void toggleFavorite(int index) {
        boolean current = words.get(index).favorite;
        words.get(index).favorite = !current;
        data.words.get(words.get(index).index).favorite = !current;
        if(current)
            myRef.child("0").child(data.username).child("favorite").setValue(index);
        else
            myRef.child("0").child(data.username).child("favorite").child(String.valueOf(index)).setValue(null);
        shouldGetData = false;
    }

//arraylist.add(int index, element)
}
