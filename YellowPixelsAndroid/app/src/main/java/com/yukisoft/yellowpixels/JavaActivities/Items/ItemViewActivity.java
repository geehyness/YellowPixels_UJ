package com.yukisoft.yellowpixels.JavaActivities.Items;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yukisoft.yellowpixels.JavaActivities.Home.HomeActivity;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.BusinessViewActivity;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.ChatActivity;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Adapters.ImageAdapter;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ItemModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;
import java.util.Objects;

public class ItemViewActivity extends AppCompatActivity {
    private ItemModel item;
    private UserModel currentUser, business;

    private TextView txtName, txtPrice, txtBusiness, txtDetails;
    private ImageView itemPic, contactBusiness;

    private ImageAdapter imageAdapter;

    ArrayList<String> imagesUri = new ArrayList<>();

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);

        Intent i = getIntent();
        item = (new Gson()).fromJson(i.getStringExtra(MainActivity.ITEM), ItemModel.class);
        currentUser = (new Gson()).fromJson(i.getStringExtra(MainActivity.CURRENT_USER), UserModel.class);

        txtName = findViewById(R.id.txtItemViewName);
        txtPrice = findViewById(R.id.txtItemViewPrice);
        txtBusiness = findViewById(R.id.txtItemViewBusiness);
        txtDetails = findViewById(R.id.txtItemViewDetails);
        itemPic = findViewById(R.id.itemPic);
        contactBusiness = findViewById(R.id.contactBusiness);

        txtBusiness.setVisibility(View.GONE);
        contactBusiness.setVisibility(View.GONE);

        Button sold = findViewById(R.id.btnSold);
        sold.setVisibility(View.GONE);
        Button del = findViewById(R.id.btnDelete);
        del.setVisibility(View.GONE);

        if (currentUser != null) {
            if (item.getUserId().equals(currentUser.getId())) {
                sold.setVisibility(View.VISIBLE);
                sold.setOnClickListener(v -> {
                    new AlertDialog.Builder(ItemViewActivity.this, R.style.MyDialogTheme)
                            .setIcon(R.drawable.ic_baseline_warning)
                            .setTitle("Sold?")
                            .setMessage("Are you sure you want to mark this item as sold?\nItems marked as sold will no longer appear on the application!")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                item.setSold(true);

                                FirebaseFirestore.getInstance().collection(CollectionName.ITEMS)
                                        .document(item.getId())
                                        .set(item)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(ItemViewActivity.this, "Item marked as sold!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ItemViewActivity.this, "Failed to change item status.\nPlease Check your internet status!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            })
                            .setNegativeButton("No", null)
                            .show();
                });

                del.setVisibility(View.VISIBLE);
                del.setOnClickListener(v -> {
                    new AlertDialog.Builder(ItemViewActivity.this, R.style.MyDialogTheme)
                            .setIcon(R.drawable.ic_baseline_warning)
                            .setTitle("Delete?")
                            .setMessage("Are you sure you want to delete this item?\nItems marked as sold will no longer appear on the application!")
                            .setPositiveButton("Yes", (dialog, which) -> {

                                FirebaseFirestore.getInstance().collection(CollectionName.ITEMS_DELETED)
                                        .document(item.getId())
                                        .set(item)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FirebaseFirestore.getInstance().collection(CollectionName.ITEMS)
                                                        .document(item.getId())
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(ItemViewActivity.this, "Item deleted!", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ItemViewActivity.this, "Failed to delete item.\nPlease Check your internet status!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }
        }

        Picasso.with(getBaseContext())
                .load(item.getImages().get(0))
                .placeholder(R.drawable.ic_baseline_photo)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(itemPic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(getBaseContext())
                                .load(item.getImages().get(0))
                                .error(R.drawable.ic_baseline_photo)
                                .placeholder(R.drawable.ic_baseline_photo)
                                .into(itemPic, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso","Could not fetch image");
                                    }
                                });
                    }
                });

        txtName.setText(item.getName());
        txtPrice.setText(String.format("R %.2f", item.getPrice()));
        txtDetails.setText(item.getDetails());
        //itemPic.setImageURI(imagesUri.get(0));

        imageRecyclerInit();

        FirebaseFirestore ff = FirebaseFirestore.getInstance();
        ff.collection(CollectionName.USERS).document(item.getUserId()).get()
            .addOnSuccessListener(documentSnapshot -> {
                business = documentSnapshot.toObject(UserModel.class);
                txtBusiness.setText(String.format("Sold by %s", Objects.requireNonNull(business).getName()));
                contactBusiness.setVisibility(View.VISIBLE);
                txtBusiness.setVisibility(View.VISIBLE);
            }).addOnFailureListener(e -> {
                e.printStackTrace();
                Toast.makeText(this, "Error getting business details!", Toast.LENGTH_SHORT).show();
            });

        contactBusiness.setOnClickListener(v -> {
            Intent intent = new Intent(ItemViewActivity.this, ChatActivity.class);
            intent.putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(currentUser));
            intent.putExtra(MainActivity.BUSINESS, (new Gson()).toJson(business));
            intent.putExtra(MainActivity.ITEM, (new Gson()).toJson(item));

            startActivity(intent);
        });

        txtBusiness.setOnClickListener(v -> {
            Intent intent = new Intent(ItemViewActivity.this, BusinessViewActivity.class);
            intent.putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(currentUser));
            intent.putExtra(MainActivity.BUSINESS, (new Gson()).toJson(business));

            startActivity(intent);
        });
    }

    private void imageRecyclerInit() {
        RecyclerView catRecyclerView = findViewById(R.id.itemPicsRecycler);
        catRecyclerView.setHasFixedSize(false);
        imageAdapter = new ImageAdapter(getBaseContext(), item.getImages());
        RecyclerView.LayoutManager catLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        catRecyclerView.setLayoutManager(catLayoutManager);
        catRecyclerView.setAdapter(imageAdapter);
        imageAdapter.setOnItemClickListener(position ->
            Picasso.with(getBaseContext())
                .load(item.getImages().get(position))
                .placeholder(R.drawable.ic_baseline_photo)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(itemPic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(getBaseContext())
                                .load(item.getImages().get(position))
                                .error(R.drawable.ic_baseline_photo)
                                .placeholder(R.drawable.ic_baseline_photo)
                                .into(itemPic, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso","Could not fetch image");
                                    }
                                });
                    }
                }));
    }
}
