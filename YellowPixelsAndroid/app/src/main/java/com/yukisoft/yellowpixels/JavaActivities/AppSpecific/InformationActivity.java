package com.yukisoft.yellowpixels.JavaActivities.AppSpecific;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;
import com.yukisoft.yellowpixels.JavaActivities.Home.HomeActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.R;

public class InformationActivity extends AppCompatActivity {
    private UserModel currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        Intent i = getIntent();
        currentUser = (new Gson()).fromJson(i.getStringExtra(MainActivity.CURRENT_USER), UserModel.class);

        TextView privacy = findViewById(R.id.privacy);
        TextView terms = findViewById(R.id.terms);

        privacy.setOnClickListener(v -> startActivity(new Intent(InformationActivity.this, PrivacyActivity.class)));
        terms.setOnClickListener(v -> startActivity(new Intent(InformationActivity.this, TermsActivity.class)));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class)
                .putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(currentUser)));
        finish();
    }
}
