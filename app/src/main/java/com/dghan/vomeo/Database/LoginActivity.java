package com.dghan.vomeo.Database;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dghan.vomeo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends Activity {
    static boolean loginDialog = true;
    private static final String TAG = LoginActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    DatabaseHandler databaseHandler = null;
    @Override
    public void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mAuth.getCurrentUser() != null){
            updateUser();
        } else {
            if(loginDialog == true) Login();
            else    Register();
        }
    }
    public void updateUser(){
        String username = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseHandler.setInstance(username.substring(0, username.indexOf("@")));
        databaseHandler = DatabaseHandler.getInstance();
        finish();
    }
    public void Register(){
        setContentView(R.layout.dialog_register);
//        EditText Email2 = findViewById(R.id.registerEdittextEmail);
//        Email2.setText(BuildConfig.APPLICATION_ID.toString());
        Button Register = findViewById(R.id.registerBtn);
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText Email = findViewById(R.id.registerEdittextEmail);
                EditText Password = findViewById(R.id.registerEdittextPassword);
                String email = String.valueOf(Email.getText());
                String password = String.valueOf(Password.getText());
                Register(email, password);
            }
        });
        TextView loginInstead = (TextView) findViewById(R.id.registerLoginInstead);
        loginInstead.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                loginDialog = true;//dialogNumber = 0;loggedIn = false;
                onRefresh();
            }
        });
    }
    public void Login(){
        setContentView(R.layout.dialog_login);

        Button Login = findViewById(R.id.btnLogin);
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText Email = findViewById(R.id.loginEmail);
                EditText Password = findViewById(R.id.loginPassword);
                String email = String.valueOf(Email.getText());
                String password = String.valueOf(Password.getText());
                Login(email, password);
            }
        });
        TextView registerInstead = (TextView) findViewById(R.id.registerInstead);
        registerInstead.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                loginDialog = false;//dialogNumber = 0;loggedIn = false;
                onRefresh();
            }
        });
        TextView loginProblem = (TextView) findViewById(R.id.loginProblem);
        loginProblem.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Lets just have a new account!", Toast.LENGTH_LONG);
                loginDialog = false;//dialogNumber = 0;loggedIn = false;
                onRefresh();
            }
        });
    }

    public void SignOut(){
        FirebaseAuth.getInstance().signOut();
        databaseHandler.delete();
    }
    public void Login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUser();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().toString(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
    public void Register(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            updateUser();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().toString(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
    public void UserInfo(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();
        }
    }
    public void onRefresh(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    /*public class LoginFacebook extends Activity{
        private LoginButton loginButton;
        private CallbackManager callbackManager;

        private FirebaseAuth firebaseAuth;
        private FirebaseAuth.AuthStateListener firebaseAuthListner;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            FacebookSdk.sdkInitialize(this.getApplicationContext());
            setContentView(R.layout.dialog_login);//activity_facebooklogin

            callbackManager = CallbackManager.Factory.create();

            loginButton = (LoginButton) findViewById(R.id.login_button);

            loginButton.setReadPermissions(Arrays.asList("email"));
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Toast.makeText(getApplicationContext(),"Cancel",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                }
            });

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuthListner = new FirebaseAuth.AuthStateListener(){
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if(user != null){
                        goMainScreen();
                    }
                }
            };
        }

        private void handleFacebookAccessToken(AccessToken accessToken) {
            AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),R.string.firebase_error_login, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            callbackManager.onActivityResult(requestCode,resultCode,data);
        }

        @Override
        protected void onStart() {
            super.onStart();
            firebaseAuth.addAuthStateListener(firebaseAuthListner);
        }

        @Override
        protected void onStop() {
            super.onStop();
            firebaseAuth.removeAuthStateListener(firebaseAuthListner);
        }

        public void goMainScreen(){
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
*/}
