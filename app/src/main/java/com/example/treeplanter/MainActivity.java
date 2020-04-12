package com.example.treeplanter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private TextView pwTV;
    private FirebaseUser currentUser;
    private boolean isEmailVerified;
    // 1 = email sent, 0 = email not yet sent
    private int emailSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hide the status bar
        //Reference: https://developer.android.com/training/system-ui/status
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Initialise Firebase Auth & set Database Reference
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        //Assign Views to variables
       email = findViewById(R.id.emailEditText);
       password = findViewById(R.id.pwEditText);
       pwTV = findViewById(R.id.reset_pwTV);
       //Change colour of Reset Password link to Blue
       pwTV.setTextColor(Color.parseColor("#0000EE"));
    }

    public void loginButton(View view) {
        if (email.getText().toString().contains(".") && email.getText().toString().contains("@")) {
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // User has successfully signed in
                                Log.d(TAG, "signInWithEmail:success");
                                //checkIfEmailVerified();
                                // procceed to the next page
                                logIn();

                            } else {
                                createAccount();
                            }
                        }
                    });
        }else{
            Toast.makeText(this, "Invalid email entered", Toast.LENGTH_LONG).show();
        }
    }

    private void logIn() {
        //intent to change to maps

        Intent intent = new Intent(this,LandingActivity.class);
        this.startActivity(intent);
    }

    private void createAccount() {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // user is logged in
                            // set current user
                            currentUser = mAuth.getCurrentUser();
                            //set up database for this user
                            myRef.child("users").child(task.getResult().getUser().getUid()).child("email")
                                    .setValue(email.getText().toString());
                            // check if email is verified
                            checkIfEmailVerified();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "An error occured: ", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //https://stackoverflow.com/questions/40404567/how-to-send-verification-email-with-firebase
    private void verificationEmail()
    {
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (emailSent == 0){
            currentUser.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // if email is sent
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "A verification link has been sent to this email address, this must be clicked before you can log in!", Toast.LENGTH_LONG).show();
                                //set variable to true to track email being sent
                                emailSent = 1;
                                //logout the user and finish this activity
                                FirebaseAuth.getInstance().signOut();
                                //refresh activity
                                refreshActivity();
                            }
                            else
                            {
                                // if the email is not sent - display message and restart the activity
                                Toast.makeText(MainActivity.this, "An error occured in send the verification email.", Toast.LENGTH_SHORT).show();
                                emailSent = 0;

                                //restart this activity
                                refreshActivity();


                            }
                        }
                    });
        }else{
            // if email has already been sent and user has not verified account, give users option to resend.
            displayAlert("Resend Verification Email","To have the verification email resent to your emaill address click 'Resend Email' below.", true);
        }
    }

    private void refreshActivity() {
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
    }

    private void displayAlert(@NonNull String title,
                              @Nullable String message,
                              boolean resendEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);

        if (resendEmail) {
            builder.setPositiveButton("Resend Email",
                    (DialogInterface dialog, int index) -> {
                        emailSent = 0;
                        verificationEmail();
                    });
        } else {
            builder.setPositiveButton("No, thanks", null);
        }
        builder.create().show();
    }

    //https://stackoverflow.com/questions/40404567/how-to-send-verification-email-with-firebase
    private void  checkIfEmailVerified(){
        if (currentUser != null && currentUser.isEmailVerified())
        {
            // user has been verified, login
            logIn();
        }
        else
        {
            // send verification email
            verificationEmail();
        }
    }

//reference: https://stackoverflow.com/questions/42800349/forgot-password-in-firebase-for-android
    public void resetPW_btn(View view) {
        if (email.getText().toString() != null && email.getText().toString().contains("@")) {
            mAuth.sendPasswordResetEmail(email.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "An email link has been sent to your email address", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Email sent.");

                                pwTV.setTextColor(Color.parseColor("#551A8B"));
                            }
                        }
                    });
        }else{
            Toast.makeText(this, "You must enter your email address, above, before resetting password", Toast.LENGTH_SHORT).show();
        }

    }

}
