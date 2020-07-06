package com.yukisoft.yellowpixels.JavaActivities.UserManagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;
import com.yukisoft.yellowpixels.JavaActivities.Home.HomeActivity;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.R;

import java.util.Objects;

public class AddAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        unregisteredControls();
    }

    private void unregisteredControls(){
        CardView login = findViewById(R.id.btnPLogin);
        login.setOnClickListener(view -> {
            startActivity(new Intent(AddAccountActivity.this, LoginActivity.class));
            finish();
        });

        CardView register = findViewById(R.id.btnPRegister);
        register.setOnClickListener(view -> {
            startActivity(new Intent(AddAccountActivity.this, RegisterActivity.class));
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AddAccountActivity.this, HomeActivity.class));
        finish();
    }
}
