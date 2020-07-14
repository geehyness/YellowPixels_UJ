package com.yukisoft.yellowpixels.JavaActivities.Home;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yukisoft.yellowpixels.JavaActivities.Home.Fragments.*;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.LoginActivity;
import com.yukisoft.yellowpixels.R;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                new HomeFragment()).commit();

        mAdView = findViewById(R.id.mainBannerAd);
        mAdView.loadAd(new AdRequest.Builder().build());
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
