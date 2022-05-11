package com.bytenine.tinnitusrelief.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.bytenine.tinnitusrelief.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Settings extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.page_4);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        this.setFinishOnTouchOutside(false);

    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {


                if (item.getItemId() == R.id.page_1)
                {
                    Log.e ("navbar_bottom", "item 1 clicked");

                    Intent intent = new Intent(this, Home.class);
                    startActivity(intent);
                    finish();

                    return true;
                }
                else if (item.getItemId() == R.id.page_2)
                {
                    Log.e ("navbar_bottom", "item 2 clicked");
                    finish();


                    return true;


                }
                else if (item.getItemId() == R.id.page_3)
                {
                    Log.e ("navbar_bottom", "item 3 clicked");

                    Intent intent = new Intent(this, Tools.class);
                    startActivity(intent);
                    finish();

                    return true;


                }
                else if (item.getItemId() == R.id.page_4)
                {
                    Log.e ("navbar_bottom", "item 4 clicked");

                 //   Intent intent = new Intent(this, Settings.class);
                  //  startActivity(intent);


                    return true;


                }
                else {
                    return false;
                }





            };

    @Override
    public void onStart() {
        super.onStart();
        bottomNavigation.setSelectedItemId(R.id.page_4);

    }
}