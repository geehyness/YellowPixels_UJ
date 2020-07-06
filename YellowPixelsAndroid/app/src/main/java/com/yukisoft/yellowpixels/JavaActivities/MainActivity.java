package com.yukisoft.yellowpixels.JavaActivities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        new Handler().postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                               FirebaseFirestore ff = FirebaseFirestore.getInstance();
                final String loginId = user.getUid();

                final DocumentReference userDoc = ff.document(CollectionName.USERS+"/"+loginId);
                userDoc.get().addOnSuccessListener(documentSnapshot -> {
                    UserModel userModel = documentSnapshot.toObject(UserModel.class);

                    if(userModel != null){
                        String userJSON = (new Gson()).toJson(userModel);
                        Intent i = new Intent(MainActivity.this, HomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.putExtra(CURRENT_USER, userJSON);
                        startActivity(i);
                        finish();
                    }
                });
            } else {
                finish();
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            }
        }, 1000);

        /*ArrayList<CategoryModel> list = new ArrayList<>();
        list.add(new CategoryModel("", "Book", "Book"));
        list.add(new CategoryModel("", "Electronic Gadget", "Electronic Gadjet"));
        list.add(new CategoryModel("", "Other", "Other"));

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
}
