package com.dghan.vomeo.Test;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.dghan.vomeo.Database.DatabaseHandler;
import com.dghan.vomeo.FullscreenActivity;
import com.dghan.vomeo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class test_activity extends AppCompatActivity {
    private String[] terms;
    private String[] definitions;
    private int questionNumber=0;
    private int correctNumber=0;
    private String answerString;
    private List<Integer> order;
    private String TAG = "MyActivity:: checking for correctness ";

    EditText answerEditText;
    TextView question, correct_number;
    ImageButton button_next;

    //Firework
    FireworkScene firework;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qna_layout);
        retrieveData();
        receiveQuestions();
        initLayout();
        nextButtonOnClicked();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(test_activity.this, FullscreenActivity.class));
        firework.Stop();
    }


    private void receiveQuestions() {
        order = new ArrayList<>();
        for (int i=0; i<terms.length; i++){
            order.add(i);
        }
        Collections.shuffle(order);
    }

    private void initLayout() {
        question = (TextView) findViewById(R.id.question);
        question.setText(definitions[order.get(0)]);

        answerEditText = findViewById(R.id.answer);
        answerEditText.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event != null &&
                                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            if (event == null || !event.isShiftPressed()) {
                                // the user is done typing.
                                nextQuestionTriggered();

                                return true; // consume.
                            }
                        }
                        return false; // pass on to other listeners.
                    }
                }
        );

        button_next = findViewById(R.id.button_next);
    }
    private void nextButtonOnClicked(){
        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextQuestionTriggered();
            }
        });
    }
    private void nextQuestionTriggered(){
        answerString = answerEditText.getText().toString().toLowerCase();
        if (answerString.equals(terms[order.get(questionNumber)])==false) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else vibrator.vibrate(500);
            Log.d(TAG, "Answer is wrong! "+ answerString + " " + terms[questionNumber]);
        } else if (answerString.equals(terms[order.get(questionNumber)])==true){
            correctNumber++;
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.audio_correct);
            mediaPlayer.start();
            Log.d(TAG, "Answer is correct!");
        }
        questionNumber++;
        answerEditText.setText("");
        getQuestion();
    }
    private void getQuestion(){
        if (questionNumber<11)
            question.setText(definitions[order.get(questionNumber)]);
        else celebrateLayout();
    }

    private void celebrateLayout(){
        setContentView(R.layout.result_layout);
        correct_number = findViewById(R.id.result);
        correct_number.setText("Your correct answer: " + Integer.toString(correctNumber));

        firework = new FireworkScene(this);
        ImageButton backToMenuButton = findViewById(R.id.back_to_main_menu);
        backToMenuButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(test_activity.this, FullscreenActivity.class));
                firework.Stop();
            }
        });
        RelativeLayout surface = findViewById( R.id.surface );
        surface.addView( firework );
    }

    private void retrieveData() {
        DatabaseHandler databaseHandler = DatabaseHandler.getInstance();
        terms = new String[databaseHandler.words.size()];
        definitions = new String[databaseHandler.words.size()];
        for (int i=0; i<databaseHandler.words.size(); i++){
            terms[i] = databaseHandler.words.get(i).term;
            definitions[i] = databaseHandler.words.get(i).definition;
        }
    }

}