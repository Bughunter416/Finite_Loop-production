package edu.siu.cs.www.parkingspotfinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private Button mLoginBtn;
    private Button mRegisterBtn;
    private EditText emailTextField;
    private EditText passwordTextField;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private static final String TAG = "LoginState";

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Create an instance of the database connection to firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Create the needed elements for the activity
        mLoginBtn = (Button) findViewById(R.id.loginBtn);
        mRegisterBtn = (Button) findViewById(R.id.registerBtn);
        emailTextField = (EditText) findViewById(R.id.emailField);
        passwordTextField = (EditText) findViewById(R.id.passwordField);

        // Set up Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // Test edit

        // Listen for login button click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                // Show the user that they are being logged in
                progressDialog = ProgressDialog.show(LoginActivity.this, "Login", "Logging into your account.", true);

                //Get the information from the needed fields
                String email = emailTextField.getText().toString().trim();
                String password = passwordTextField.getText().toString().trim();

                Log.d(TAG, "EMAIL: " + email);

                // Validate the login credentials before logging in
                if (email.matches("") || password.matches("")){
                    Toast.makeText(LoginActivity.this, "Unable to authenticate. Provide all information!", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                } else {
                    //Sign in the user
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(!task.isSuccessful()){
                                        Log.w(TAG, "signInWithEmail" + task.getException().getMessage());
                                        Toast.makeText(LoginActivity.this, "Authentication Failed!",
                                                Toast.LENGTH_LONG).show();
                                        progressDialog.dismiss();
                                    } else {
                                        Intent startMainView = new Intent(LoginActivity.this, MapActivity.class);
                                        startActivity(startMainView);
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                }
            }
        });

        // Starts the register activity
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new intent and start it
                Intent registerActivity = new Intent(LoginActivity.this, RegisterActivitty.class);
                startActivity(registerActivity);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
