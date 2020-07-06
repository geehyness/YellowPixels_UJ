package com.yukisoft.yellowpixels.JavaActivities.Items;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.yukisoft.yellowpixels.JavaActivities.Home.HomeActivity;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Adapters.ImageAdapter;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.Models.CategoryModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ItemModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class SellItemActivity extends AppCompatActivity {
    private static final int REQ_CODE_PICK_IMAGE_FILE = 22;
    private UserModel currentUser;
    private EditText txtDetails;
    private Spinner catSelect;
    private boolean selectingFile = false;

    private ImageView addPic;

    ArrayList<CategoryModel> catList = new ArrayList<>();

    ArrayList<Uri> imageArray = new ArrayList<>();
    ArrayList<String> imageStringArray = new ArrayList<>();
    ArrayList<String> onlineImageArray = new ArrayList<>();
    private ImageAdapter imageAdapter;

    StorageReference storageReference;
    private StorageTask uploadTask;
    private ProgressBar progressBarUpload;
    private ImageView btnCancelUpload;
    private TextView txtProgress;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_item);

        currentUser = (new Gson()).fromJson(getIntent().getStringExtra(MainActivity.CURRENT_USER), UserModel.class);
        // TODO: 2020/01/03 VALIDATE USER
        /*if (currentUser == null || !currentUser.getType().equals(AccountType.Verified_Business)) {
            Toast.makeText(this, "Check your user details to continue.\nYou must be verified to sell!", Toast.LENGTH_SHORT).show();
            finish();
        }*/

        storageReference = FirebaseStorage.getInstance().getReference("items/" + currentUser.getId());

        imageRecyclerInit();

        progressBarUpload = findViewById(R.id.progressBar);
        btnCancelUpload = findViewById(R.id.btnCancel);
        txtProgress = findViewById(R.id.txtProgress);
        progressBarUpload.setVisibility(View.GONE);
        btnCancelUpload.setVisibility(View.GONE);
        txtProgress.setVisibility(View.GONE);

        txtDetails = findViewById(R.id.txtSellDetails);
        txtDetails.setOnTouchListener((v, event) -> {
            if (txtDetails.hasFocus()) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                }
            }
            return false;
        });

        addPic = findViewById(R.id.btnAddPic);
        addPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectingFile) {
                    selectingFile = true;
                    Intent intent;
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Pick display logo"), REQ_CODE_PICK_IMAGE_FILE);
                }
            }
        });

        catSelect = findViewById(R.id.spHomeCat);
        createSpinner();

        Button sell = findViewById(R.id.btnSell);
        // TODO: 2020/01/03 SELL ITEM
        sell.setOnClickListener(v -> sellItem());
    }

    private void sellItem(){
        EditText txtName = findViewById(R.id.txtSellName);
        EditText txtPrice = findViewById(R.id.txtSellPrice);

        String name = txtName.getText().toString();
        String price = txtPrice.getText().toString();
        String details = txtDetails.getText().toString();

        CategoryModel category;

        if (TextUtils.isEmpty(name)){
            Toast.makeText(SellItemActivity.this, "Item Name cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(price)){
            Toast.makeText(SellItemActivity.this, "Item Price cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(details)){
            Toast.makeText(SellItemActivity.this, "Provide details for the item!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (catSelect.getSelectedItemPosition() <= 0){
            Toast.makeText(SellItemActivity.this, "Please specify the item category", Toast.LENGTH_SHORT).show();
            return;
        } else {
            category = catList.get(catSelect.getSelectedItemPosition() - 1);
        }

        if (imageArray.isEmpty()) {
            Toast.makeText(this, "Item should have atleast 1 image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarUpload.setVisibility(View.VISIBLE);
        btnCancelUpload.setVisibility(View.VISIBLE);
        txtProgress.setText("0%");
        txtProgress.setVisibility(View.VISIBLE);


        //Toast.makeText(this, category.getName(), Toast.LENGTH_SHORT).show();

        uploadFile(0, name, Double.parseDouble(price), details, category);
    }

    private String fileExtension(Uri uri){
        if (uri != null){
            ContentResolver cr = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(cr.getType(uri));
        } else {
            return null;
        }
    }

    private void uploadFile(int position, String name, double price, String details, CategoryModel category) {
        Log.d("images", position + "/" + imageArray.size());
        if (position == imageArray.size()) {
            progressBarUpload.setVisibility(View.GONE);
            btnCancelUpload.setVisibility(View.GONE);
            txtProgress.setVisibility(View.GONE);

            if (imageArray.size() != onlineImageArray.size()){
                Toast.makeText(this, "THIS SUCKS", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: 2020/01/16 PASS CATEGORY
            ItemModel currItem = new ItemModel(name, price, details,
                    currentUser.getId(), category.getId(), onlineImageArray);

            FirebaseFirestore ff = FirebaseFirestore.getInstance();
            ff.collection(CollectionName.ITEMS).add(currItem).addOnSuccessListener(documentReference -> {
                String userJSON = (new Gson()).toJson(currentUser);
                Intent i = new Intent(SellItemActivity.this, HomeActivity.class);
                i.putExtra(MainActivity.CURRENT_USER, userJSON);
                startActivity(i);
                Objects.requireNonNull(SellItemActivity.this).finish();
            });

            return;
        } else {
            final StorageReference fileRef = storageReference.child((new Date()).toString() + "." + fileExtension(imageArray.get(position)));
            uploadTask = fileRef.putFile(imageArray.get(position))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    onlineImageArray.add(uri.toString());
                                    Log.d("image", uri.toString());
                                    uploadFile(position + 1, name, price, details, category);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(SellItemActivity.this, "Unable to upload audio file!\nPlease retry.", Toast.LENGTH_SHORT).show())
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            progressBarUpload.setProgress((int) progress, true);
                        } else {
                            progressBarUpload.setProgress((int) progress);
                        }
                        txtProgress.setText(progress*100 + "%");
                    });
        }
    }

    private void createSpinner(){
        FirebaseFirestore ff = FirebaseFirestore.getInstance();

        ff.collection(CollectionName.ITEM_CATEGORIES)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            ArrayList<String> strList = new ArrayList<>();
            strList.add("Pick a category");
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                CategoryModel cat = documentSnapshot.toObject(CategoryModel.class);
                assert cat != null;
                cat.setId(documentSnapshot.getId());
                catList.add(cat);
                strList.add(Objects.requireNonNull(cat).getName());
            }

            ArrayAdapter<String> catAdapter = new ArrayAdapter<>(Objects.requireNonNull(SellItemActivity.this), android.R.layout.simple_spinner_item, strList);
            catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            catSelect.setAdapter(catAdapter);
        });
    }

    private void imageRecyclerInit() {
        RecyclerView catRecyclerView = findViewById(R.id.imageRecycler);
        catRecyclerView.setHasFixedSize(false);
        imageAdapter = new ImageAdapter(getBaseContext(), imageStringArray);
        RecyclerView.LayoutManager catLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        catRecyclerView.setLayoutManager(catLayoutManager);
        catRecyclerView.setAdapter(imageAdapter);
        imageAdapter.setOnItemClickListener(position -> {
             imageArray.remove(position);
             imageStringArray.remove(position);
             imageAdapter.notifyItemRemoved(position);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PICK_IMAGE_FILE && resultCode == RESULT_OK) {
            if ((data != null) && (data.getData() != null)) {
                imageArray.add(data.getData());
                imageStringArray.add(data.getDataString());
                imageAdapter.notifyDataSetChanged();
            }
        }

        selectingFile = false;
    }
}