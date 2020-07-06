package com.yukisoft.yellowpixels.JavaActivities.Home.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.BusinessViewActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.AccountType;
import com.yukisoft.yellowpixels.JavaRepositories.Adapters.BusinessAdapter;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.CategoryModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;
import java.util.Objects;

public class BusinessesFragment extends Fragment {
    private ArrayList<UserModel> BusinessList = new ArrayList<>();
    private ArrayList<UserModel> displayBusinessList = new ArrayList<>();

    private ProgressBar loading;
    private TextView loadingStatus, businessCatLabel;

    private static final CategoryModel ALL = new CategoryModel("ALL", "All", "Displaying ALL businesses");
    private CategoryModel currentCategory;

    private boolean isLoading = true;

    private BusinessAdapter businessAdapter;

    private CollectionReference businessRef = FirebaseFirestore.getInstance().collection(CollectionName.USERS);
    private UserModel currentUser = null;
    private UserModel business = null;

    private Spinner spBusinessCat;
    private TextView txtCat;
    private View dropLine;

    private SearchView txtSearch;

    private ImageView btnBusinessDropDown;

    private boolean down = false;

    public BusinessesFragment() {
        ALL.setId("all");
        currentCategory = ALL;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_businesses, container, false);

        Intent i = Objects.requireNonNull(getActivity()).getIntent();
        String userJSON = i.getStringExtra(MainActivity.CURRENT_USER);
        currentUser = (new Gson()).fromJson(userJSON, UserModel.class);

        loading= view.findViewById(R.id.loading);
        loadingStatus = view.findViewById(R.id.loadingStatus);
        businessCatLabel = view.findViewById(R.id.businessCatLabel);
        btnBusinessDropDown = view.findViewById(R.id.btnBusinessDropDown);
        spBusinessCat = view.findViewById(R.id.spBusinessCat);
        txtCat = view.findViewById(R.id.txtCat);
        dropLine = view.findViewById(R.id.dropLine);

        buildRecyclerView(view);

        btnBusinessDropDown.setOnClickListener(v -> toggleDropDown());
        spBusinessCat.setVisibility(View.GONE);
        txtCat.setVisibility(View.GONE);
        dropLine.setVisibility(View.GONE);

        businessRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null){
                for(DocumentSnapshot msg : queryDocumentSnapshots){
                    UserModel tempUser = msg.toObject(UserModel.class);
                    assert tempUser != null;
                    tempUser.setId(msg.getId());

                    boolean exists = false;

                    for (UserModel m : BusinessList)
                        if(m.getId().equals(tempUser.getId()))
                            exists = true;

                    if(!exists)
                        if (tempUser.getType().equals(AccountType.Business)
                                || tempUser.getType().equals(AccountType.Verified_Business))
                            displayBusinessList.add(tempUser);
                        BusinessList.add(tempUser);
                }
            }
            //Collections.sort(displayBusinessList, new AlphabetComparator());
            //Collections.sort(MTList, new AlphabetComparator());

            isLoading = false;
            checkList();

            businessAdapter.notifyDataSetChanged();
        });

        txtSearch = view.findViewById(R.id.searchBox);
        txtSearch.setOnCloseListener(() -> {
            /* collectionView.setVisibility(View.VISIBLE); */
            txtSearch.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            businessCatLabel.setText("All Businesses");
            return false;
        });
        txtSearch.setOnSearchClickListener(v -> {
            txtSearch.setBackgroundColor(getResources().getColor(R.color.colorBG));
            if (currentCategory != ALL) {
                businessCatLabel.setText(String.format("Businesses under - %s", currentCategory.getName()));
            } else {
                businessCatLabel.setText("All Businesses");
            }
        });
        txtSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                //Toast.makeText(getContext(), newText, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        return view;
    }

    private void checkList(){
        loadingStatus.setVisibility(View.GONE);
        if (!isLoading) {
            if (displayBusinessList.isEmpty()) {
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
            spBusinessCat.setVisibility(View.GONE);
            txtCat.setVisibility(View.GONE);
            dropLine.setVisibility(View.GONE);
            btnBusinessDropDown.setImageResource(R.drawable.ic_arrow_down);
            down = false;
        } else {
            spBusinessCat.setVisibility(View.VISIBLE);
            txtCat.setVisibility(View.VISIBLE);
            dropLine.setVisibility(View.VISIBLE);
            btnBusinessDropDown.setImageResource(R.drawable.ic_arrow_up);
            down = true;
        }
    }

    @SuppressLint("SetTextI18n")
    private void search(String input) {

        if (TextUtils.isEmpty(input))
            businessCatLabel.setText("All Businesses");
        else {
            businessCatLabel.setText(String.format("'%s'", input));
            businessCatLabel.append(String.format(" in '%s'", currentCategory.getName()));
        }

        displayBusinessList.clear();

        for (UserModel u : BusinessList) {
            if (!u.getType().equals(AccountType.Customer))
                if (u.getName().toLowerCase().contains(input.toLowerCase())
                        || u.getDetails().toLowerCase().contains(input.toLowerCase()))
                    if (currentCategory != ALL) {
                        if (u.getCategory().equals(currentCategory.getId()))
                            displayBusinessList.add(u);
                    } else {
                        displayBusinessList.add(u);
                    }
        }

        businessAdapter.notifyDataSetChanged();

        checkList();
    }

    private void buildRecyclerView(View v){
        RecyclerView businessRecyclerView = v.findViewById(R.id.businessRecycler);
        businessRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager businessLayoutManager = new LinearLayoutManager(getContext());
        businessAdapter = new BusinessAdapter(getContext(), displayBusinessList);

        businessRecyclerView.setLayoutManager(businessLayoutManager);
        businessRecyclerView.setAdapter(businessAdapter);

        businessAdapter.setOnItemClickListener(position -> {
            business = displayBusinessList.get(position);

            Intent i = new Intent(getActivity(), BusinessViewActivity.class);
            i.putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(currentUser));
            i.putExtra(MainActivity.BUSINESS, (new Gson()).toJson(business));
            startActivity(i);
        });
    }

}
