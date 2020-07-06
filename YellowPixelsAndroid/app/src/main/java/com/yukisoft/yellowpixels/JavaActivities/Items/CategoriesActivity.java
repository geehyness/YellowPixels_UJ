package com.yukisoft.yellowpixels.JavaActivities.Items;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.yukisoft.yellowpixels.JavaActivities.Home.HomeActivity;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.ChatActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Adapters.CategoryAdapter;
import com.yukisoft.yellowpixels.JavaRepositories.Adapters.ItemAdapter;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.CategoryModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ItemModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;
import java.util.Objects;

public class CategoriesActivity extends AppCompatActivity {
    UserModel currentUser;

    private ItemAdapter itemAdapter;
    private CategoryAdapter catAdapter;

    private ArrayList<ItemModel> ItemList = new ArrayList<>();
    private ArrayList<ItemModel> displayItemList = new ArrayList<>();
    private ArrayList<CategoryModel> catList = new ArrayList<>();

    private ConstraintLayout catView, itemView;

    private CategoryModel currentCategory;
    private TextView catName;
    private TextView catDetails;
    private TextView txtItemCat, loadingStatus;
    private ProgressBar loading;

    private boolean inCatView = false;
    private boolean isLoading = true;

    private static final CategoryModel ALL = new CategoryModel("ALL", "All", "Displaying ALL items from ALL categories:");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        Intent i = getIntent();
        String userJSON = i.getStringExtra(MainActivity.CURRENT_USER);
        currentUser = (new Gson()).fromJson(userJSON, UserModel.class);

        catView = findViewById(R.id.catView);
        itemView = findViewById(R.id.itemView);
        catView.setVisibility(View.VISIBLE);
        itemView.setVisibility(View.GONE);

        loading= findViewById(R.id.loading);
        loadingStatus = findViewById(R.id.loadingStatus);

        buildRecyclerView();
    }

    private void buildRecyclerView(){
        catName = findViewById(R.id.catName);
        catDetails = findViewById(R.id.catDetails);
        catName.setText("All");
        catDetails.setText("Displaying ALL items from ALL categories:");

        RecyclerView itemRecyclerView = findViewById(R.id.itemRecycler);
        itemRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager itemLayoutManager = new LinearLayoutManager(CategoriesActivity.this);
        itemAdapter = new ItemAdapter(CategoriesActivity.this, displayItemList);

        itemRecyclerView.setLayoutManager(itemLayoutManager);
        itemRecyclerView.setAdapter(itemAdapter);

        itemAdapter.setOnItemClickListener(position -> {
            ItemModel itemModel = displayItemList.get(position);
            String itemJSON = (new Gson()).toJson(itemModel);
            String userJSON = (new Gson()).toJson(currentUser);

            Log.d("Item", "Item - " + itemJSON);

            Intent i = new Intent(CategoriesActivity.this, ItemViewActivity.class);
            i.putExtra(MainActivity.ITEM, itemJSON);
            i.putExtra(MainActivity.CURRENT_USER, userJSON);
            startActivity(i);
        });

        RecyclerView catRecyclerView = findViewById(R.id.catRecycler);
        catRecyclerView.setHasFixedSize(false);
        catAdapter = new CategoryAdapter(catList);
        GridLayoutManager catLayoutManager = new GridLayoutManager(CategoriesActivity.this, 3);
        catRecyclerView.setLayoutManager(catLayoutManager);
        catRecyclerView.setAdapter(catAdapter);
        catAdapter.setOnItemClickListener(position -> {
            CategoryModel collection = catList.get(position);

            currentCategory = collection;
            catName.setText(collection.getName());
            if (!TextUtils.isEmpty(currentCategory.getDetails()))
                catDetails.setText(collection.getDetails());
            else
                catDetails.setText("Details Unavailable!");

            catView.setVisibility(View.GONE);
            itemView.setVisibility(View.VISIBLE);

            inCatView = true;
            displayItemList.clear();

            FirebaseFirestore ff = FirebaseFirestore.getInstance();
            if (currentCategory.equals(ALL)) {
                ff.collection(CollectionName.ITEMS)
                        .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null){
                        for(DocumentSnapshot msg : queryDocumentSnapshots){
                            ItemModel tempUser = msg.toObject(ItemModel.class);
                            assert tempUser != null;
                            tempUser.setId(msg.getId());

                            boolean exists = false;

                            for (ItemModel m : ItemList)
                                if(m.getId().equals(tempUser.getId()))
                                    exists = true;

                            if(!exists)
                                ItemList.add(tempUser);
                        }
                        isLoading = false;
                    }
                    //Collections.sort(displayItemList, new AlphabetComparator());
                    //Collections.sort(MTList, new AlphabetComparator());

                    displayItemList.addAll(ItemList);
                    checkList();

                    itemAdapter.notifyDataSetChanged();
                });
            } else {
                ff.collection(CollectionName.ITEMS).whereEqualTo("category", currentCategory.getId())
                        .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null){
                        for(DocumentSnapshot msg : queryDocumentSnapshots){
                            ItemModel tempUser = msg.toObject(ItemModel.class);
                            assert tempUser != null;
                            tempUser.setId(msg.getId());

                            boolean exists = false;

                            for (ItemModel m : ItemList)
                                if(m.getId().equals(tempUser.getId()))
                                    exists = true;

                            if(!exists)
                                ItemList.add(tempUser);
                        }
                        isLoading = false;
                    }
                    //Collections.sort(displayItemList, new AlphabetComparator());
                    //Collections.sort(MTList, new AlphabetComparator());

                    displayItemList.addAll(ItemList);
                    checkList();

                    itemAdapter.notifyDataSetChanged();
                });
            }
        });

        getCategories();
    }

    private void checkList(){
        loadingStatus.setVisibility(View.GONE);
        if (!isLoading) {
            if (displayItemList.isEmpty()) {
                loading.setVisibility(View.GONE);
                loadingStatus.setText("Nothing to display!");
                loadingStatus.setVisibility(View.VISIBLE);
            } else {
                loading.setVisibility(View.GONE);
            }
        }
    }

    private void getCategories() {
        catList.add(ALL);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection(CollectionName.ITEM_CATEGORIES).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        CategoryModel cat = documentSnapshot.toObject(CategoryModel.class);

                        catList.add(cat);
                    }
                    catAdapter.notifyDataSetChanged();
                    //setCatSpinner(catList);
                });
    }

    @Override
    public void onBackPressed() {
        if (!inCatView) {
            startActivity(new Intent(CategoriesActivity.this, HomeActivity.class)
                    .putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(currentUser)));
            finish();
        } else {
            catView.setVisibility(View.VISIBLE);
            itemView.setVisibility(View.GONE);
            inCatView = false;
        }
    }
}
