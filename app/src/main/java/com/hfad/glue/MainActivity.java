package com.hfad.glue;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private FirebaseAuth mAuth;
    private ViewPager viewPager;
    private Toolbar mToolbar;

    private TabLayout  mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        viewPager=(ViewPager) findViewById(R.id.main_tab_pager);
        mToolbar=(Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Glue");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout=(TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser==null){
            sendToStart();
        }
    }

    public void sendToStart(){
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.main_logout_button){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if(item.getItemId()==R.id.main_settings_btn){
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        }

        if(item.getItemId()==R.id.main_users_btn){
            Intent intent =new Intent (MainActivity.this,UsersActivity.class);
            startActivity(intent);
        }

        return true;
    }
}
