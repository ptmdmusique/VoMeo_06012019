package com.dghan.vomeo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
//import android.widget.ListAdapter;
import com.dghan.vomeo.Database.Word;
import com.dghan.vomeo.Flashcard.flashcardActivity;
import com.dghan.vomeo.Hangman.HangmanActivity;
import com.dghan.vomeo.Test.test_activity;
import com.dghan.vomeo.UI.BugReport;
import com.dghan.vomeo.UI.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dghan.vomeo.Database.Database;
import com.dghan.vomeo.Database.DatabaseHandler;
import com.dghan.vomeo.Database.LoginActivity;
import com.dghan.vomeo.R;
import com.dghan.vomeo.UI.Card;
import com.dghan.vomeo.UI.SettingActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.Comparator;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity  implements
        NavigationView.OnNavigationItemSelectedListener,
        AdapterView.OnItemSelectedListener{
    DatabaseHandler databaseHandler = null;

    //Code part:
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private ImageButton mShuffle, userImage, userName;
    private Spinner mSpinner;
    private ListView mCardsList;
    private ListAdapter mListAdapter;
    private NavigationView navigationView;
    private View headerView;
    public static FullscreenActivity instance;
    boolean refresh = false;
    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 0;
    private TextView mAddBtn;
    //Floating button
    private FloatingActionButton gameCenter, testCenter, fab, fabDismiss, flashcardCenter;
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    public void login(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent login = new Intent(FullscreenActivity.this, LoginActivity.class);
            startActivity(login);
            refresh = true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        setContentView(R.layout.activity_fullscreen);
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mCardsList = (ListView) findViewById(R.id.CList);
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        if(FirebaseAuth.getInstance().getCurrentUser() == null)
            login();
        else
            main();
        floatingButtonActivity();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if(refresh) {
            databaseHandler.words = databaseHandler.data.words;
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.welcome_to_vo_meo);
            mediaPlayer.start();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
            public void run() {
                main();
            }
        }, 8000);
            refresh = false;
        } else {
            databaseHandler = DatabaseHandler.getInstance();
            mListAdapter = new ListAdapter(getApplicationContext());
            mListAdapter.syncDatabaseHandler();
            mCardsList.setAdapter(mListAdapter);
        }
    }

    public void main(){
        databaseHandler = DatabaseHandler.getInstance();
        findViewById(R.id.fab).setVisibility(View.VISIBLE);
        mListAdapter = new ListAdapter(getApplicationContext());
        mListAdapter.syncDatabaseHandler();
        mCardsList.setAdapter(mListAdapter);
        //Code:
        mDrawerLayout=(DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(FullscreenActivity.this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView=(NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView username = navigationView.getHeaderView(0).findViewById(R.id.user_name);
        username.setText(databaseHandler.data.username);

        //Spinner
        mSpinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Level, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        //mSpinner.setOnItemSelectedListener(this);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = adapterView.getItemAtPosition(i);
                if(item.toString().equals("All")){
                    databaseHandler.getLevel(0);
                }else if (item.toString().equals("Basic")){
                    databaseHandler.getLevel(1);
                }else if (item.toString().equals("Intermediate")){
                    databaseHandler.getLevel(2);
                }else if (item.toString().equals("Advanced")){
                    databaseHandler.getLevel(3);
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        databaseHandler = DatabaseHandler.getInstance();
                        mListAdapter = new ListAdapter(getApplicationContext());
                        mListAdapter.syncDatabaseHandler();
                        mCardsList.setAdapter(mListAdapter);
                    }
                }, 3000);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Shuffle
        final int[] flagShuffle = {0}; //0: all    1: shuffle
        mShuffle = (ImageButton) findViewById(R.id.shuffle);
        mShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flagShuffle[0] == 0) {  //if current state is all, perform shuffle's features and change icon shuffle, set flag to 1
                    mShuffle.setImageDrawable(getResources().getDrawable(R.drawable.shuffle));
                    //mShuffle.setBackground(getResources().getDrawable(R.drawable.shuffle));
                    //Shuffle features
                    Toast.makeText(FullscreenActivity.this, "Shuffle", Toast.LENGTH_SHORT).show();

                    flagShuffle[0] = 1;

                    Collections.shuffle(databaseHandler.words);
                    mCardsList.invalidateViews();// reset the list view??
                }
                else if (flagShuffle[0] == 1){ //if current state is shuffle, perform all's features and change icon all, set flag to 0
                    mShuffle.setImageDrawable(getResources().getDrawable(R.drawable.loop));
                    //mShuffle.setBackground(getResources().getDrawable(R.drawable.loop));
                    //All features
                    Toast.makeText(FullscreenActivity.this, "All", Toast.LENGTH_SHORT).show();

                    flagShuffle[0] = 0;

                    Collections.sort(databaseHandler.words, new Comparator<Word>() {
                        @Override
                        public int compare(Word c1, Word c2) {
                            return c1.index - c2.index;
                        }
                    });
                    mCardsList.invalidateViews();
                }
            }
        });

        //Header
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        headerView = navigationView.getHeaderView(0);
        userImage = (ImageButton) headerView.findViewById(R.id.user_image);
            //Change avt
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

        //Report Bug Button
        mAddBtn = findViewById(R.id.addBtn);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bugIntent = new Intent(FullscreenActivity.this, BugReport.class);
                startActivity(bugIntent);
            }
        });
    }

    private void SelectImage() {
        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")){

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);

                }else if (items[i].equals("Gallery")){

                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Select File"), SELECT_FILE);

                }else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_CAMERA){
                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                userImage.setImageBitmap(bmp);

            }else if (requestCode == SELECT_FILE){
                Uri selectImageUri = data.getData();
                userImage.setImageURI(selectImageUri);
            }
        }
    }


    public static FullscreenActivity getInstance(){
        return instance;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    //Code
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        /*take all items*/
        int id = menuItem.getItemId();
        /*Features in drawer*/
        if(id == R.id.home){
            Toast.makeText(this, "This is home", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.setting){
            Intent intent = new Intent(FullscreenActivity.this, SettingActivity.class);
            startActivity(intent);
        }

        if(id == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            DatabaseHandler databaseHandler = DatabaseHandler.getInstance();
            databaseHandler.delete();
            mCardsList.invalidateViews();
            login();
        }

        if(id == R.id.aboutus){
            Toast.makeText(this, "This is AboutUs", Toast.LENGTH_SHORT).show();
        }

        if(id == R.id.help){
            Toast.makeText(this, "This is Help", Toast.LENGTH_SHORT).show();
        }

        if(id == R.id.profile){
            Toast.makeText(this, "This is Profile", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.favorite_list){
            Toast.makeText(this, "This is Favorite List", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) { // Level code
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    protected void floatingButtonActivity (){
        gameCenter = findViewById(R.id.game_center);
        testCenter = findViewById(R.id.test);
        flashcardCenter = findViewById(R.id.flashcard);
        fab = findViewById(R.id.fab);
        fabDismiss = findViewById(R.id.fab_dismiss);

        fab.setImageBitmap(textAsBitmap("+",20, Color.WHITE));
        fabDismiss.setImageBitmap(textAsBitmap("X", 20, Color.WHITE));
        //gameCenter.setImageResource();
        //testCenter.setImageResource();
        //flashcardCenter.setImageResource();

        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                fab.hide();
                fabDismiss.show();
                gameCenter.show();
                testCenter.show();
                flashcardCenter.show();
            }
        });
        fabDismiss.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                fab.show();
                fabDismiss.hide();
                gameCenter.hide();
                testCenter.hide();
                flashcardCenter.hide();
            }
        });
        gameCenter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                Intent intent = new Intent(FullscreenActivity.this, HangmanActivity.class);
                startActivity(intent);
            }
        });
        testCenter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                //move to test activity
                Intent intent = new Intent(FullscreenActivity.this, test_activity.class);
                startActivity(intent);
            }
        });
        flashcardCenter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                //move to flashcard activity
                Intent intent = new Intent(FullscreenActivity.this, flashcardActivity.class);
                startActivity(intent);
            }
        });
    }

    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }
}
