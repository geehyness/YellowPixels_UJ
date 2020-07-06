package com.yukisoft.yellowpixels.JavaActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.CategoryModel;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class AppAdminActivity extends AppCompatActivity {
    ArrayList<String> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_admin);
    }

    void write(int position) {
        if (position <= list.size()) {
            CategoryModel cat = new CategoryModel("", list.get(position), "");
            FirebaseFirestore ff = FirebaseFirestore.getInstance();
            ff.collection(CollectionName.ITEM_CATEGORIES).add(cat)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(AppAdminActivity.this, list.get(position), Toast.LENGTH_SHORT).show();
                        write(position + 1);
                    })
                    .addOnFailureListener(e -> Log.d("category", cat.getName()));
        }
    }
}
