package com.yukisoft.yellowpixels.JavaActivities.Home.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.yukisoft.yellowpixels.JavaActivities.Items.CategoriesActivity;
import com.yukisoft.yellowpixels.JavaActivities.Items.ItemViewActivity;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.ChatActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Adapters.CategoryAdapter;
import com.yukisoft.yellowpixels.JavaRepositories.Adapters.ItemAdapter;
import com.yukisoft.yellowpixels.JavaRepositories.Models.CategoryModel;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ItemModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private ArrayList<ItemModel> ItemList = new ArrayList<>();
    private ArrayList<ItemModel> displayItemList = new ArrayList<>();
    private ArrayList<CategoryModel> catList = new ArrayList<>();

    private static final CategoryModel ALL = new CategoryModel("ALL", "All", "Displaying ALL items from ALL categories:");


    private ItemAdapter itemAdapter;
    private CategoryAdapter catAdapter;

    private CollectionReference itemRef = FirebaseFirestore.getInstance().collection(CollectionName.ITEMS);
    static final String ITEM_MODEL = "ItemModel";
    private static UserModel currentUser;

    private SearchView txtSearch;
    private ProgressBar loading;

    private Spinner spItemCat;
    private TextView txtItemCat, loadingStatus;
    private View dropLineHome;
    private ImageView btnItemDropDown;
    private boolean down = false;

    private CategoryModel currentCategory;
    private TextView catName;
    private TextView catDetails;

    private ConstraintLayout catView, itemView;

    private boolean inCatView = false;
    private boolean isLoading = true;

    public HomeFragment() {
        ALL.setId("ALL");
        currentCategory = ALL;
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Intent i = Objects.requireNonNull(getActivity()).getIntent();
        String userJSON = i.getStringExtra(MainActivity.CURRENT_USER);
        currentUser = (new Gson()).fromJson(userJSON, UserModel.class);

        catView = view.findViewById(R.id.catView);
        itemView = view.findViewById(R.id.itemView);
        // TODO: 2020/01/30 SHOW CAT VIEW
        catView.setVisibility(View.GONE);

        spItemCat = view.findViewById(R.id.spItemCat);
        txtItemCat = view.findViewById(R.id.txtItemCat);
        dropLineHome = view.findViewById(R.id.dropLineHome);
        spItemCat.setVisibility(View.GONE);
        txtItemCat.setVisibility(View.GONE);
        dropLineHome.setVisibility(View.GONE);

        loading= view.findViewById(R.id.loading);
        loadingStatus = view.findViewById(R.id.loadingStatus);
        btnItemDropDown = view.findViewById(R.id.btnItemDropDown);
        btnItemDropDown.setOnClickListener(v -> toggleDropDown());

        buildRecyclerView(view);

        itemRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
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

        /*CardView btnCategories = view.findViewById(R.id.btnCategories);
        btnCategories.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CategoriesActivity.class)
                    .putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(currentUser)));
            HomeFragment.this.getActivity().finish();
        });*/

        txtSearch = view.findViewById(R.id.searchBox);
        txtSearch.setOnCloseListener(() -> {
            /* collectionView.setVisibility(View.VISIBLE); */
            //txtSearch.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

            /*if (!inCatView) {
                catView.setVisibility(View.VISIBLE);
                itemView.setVisibility(View.GONE);
            } else {
                if (currentCategory != ALL) {
                    catName.setText(currentCategory.getName());
                    catDetails.setText(currentCategory.getDetails());
                } else {
                    catName.setText("All");
                    catDetails.setText("Displaying ALL items from ALL categories:");
                }
            }*/

            if (currentCategory != ALL) {
                catName.setText(currentCategory.getName());
                catDetails.setText(currentCategory.getDetails());
            } else {
                catName.setText("All");
                catDetails.setText("Displaying ALL items from ALL categories:");
            }

            return false;
        });
        txtSearch.setOnSearchClickListener(v -> {
            txtSearch.setBackgroundColor(getResources().getColor(R.color.colorBG));

            catName.setText("''");
            if (currentCategory == null)
                catDetails.setText("in 'All'");
            else
                catDetails.setText(String.format("in '%s'", currentCategory.getName()));

            itemView.setVisibility(View.VISIBLE);
            catView.setVisibility(View.GONE);
        });
        txtSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });

        spItemCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = catList.get(position);
                search(txtSearch.getQuery().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void search(String input) {

        if (TextUtils.isEmpty(input)) {
            catName.setText(currentCategory.getName());
            if (currentCategory.equals(ALL))
                catDetails.setText("Displaying ALL items from ALL categories:");
            else
                catDetails.setText("Displaying ALL items from '" + currentCategory.getName() + "' category:");
        } else {
            catName.setText(String.format("'%s'", input));
            catDetails.setText("Displaying '" + input + "' items from '" + currentCategory.getName() + "' category:");
        }

        displayItemList.clear();

        for (ItemModel i : ItemList) {
            if (i.getName().toLowerCase().contains(input.toLowerCase())
                || i.getDetails().toLowerCase().contains(input.toLowerCase()))
                if (currentCategory != ALL) {
                    if (i.getCategory() != null)
                        if (i.getCategory().equals(currentCategory.getId()))
                            displayItemList.add(i);
                } else {
                    displayItemList.add(i);
                }
        }

        itemAdapter.notifyDataSetChanged();

        checkList();
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

    private void toggleDropDown() {
        if (down) {
            spItemCat.setVisibility(View.GONE);
            txtItemCat.setVisibility(View.GONE);
            dropLineHome.setVisibility(View.GONE);
            btnItemDropDown.setImageResource(R.drawable.ic_arrow_down);
            down = false;
        } else {
            spItemCat.setVisibility(View.VISIBLE);
            txtItemCat.setVisibility(View.VISIBLE);
            dropLineHome.setVisibility(View.VISIBLE);
            btnItemDropDown.setImageResource(R.drawable.ic_arrow_up);
            down = true;
        }
    }

    private void buildRecyclerView(View v){
        catName = v.findViewById(R.id.catName);
        catDetails = v.findViewById(R.id.catDetails);
        catName.setText("All");
        catDetails.setText("Displaying ALL items from ALL categories:");

        RecyclerView itemRecyclerView = v.findViewById(R.id.itemRecycler);
        itemRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager itemLayoutManager = new LinearLayoutManager(getContext());
        itemAdapter = new ItemAdapter(getContext(), displayItemList);

        itemRecyclerView.setLayoutManager(itemLayoutManager);
        itemRecyclerView.setAdapter(itemAdapter);

        itemAdapter.setOnItemClickListener(position -> {
            ItemModel itemModel = displayItemList.get(position);
            String itemJSON = (new Gson()).toJson(itemModel);
            String userJSON = (new Gson()).toJson(currentUser);

            Log.d("Item", "Item - " + itemJSON);

            Intent i = new Intent(getContext(), ItemViewActivity.class);
            i.putExtra(MainActivity.ITEM, itemJSON);
            i.putExtra(MainActivity.CURRENT_USER, userJSON);
            startActivity(i);
        });

        RecyclerView catRecyclerView = v.findViewById(R.id.catRecycler);
        catRecyclerView.setHasFixedSize(false);
        catAdapter = new CategoryAdapter(catList);
        GridLayoutManager catLayoutManager = new GridLayoutManager(getActivity(), 3);
        catRecyclerView.setLayoutManager(catLayoutManager);
        catRecyclerView.setAdapter(catAdapter);
        catAdapter.setOnItemClickListener(position -> {
            CategoryModel collection = catList.get(position);

            currentCategory = collection;
            catName.setText(collection.getName());
            catDetails.setText(collection.getDetails());

            catView.setVisibility(View.GONE);
            itemView.setVisibility(View.VISIBLE);

            displayItemList.clear();

            for (ItemModel a : ItemList)
                if (a.getCategory().equals(currentCategory.getId()))
                    displayItemList.add(a);

            itemAdapter.notifyDataSetChanged();

            /*messageView.setVisibility(View.VISIBLE);
            extendCollection.setVisibility(View.GONE);
            btnDeleteCollection.setVisibility(View.VISIBLE);*/
        });

        getCategories();
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
                setCatSpinner(catList);
        });
    }

    private void setCatSpinner(ArrayList<CategoryModel> list) {
        ArrayList<String> categories = new ArrayList<>();

        for (CategoryModel m : list) {
            categories.add(String.valueOf(m.getName()));
        }

        if (!categories.isEmpty()){
            if (getActivity() != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_spinner_item, categories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spItemCat.setAdapter(adapter);
            }
        }
    }

}