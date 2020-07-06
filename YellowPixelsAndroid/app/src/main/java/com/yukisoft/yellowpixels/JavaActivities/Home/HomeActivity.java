package com.yukisoft.yellowpixels.JavaActivities.Home;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yukisoft.yellowpixels.JavaActivities.Home.Fragments.*;
import com.yukisoft.yellowpixels.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                new HomeFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = menuItem -> {
        Fragment selectedFrag = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                selectedFrag = new HomeFragment();
                break;
            /*case R.id.nav_search:
                selectedFrag = new SearchFragment();
                break;*/
            /*case R.id.nav_businesses:
                selectedFrag = new BusinessesFragment();
                break;*/
            case R.id.nav_chats:
                selectedFrag = new ChatsFragment();
                break;
            case R.id.nav_profile:
                selectedFrag = new ProfileFragment();
                break;
        }

        assert selectedFrag != null;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                selectedFrag).commit();
        return true;
    };
}
