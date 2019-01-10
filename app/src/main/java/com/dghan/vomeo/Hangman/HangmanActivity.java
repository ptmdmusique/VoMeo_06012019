package com.dghan.vomeo.Hangman;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dghan.vomeo.FullscreenActivity;
import com.dghan.vomeo.R;
import com.dghan.vomeo.Database.DatabaseHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HangmanActivity extends AppCompatActivity implements KeyboardView.OnKeyboardActionListener {

    Keyboard mKeyboard;
    KeyboardView mKeyboardView;
    //number of characters in current word
    private int _numChars;
    //number correctly guessed
    private int _numCorr;
    private static String _char;
    //private static ArrayList<String> _listword;
    private static String _word;
    protected static int _count;
    private ImageView imageView;
    private static int _answerLengthCount;
    private LinearLayout textLayout;
    private TextView [] charViews;
    private AlertDialog helpAlert;
    private static final String TAG = HangmanActivity.class.getSimpleName();
    private DatabaseHandler databaseHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hangman);

        /*DatabaseReference _FirebaseDatabase;
        _FirebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Database").child("tester");
        final ValueEventListener getDatabase = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot data_alarm = dataSnapshot.child("alarm");
                    DataSnapshot data_terms = dataSnapshot.child("term");
                    Iterable<DataSnapshot> termsChildren = data_terms.getChildren();
                    _listword = new ArrayList<>();
                    for (DataSnapshot term : termsChildren) {
                        String s = term.getValue(String.class);
                        _listword.add(s);
                    }
                    String s = data_alarm.getValue(String.class);
                } else {
                    Log.d(TAG, "retrieved null database");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        };

        _FirebaseDatabase.addValueEventListener(getDatabase);
        //mFirebaseDatabase.addOnFailureListener(databaseFail);
        */
        databaseHandler = DatabaseHandler.getInstance();
        showHelp();
        initGame();
    }

    @Override
    public void onPress(int primaryCode) {
        Log.i("Key", "Pressed");

        char code = (char) primaryCode;
        _char = String.valueOf(code);
        Log.i("Key", _char);
        displayStage(search(_char));
    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    public boolean search(String string){
        boolean hasChar = false;
        for(int i = 0; i<_word.length(); i++){
            if(_char.equals(String.valueOf(_word.charAt(i))) && charViews[i].getText().toString().equals("_ ")){
                hasChar = true;
                charViews[i].setText(""+_word.charAt(i)+" ");
                ++_answerLengthCount;
                String _test = String.valueOf(_answerLengthCount);
                Log.i("count", _test);
            }
            else if(_char.equals(String.valueOf(_word.charAt(i))) && !charViews[i].getText().toString().equals("_ "))
                hasChar = true;
        }
        return hasChar;
    }

    public void displayStage(boolean bool){
        if(!bool){
            switch(++_count){
                case 1:
                    imageView.setImageResource(R.drawable.head);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.body);
                    break;
                case 3:
                    imageView.setImageResource(R.drawable.leftarm);
                    break;
                case 4:
                    imageView.setImageResource(R.drawable.rightarm);
                    break;
                case 5:
                    imageView.setImageResource(R.drawable.leftfoot);
                    break;
                case 6:
                    imageView.setImageResource(R.drawable.execute_animation);
                    AnimationDrawable hangAnimation = (AnimationDrawable)imageView.getDrawable();
                    hangAnimation.start();
                    int time=0;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showDialog();
                        }
                    }, 5600);
                    break;
            }

        }
        else {
            showDialog();
        }
    }

    public void initGame(){
        _count=0;
        Random rand = new Random();
        int x = rand.nextInt(databaseHandler.words.size());//_listword.size()-1);
        _word = databaseHandler.words.get(x).term;

        imageView = findViewById(R.id.imageView);
        textLayout = findViewById(R.id.answer);
        textLayout.removeAllViews();

        mKeyboard= new Keyboard(this,R.xml.keyboard);
        mKeyboardView= findViewById(R.id.keyboardview);
        mKeyboardView.setKeyboard( mKeyboard );
        mKeyboardView.setEnabled(true);
        mKeyboardView.setOnKeyboardActionListener(this);
        imageView.setImageResource(R.drawable.thestartcollar);

        _answerLengthCount = 0;
        charViews = new TextView[_word.length()];
        for (int c = 0; c < _word.length(); c++) {
            charViews[c] = new TextView(this);
            charViews[c].setText("_ ");

            charViews[c].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            charViews[c].setGravity(Gravity.CENTER);
            charViews[c].setAllCaps(true);
            charViews[c].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
            charViews[c].setTextColor(Color.parseColor("#FFFFFF"));
            //charViews[c].setBackgroundResource(R.drawable.letter_bg);
            //add to layout
            textLayout.addView(charViews[c]);
        }

    }

    public void showHelp(){
        AlertDialog.Builder helpBuild = new AlertDialog.Builder(this);

        helpBuild.setTitle("Help");
        helpBuild.setMessage("Guess the word by selecting the letters.\n\n"
                + "You only have 6 wrong selections then it's game over!");
        helpBuild.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        helpAlert.dismiss();
                    }});
        helpAlert = helpBuild.create();

        helpBuild.show();
    }

    public void showDialog(){

        if (_answerLengthCount == _word.length() && _count<6) {

            // Display Alert Dialog
            AlertDialog.Builder winBuild = new AlertDialog.Builder(this);
            winBuild.setTitle("YAY");
            winBuild.setMessage("You win!\n\nThe answer was:\n\n"+_word);
            winBuild.setPositiveButton("Play Again",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            HangmanActivity.this.initGame();
                        }});

            winBuild.setNegativeButton("Exit",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent myIntent = new Intent(HangmanActivity.this, FullscreenActivity.class);
                            startActivity(myIntent);
                        }});

            winBuild.show();
        }
        if (_answerLengthCount != _word.length() && _count>=6){
            // Display Alert Dialog
            Log.i("deaddiag", "dead");
            AlertDialog.Builder loseBuild = new AlertDialog.Builder(this);
            loseBuild.setTitle("OOPS");
            loseBuild.setMessage("You lose!\n\nThe answer was:\n\n"+_word);
            loseBuild.setPositiveButton("Play Again",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            HangmanActivity.this.initGame();
                        }});

            loseBuild.setNegativeButton("Exit",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent myIntent = new Intent(HangmanActivity.this, FullscreenActivity.class);
                            startActivity(myIntent);
                        }});

            loseBuild.show();

        }

    }
}
