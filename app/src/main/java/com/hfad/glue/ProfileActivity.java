package com.hfad.glue;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private Button mProfileSendButton,mProfileDeclineBtn;
    private DatabaseReference mUsersDatabase;
    private FirebaseUser mCurrentUser;
    private String mcurrent_state;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mRouteRef;

    private DatabaseReference mNotificationDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("user_id");


        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase= FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRouteRef=FirebaseDatabase.getInstance().getReference();

        mCurrentUser=FirebaseAuth.getInstance().getCurrentUser();


      mProfileImage= (ImageView) findViewById(R.id.profile_image);
      mProfileName = (TextView) findViewById(R.id.profile_displayName);
      mProfileStatus = (TextView) findViewById(R.id.profile_status);
      mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
      mProfileSendButton = (Button) findViewById(R.id.profile_send_req_btn);
      mProfileDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);

      mcurrent_state="not_friends";

      mProgressDialog = new ProgressDialog(this);
      mProgressDialog.setTitle("Loading user data");
      mProgressDialog.setMessage("Please wait while we load the data");
      mProgressDialog.setCanceledOnTouchOutside(false);
      mProgressDialog.show();



      mUsersDatabase.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {

              String display_name=dataSnapshot.child("name").getValue().toString();
              String image=dataSnapshot.child("image").getValue().toString();
              String status=dataSnapshot.child("status").getValue().toString();

              mProfileName.setText(display_name);
              mProfileStatus.setText(status);

              Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.avatar).into(mProfileImage);


              //------------------------------------------FRIEND LIST -----------------------------------

              mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(DataSnapshot dataSnapshot) {
                      if(dataSnapshot.hasChild(user_id)){
                          String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                          if(req_type.equals("received")){

                              mcurrent_state = "req_received";
                              mProfileSendButton.setText("Accept friend Request");

                              mProfileDeclineBtn.setVisibility(View.VISIBLE);
                              mProfileDeclineBtn.setEnabled(true);

                          }else if(req_type.equals("sent")){
                                mcurrent_state="req_sent";
                                mProfileSendButton.setText("Cancel Friend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                          }

                          mProgressDialog.dismiss();
                      }else {

                          mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                              @Override
                              public void onDataChange(DataSnapshot dataSnapshot) {


                                  if(dataSnapshot.hasChild(user_id)){

                                      mcurrent_state = "friends";
                                      mProfileSendButton.setText("Un-friend");

                                      mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                      mProfileDeclineBtn.setEnabled(false);
                                  }

                                  mProgressDialog.dismiss();

                              }

                              @Override
                              public void onCancelled(DatabaseError databaseError) {

                                  mProgressDialog.dismiss();


                              }
                          });
                      }




                  }

                  @Override
                  public void onCancelled(DatabaseError databaseError) {

                  }
              });


          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      });


      mProfileSendButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              mProfileSendButton.setEnabled(false);

              if(mcurrent_state.equals("not_friends")){

                  DatabaseReference mNotificationRef=mRouteRef.child("notifications").child(user_id).push();
                  String newNotificationId = mNotificationRef.getKey();

                  HashMap<String,String> notificationData = new HashMap<>();
                  notificationData.put("from",mCurrentUser.getUid());
                  notificationData.put("type","request");

                  Map request_map = new HashMap();
                  request_map.put("Friend_req/" + mCurrentUser.getUid() + "/"+ user_id + "/request_type", "sent");
                  request_map.put("Friend_req/" + user_id + "/"+ mCurrentUser.getUid() + "/request_type", "received");
                  request_map.put("notifications/"+ user_id + newNotificationId,notificationData );

                  mRouteRef.updateChildren(request_map, new DatabaseReference.CompletionListener() {
                      @Override
                      public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                          if(databaseError!=null){
                              Toast.makeText(ProfileActivity.this,"There was an error",Toast.LENGTH_SHORT).show();
                          }
                          mProfileSendButton.setEnabled(true);

                          mcurrent_state = "req_sent";
                          mProfileSendButton.setText("Cancel friend Request");

                      }
                  });

              }

              if(mcurrent_state.equals("req_sent")){
                  mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void aVoid) {

                          mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void aVoid) {

                                  mProfileSendButton.setEnabled(true);
                                  mcurrent_state = "not_friends";
                                  mProfileSendButton.setText("Send friend Request");
                                  mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                  mProfileDeclineBtn.setEnabled(false);
                              }
                          });

                      }
                  });
              }

              //------------------REQ RECEIVED----------------------

              if(mcurrent_state.equals("req_received")){

                  final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                  Map friendsMap = new HashMap();
                  friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date",currentDate);
                  friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date",currentDate);

                  friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id,null);
                  friendsMap.put("Friend_req/" + user_id + mCurrentUser.getUid(),null);

                  mRouteRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                      @Override
                      public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                          if(databaseError == null){
                              mProfileSendButton.setEnabled(true);
                              mcurrent_state = "friends";
                              mProfileSendButton.setText("un-friend");

                              mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                              mProfileDeclineBtn.setEnabled(false);
                          }else{
                              String error = databaseError.getMessage();
                              Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                          }

                      }
                  });
              }

              //---------------UNFRIEND-------------------------

              if(mcurrent_state.equals("friends")){

                  Map unFriendsMap = new HashMap();
                  unFriendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id,null);
                  unFriendsMap.put("Friends/" + user_id + mCurrentUser.getUid(),null);

                  mRouteRef.updateChildren(unFriendsMap, new DatabaseReference.CompletionListener() {
                      @Override
                      public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                          if(databaseError == null){
                              mcurrent_state = "not_friends";
                              mProfileSendButton.setText("send friend req");

                              mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                              mProfileDeclineBtn.setEnabled(false);
                          }else{
                              String error = databaseError.getMessage();
                              Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                          }

                          mProfileSendButton.setEnabled(true);
                      }
                  });

              }

          }

      });


    }
}
