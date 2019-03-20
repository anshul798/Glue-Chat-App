package com.hfad.glue;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseReference mFirebaseDatabase;

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private ProgressDialog mRegProgress;

    private TextInputLayout displayName,email,password;
    private Button createButton;

    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mToolbar=(Toolbar) findViewById(R.id.reg_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        displayName=(TextInputLayout) findViewById(R.id.reg_display_name);
        email=(TextInputLayout) findViewById(R.id.reg_email);
        password=(TextInputLayout) findViewById(R.id.reg_password);
        createButton=(Button) findViewById(R.id.reg_create_btn);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name_string=displayName.getEditText().getText().toString();
                String email_string=email.getEditText().getText().toString();
                String password_string=password.getEditText().getText().toString();

                 if(!TextUtils.isEmpty(display_name_string) || !TextUtils.isEmpty(email_string) || !TextUtils.isEmpty(password_string)){
                     mRegProgress.setTitle("Registering User");
                     mRegProgress.setMessage("Please wait while we create your account  !");
                     mRegProgress.setCanceledOnTouchOutside(false);
                     mRegProgress.show();
                     register_user(display_name_string,email_string,password_string);

                 }
            }
        });
    }

    private void register_user(final String display_name_string, String email_string, String password_string) {
        mAuth.createUserWithEmailAndPassword(email_string, password_string)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information


                            FirebaseUser current_user= FirebaseAuth.getInstance().getCurrentUser();
                            String uid=current_user.getUid();

                            mFirebaseDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String,String> userMap = new HashMap<>();
                            userMap.put("name",display_name_string);
                            userMap.put("status","Hi there, I am using Glue");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");

                            mFirebaseDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        mRegProgress.dismiss();

                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                        String currentUserID=mAuth.getCurrentUser().getUid();

                                        mUserDatabase.child(currentUserID).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                                startActivity(mainIntent);
                                                finish();

                                            }
                                        });


                                    }
                                }
                            });



                        } else {

                            // If sign in fails, display a message to the user.
                            mRegProgress.hide();
                            Toast.makeText(RegisterActivity.this, "Please check your email and password",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }
}
