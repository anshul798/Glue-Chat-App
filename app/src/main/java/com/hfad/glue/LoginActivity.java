package com.hfad.glue;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    private Button mLoginButton;
    private FirebaseAuth mAuth;
    private ProgressDialog mRegProgress;

    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar=(Toolbar) findViewById(R.id.log_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("LogIn");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mLoginEmail=(TextInputLayout) findViewById(R.id.log_email);
        mLoginPassword=(TextInputLayout) findViewById(R.id.log_password);
        mLoginButton=(Button) findViewById(R.id.log_btn);


        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email=mLoginEmail.getEditText().getText().toString();
                String password=mLoginPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

                    mRegProgress.setTitle("Logging in");
                    mRegProgress.setMessage("Please wait while we Log you in !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    loginUser(email,password);
                }

            }
        });
    }

    private void loginUser(String email,String password){

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            mRegProgress.dismiss();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String currentUserID=mAuth.getCurrentUser().getUid();

                            mUserDatabase.child(currentUserID).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Intent start=new Intent(LoginActivity.this,MainActivity.class);

                                    start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(start);
                                    finish();

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            mRegProgress.hide();
                            Toast.makeText(LoginActivity.this, "Please check your email and password",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }
}
