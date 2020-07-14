package com.yukisoft.yellowpixels.JavaActivities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.yukisoft.yellowpixels.JavaActivities.Home.HomeActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.CategoryModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String CURRENT_USER = "curUser";
    public static final String CHAT = "chat";
    public static final String BUSINESS = "businessUser";
    public static final String ITEM = "item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        new Handler().postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseAuth.getInstance().getCurrentUser().reload()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                getUserInfo(user);
                            }
                        });
            } else {
                finish();
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            }
        }, 1000);

        /*ArrayList<CategoryModel> list = new ArrayList<>();
        list.add(new CategoryModel("", "Tutoring Services", "Tutoring Services"));

        for (CategoryModel c : list) {
            FirebaseFirestore.getInstance().collection(CollectionName.ITEM_CATEGORIES)
                    .document(c.getName().toLowerCase().replace(" ", "_"))
                    .set(c)
                    .addOnSuccessListener(s -> {
                        Toast.makeText(this, c.getName(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(null);
        }*/
    }

    private void getUserInfo(FirebaseUser user) {
        final String loginId = user.getUid();

        FirebaseFirestore.getInstance().collection(CollectionName.USERS)
                .document(loginId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserModel userModel = documentSnapshot.toObject(UserModel.class);
                    userModel.setId(documentSnapshot.getId());

                    if (!userModel.isVerified()) {
                        if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                            userModel.setVerified(true);
                            FirebaseFirestore.getInstance().collection(CollectionName.USERS)
                                    .document(userModel.getId())
                                    .set(userModel)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            startApp(userModel);
                                        }
                                    });
                        }
                    } else {
                        startApp(userModel);
                    }
                });
    }


    private void startApp(UserModel userModel) {
        if(userModel != null){
            String userJSON = (new Gson()).toJson(userModel);
            Intent i = new Intent(MainActivity.this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra(CURRENT_USER, userJSON);
            startActivity(i);
            finish();
        }
    }
}
