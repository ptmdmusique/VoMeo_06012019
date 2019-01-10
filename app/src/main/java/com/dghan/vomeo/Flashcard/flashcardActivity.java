package com.dghan.vomeo.Flashcard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.dghan.vomeo.Database.DatabaseHandler;
import com.dghan.vomeo.Database.Word;
import com.dghan.vomeo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.dghan.vomeo.Database.DatabaseHandler.words;

public class flashcardActivity extends AppCompatActivity {
    ListView list;
    ImageButton button_shuffle, button_replay;
    private FLashcardAdapter fLashcardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flashcard_layout);
        list = findViewById(R.id.flashcard_list);
        fLashcardAdapter = new FLashcardAdapter(this);
        list.setAdapter(fLashcardAdapter);
        shuffleActivity();
        replayActivity();
    }

    @SuppressLint("WrongViewCast")
    private void replayActivity() {
        button_replay = findViewById(R.id.button_repeat);
        button_replay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Collections.sort(words, new Comparator<Word>() {
                    @Override
                    public int compare(Word c1, Word c2) {
                        return c1.index - c2.index;
                    }
                });
                fLashcardAdapter.notifyDataSetChanged();
                list.invalidateViews();
                list.refreshDrawableState();
            }
        });
    }

    @SuppressLint("WrongViewCast")
    private void shuffleActivity() {
        button_shuffle = findViewById(R.id.button_shuffle);
        button_shuffle.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Collections.shuffle(words);
                fLashcardAdapter.notifyDataSetChanged();
                list.invalidateViews();
                list.refreshDrawableState();
            }
        });
    }

    class FLashcardAdapter extends ArrayAdapter<String>{
        boolean[] termShown;
        DatabaseHandler databaseHandler = null;
        ArrayList<Word> words = null;
        FLashcardAdapter(Context context){
            super(context, R.layout.flashcard_row, R.id.flashcard_word, DatabaseHandler.getTerms());
            databaseHandler = DatabaseHandler.getInstance();
            words = DatabaseHandler.words;
        }
        public void play(String term) {
            databaseHandler.playAudio(term);
        }
        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.flashcard_row, parent, false);

            final TextView textView = row.findViewById(R.id.flashcard_word);
            ImageButton button_audio = row.findViewById(R.id.flashcard_speaker_button);
            termShown = new boolean[DatabaseHandler.data.words.size()];//on init wordlist
            for (int i=0; i<3; i++){
                termShown[i] = true;
            }

            textView.setText(words.get(position).term);
            textviewActivity(textView, position);

            button_audio.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    play(words.get(position).term);
                }
            });

            return row;
        }
        private void textviewActivity(final TextView textView, final int position){
            textView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Word word = words.get(position);
                    if (termShown[word.index]) {
                        textView.setText(word.definition);
                        termShown[word.index] = false;
                    } else {
                        textView.setText(word.term);
                        termShown[word.index] = true;
                    }
                }
            });
        }
    }
}
