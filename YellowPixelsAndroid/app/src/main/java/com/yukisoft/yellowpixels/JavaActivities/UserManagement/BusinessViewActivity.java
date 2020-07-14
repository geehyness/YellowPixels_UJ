package com.yukisoft.yellowpixels.JavaActivities.UserManagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yukisoft.yellowpixels.JavaActivities.Items.ItemViewActivity;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Adapters.ItemAdapter;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.AccountType;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ItemModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;

public class BusinessViewActivity extends AppCompatActivity {
    private ItemAdapter itemAdapter;
    private ArrayList<ItemModel> ItemList = new ArrayList<>();
    private ArrayList<ItemModel> displayItemList = new ArrayList<>();

    private TextView bName, bLoc,
            //bDesc,
            bStatus,
            bPhone, bWhatsapp, bEmail;
    private ImageView icVerified, imgBusinessPic;

    private UserModel currentUser = null;
    private UserModel business = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_view);

        Intent i = getIntent();
        currentUser = (new Gson()).fromJson(i.getStringExtra(MainActivity.CURRENT_USER), UserModel.class);
        business = (new Gson()).fromJson(i.getStringExtra(MainActivity.BUSINESS), UserModel.class);

        bName = findViewById(R.id.txtBusinessName);
        bLoc = findViewById(R.id.txtBusinessLocation);
        //bDesc = findViewById(R.id.txtBusinessDescription);
        bStatus = findViewById(R.id.txtBusinessStatus);
        bPhone = findViewById(R.id.txtBusinessPhone);
        bWhatsapp = findViewById(R.id.txtBusenessWhatsapp);
        bEmail = findViewById(R.id.txtBusinessEmail);
        icVerified = findViewById(R.id.icVerified);
        imgBusinessPic = findViewById(R.id.imgBusinessPic);

        ImageView btnBusinessChat = findViewById(R.id.btnBusinessChat);
        btnBusinessChat.setOnClickListener(v -> {
            if (currentUser == null) {
                startActivity(new Intent(this, AddAccountActivity.class));
                finish();
            } else {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(currentUser));
                intent.putExtra(MainActivity.BUSINESS, (new Gson()).toJson(business));
                startActivity(intent);
            }
        });

        displayBusinessInfo();
        itemRecyclerInit();

        FirebaseFirestore ff = FirebaseFirestore.getInstance();

        ff.collection(CollectionName.ITEMS).whereEqualTo("userId", business.getId()).get()
               .addOnSuccessListener(queryDocumentSnapshots -> {
                   for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                       ItemModel tempItem = snapshot.toObject(ItemModel.class);
                       assert tempItem != null;
                       tempItem.setId(snapshot.getId());

                       if (!tempItem.isSold()) {
                           boolean exists = false;

                           for (ItemModel i1 : ItemList) {
                               if (i1.equals(tempItem)) {
                                   exists = true;
                               }
                           }

                           if (!exists) {
                               ItemList.add(tempItem);
                           }
                       }
                   }

                   displayItemList.clear();
                   displayItemList.addAll(ItemList);
                   itemAdapter.notifyDataSetChanged();
               })
               .addOnFailureListener(e -> {
                   e.printStackTrace();
                   Toast.makeText(BusinessViewActivity.this, "Unable to get items sold by this person", Toast.LENGTH_SHORT).show();
               });
    }

    private void displayBusinessInfo() {
        // TODO: 2019/12/25 VALIDATE THE EXISTENCE OF DATA
        bName.setText(business.getName());

        if (business.getLocation() != null && !TextUtils.isEmpty(business.getLocation()))
            bLoc.setText(business.getLocation());
        else
            bLoc.setText("Unknown Campus");

        //bDesc.setText(business.getDetails());
        //bStatus.setText(business.getType().toString().replace("_", " "));
        if (business.getLandLine() != null)
            bPhone.setText(String.format("Phone - %s", business.getLandLine()));
        else
            bPhone.setText("Phone - ");

        if (business.getWhatsappNum() != null)
            bWhatsapp.setText(String.format("Whatsapp - %s", business.getWhatsappNum()));
        else
            bWhatsapp.setText("Whatsapp - ");

        bEmail.setText(String.format("Email - %s", business.getEmail()));

        if (business.isVerified()) {
            bStatus.setText("Verified");
            icVerified.setVisibility(View.VISIBLE);
        } else {
            bStatus.setText("Unverified");
            icVerified.setVisibility(View.GONE);
        }

        Picasso.with(BusinessViewActivity.this)
                .load(business.getDpURI())
                .resize(500, 500)
                .centerCrop()
                .placeholder(R.drawable.ic_person)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imgBusinessPic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(BusinessViewActivity.this)
                                .load(business.getDpURI())
                                .resize(500, 500)
                                .centerCrop()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(imgBusinessPic, new Callback() {
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
    }

    void itemRecyclerInit () {
        RecyclerView itemRecyclerView = findViewById(R.id.itemRecycler);
        itemRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager itemLayoutManager = new LinearLayoutManager(BusinessViewActivity.this);
        itemAdapter = new ItemAdapter(getBaseContext(), displayItemList);

        itemRecyclerView.setLayoutManager(itemLayoutManager);
        itemRecyclerView.setAdapter(itemAdapter);

        itemAdapter.setOnItemClickListener(position -> {
            ItemModel itemModel = displayItemList.get(position);
            String itemJSON = (new Gson()).toJson(itemModel);
            String userJSON = (new Gson()).toJson(currentUser);

            Log.d("Item", "Item - " + itemJSON);

            Intent i = new Intent(BusinessViewActivity.this, ItemViewActivity.class);
            i.putExtra(MainActivity.ITEM, itemJSON);
            i.putExtra(MainActivity.CURRENT_USER, userJSON);
            startActivity(i);

            Toast.makeText(BusinessViewActivity.this, itemModel.getName(), Toast.LENGTH_SHORT).show();
        });
    }
}
