package com.yukisoft.yellowpixels.JavaActivities.Home.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yukisoft.yellowpixels.JavaActivities.Home.HomeActivity;
import com.yukisoft.yellowpixels.JavaActivities.Items.ItemViewActivity;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.AddAccountActivity;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.LoginActivity;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.SettingsActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Adapters.ItemAdapter;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.AccountType;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ItemModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.R;
import com.yukisoft.yellowpixels.JavaActivities.Items.SellItemActivity;

import java.util.ArrayList;
import java.util.Objects;

import static com.yukisoft.yellowpixels.R.*;

public class ProfileFragment extends Fragment {
    private UserModel currentUser;

    private ItemAdapter itemAdapter;

    private ArrayList<ItemModel> ItemList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(layout.fragment_profile, container, false);

        ConstraintLayout profile = v.findViewById(id.userProfile);
        profile.setVisibility(View.GONE);
        FloatingActionButton btnAddSellItem = v.findViewById(R.id.btnAddSellItem);
        btnAddSellItem.hide();

        Intent i = Objects.requireNonNull(getActivity()).getIntent();
        currentUser = (new Gson()).fromJson(i.getStringExtra(MainActivity.CURRENT_USER), UserModel.class);

        if (currentUser == null){
            startActivity(new Intent(getContext(), AddAccountActivity.class));
            getActivity().finish();
        } else {
            profile.setVisibility(View.VISIBLE);
            initProfile(v);

            btnAddSellItem.setOnClickListener(v1 -> {
                // start item sale activity
                startActivity(new Intent(getActivity(), SellItemActivity.class).putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(currentUser)));
            });
            if (currentUser.getType().equals(AccountType.Business)){
                btnAddSellItem.show();
            }
        }




        return v;
    }

    private void initProfile(View v){
        TextView name = v.findViewById(id.lblPName);
        TextView email = v.findViewById(id.lblEmail);
        TextView phone = v.findViewById(id.lblPhone);
        TextView whatsapp = v.findViewById(id.lblWhatsapp);
        ImageView businessIcon = v.findViewById(id.businessIcon);

        Picasso.with(getContext())
                .load(currentUser.getDpURI())
                .resize(500, 500)
                .placeholder(R.drawable.ic_business)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(businessIcon, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(getActivity())
                                .load(currentUser.getDpURI())
                                .error(drawable.ic_business)
                                .placeholder(R.drawable.ic_business)
                                .resize(500, 500)
                                .into(businessIcon, new Callback() {
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

        name.setText(currentUser.getName());
        email.setText(currentUser.getEmail());
        phone.setText(string.phone_number);
        whatsapp.setText(string.whatsapp_number);
        try {
            phone.append(currentUser.getLandLine());
            whatsapp.append(currentUser.getWhatsappNum());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initRecycler(v);
        getItems();

        ImageView settings = v.findViewById(id.btnSettings);
        settings.setOnClickListener(this::showProfileMenu);
    }

    public void showProfileMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == id.itemAccountSettings){
                    startActivity(new Intent(getContext(), SettingsActivity.class)
                            .putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(currentUser)));
                } else if (item.getItemId() == id.itemLogout){
                    new AlertDialog.Builder(getContext(), R.style.MyDialogTheme)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Logout?")
                            .setMessage("Are you sure you want to logout?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Toast.makeText(getContext(), "Goodbye " + currentUser.getName(), Toast.LENGTH_SHORT).show();
                                FirebaseAuth fbAuth = FirebaseAuth.getInstance();
                                fbAuth.signOut();
                                startActivity(new Intent(getContext(), HomeActivity.class));
                                Objects.requireNonNull(getActivity()).finish();
                            })
                            .setNegativeButton("No", null)
                            .show();
                }

                return false;
            }
        });
        popup.inflate(menu.profile_menu);
        popup.show();
    }

    /**
     *
     * EXTRA
     *
     */
    private void initRecycler(View v){
            RecyclerView itemRecyclerView = v.findViewById(R.id.itemRecycler);
            itemRecyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager itemLayoutManager = new LinearLayoutManager(getContext());
            itemAdapter = new ItemAdapter(getContext(), ItemList);

            itemRecyclerView.setLayoutManager(itemLayoutManager);
            itemRecyclerView.setAdapter(itemAdapter);

            itemAdapter.setOnItemClickListener(position -> {
                ItemModel itemModel = ItemList.get(position);
                String itemJSON = (new Gson()).toJson(itemModel);
                String userJSON = (new Gson()).toJson(currentUser);

                Log.d("Item", "Item - " + itemJSON);

                Intent i = new Intent(getContext(), ItemViewActivity.class);
                i.putExtra(MainActivity.ITEM, itemJSON);
                i.putExtra(MainActivity.CURRENT_USER, userJSON);
                startActivity(i);

                Toast.makeText(getContext(), itemModel.getName(), Toast.LENGTH_SHORT).show();
            });

    }

    private void getItems(){
        FirebaseFirestore ff = FirebaseFirestore.getInstance();

        ff.collection(CollectionName.ITEMS).whereEqualTo("userId", currentUser.getId()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        ItemModel tempItem = snapshot.toObject(ItemModel.class);

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

                    itemAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Unable to get items sold by business", Toast.LENGTH_SHORT).show();
                });
    }
}
