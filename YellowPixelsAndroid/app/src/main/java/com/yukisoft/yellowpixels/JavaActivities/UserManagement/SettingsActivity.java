package com.yukisoft.yellowpixels.JavaActivities.UserManagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yukisoft.yellowpixels.JavaActivities.Home.HomeActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.AccountType;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.UIElements.MyProgressDialog;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView btnEditUname, btnCancelUpload, imgProfilePic;
    TextInputLayout inputEmail, inputWhatsapp, inputLandline, inputLocation, inputDetails;
    TextView lblProfileUname;
    //Spinner spAccountType;
    ProgressBar progressBarUpload;
    Button save;

    UserModel currentUser = null;

    private StorageReference storageReference;
    private StorageTask uploadTask;

    private final int REQ_CODE_PICK_IMAGE_FILE = 1;
    private Uri dpURI;
    private boolean selectingFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent i = getIntent();
        currentUser = (new Gson()).fromJson(i.getStringExtra(MainActivity.CURRENT_USER), UserModel.class);

        initViews();
        storageReference = FirebaseStorage.getInstance().getReference("users");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnProfileSave:
                saveUser();
                break;

            case R.id.btnProfileEditUname:
                editName();
                break;

            case R.id.imgProfilePic:
                if (uploadTask != null && uploadTask.isInProgress()){
                    Toast.makeText(SettingsActivity.this, "File upload already in progress!\nPress cancel button to stop upload!", Toast.LENGTH_SHORT).show();
                } else {
                    if (!selectingFile) {
                        selectingFile = true;
                        Intent intent;
                        intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Pick display logo"), REQ_CODE_PICK_IMAGE_FILE);
                    }
                }
                break;

            case R.id.btnCancelUpload:
                if (uploadTask != null && uploadTask.isInProgress()){
                    uploadTask.cancel();
                    uploadTask = null;
                    progressBarUpload.setVisibility(View.GONE);
                    btnCancelUpload.setVisibility(View.GONE);
                    imgProfilePic.setImageURI(null);
                    //imgProfilePic.setImageURI(currentUser.getDpURI());
                }
                break;
        }
    }

    private void saveUser() {
        if (TextUtils.isEmpty(inputEmail.getEditText().getText())) {
            inputEmail.getEditText().setError("Email cannot be empty!");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getEditText().getText()).matches()) {
            inputEmail.getEditText().setError("Email is invalid!");
            return;
        }

        currentUser.setName(lblProfileUname.getText().toString());
        currentUser.setEmail(inputEmail.getEditText().getText().toString());
        currentUser.setWhatsappNum(inputWhatsapp.getEditText().getText().toString());
        currentUser.setLandLine(inputLandline.getEditText().getText().toString());
        //currentUser.setType(AccountType.valueOf(spAccountType.getSelectedItem().toString().replace(" ", "_")));
        currentUser.setLocation(inputLocation.getEditText().getText().toString());
        currentUser.setDetails(inputDetails.getEditText().getText().toString());

        final MyProgressDialog progressDialog = new MyProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseFirestore ff = FirebaseFirestore.getInstance();

        ff.collection(CollectionName.USERS).document(currentUser.getId()).set(currentUser).addOnSuccessListener(aVoid -> {
            progressDialog.dismiss();
            Toast.makeText(SettingsActivity.this, "Updated Successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, HomeActivity.class)
                    .putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(currentUser)));
            finish();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(SettingsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PICK_IMAGE_FILE && resultCode == RESULT_OK) {
            if ((data != null) && (data.getData() != null)) {
                dpURI = data.getData();

                imgProfilePic.setImageURI(null);
                imgProfilePic.setImageURI(dpURI);

                progressBarUpload.setVisibility(View.VISIBLE);
                btnCancelUpload.setVisibility(View.VISIBLE);

                if (dpURI != null){
                    final StorageReference fileRef = storageReference.child(currentUser.getId() + "." + fileExtension(dpURI));
                    uploadTask = fileRef.putFile(dpURI)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    uploadTask = null;
                                    progressBarUpload.setVisibility(View.GONE);
                                    btnCancelUpload.setVisibility(View.GONE);

                                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            currentUser.setDpURI(uri.toString());
                                            Log.d("image", uri.toString());
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Unable to upload audio file!\nPlease retry.", Toast.LENGTH_SHORT).show())
                            .addOnProgressListener(taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    progressBarUpload.setProgress((int) progress, true);
                                } else {
                                    progressBarUpload.setProgress((int) progress);
                                }
                            });
                } else {
                    Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
                }
            }
        }

        selectingFile = false;
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

    private void initViews() {
        btnEditUname = findViewById(R.id.btnProfileEditUname);
        btnEditUname.setOnClickListener(this);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        imgProfilePic.setOnClickListener(this);
        imgProfilePic.setImageURI(null);

        Picasso.with(SettingsActivity.this)
                .load(currentUser.getDpURI())
                .resize(500, 500)
                .placeholder(R.drawable.ic_business)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imgProfilePic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(SettingsActivity.this)
                                .load(currentUser.getDpURI())
                                .resize(500, 500)
                                .placeholder(R.drawable.ic_business)
                                .error(R.drawable.ic_business)
                                .into(imgProfilePic, new Callback() {
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
        inputEmail = findViewById(R.id.txtProfileEmail);
        inputWhatsapp = findViewById(R.id.txtProfileWhatsapp);
        inputLandline = findViewById(R.id.txtProfileLandline);
        inputLocation = findViewById(R.id.txtProfileLocation);
        inputDetails = findViewById(R.id.txtProfileDetails);

        progressBarUpload = findViewById(R.id.progressBarUpload);
        progressBarUpload.setVisibility(View.GONE);
        btnCancelUpload = findViewById(R.id.btnCancelUpload);
        btnCancelUpload.setOnClickListener(this);
        btnCancelUpload.setVisibility(View.GONE);

        try {
            inputEmail.getEditText().setText(currentUser.getEmail());
            inputWhatsapp.getEditText().setText(currentUser.getWhatsappNum());
            inputLandline.getEditText().setText(currentUser.getLandLine());
            inputLocation.getEditText().setText(currentUser.getLocation());
            inputDetails.getEditText().setText(currentUser.getDetails());
        } catch (Exception e) {
            Log.d("Input Fields", Objects.requireNonNull(e.getMessage()));
            Toast.makeText(this, "Unable to load user information!", Toast.LENGTH_SHORT).show();
        }

        lblProfileUname = findViewById(R.id.lblProfileUname);
        lblProfileUname.setText(currentUser.getName());

        //spAccountType = findViewById(R.id.spProfileAccountType);
        ArrayList<String> types = new ArrayList();
        types.add(AccountType.Customer.toString());
        types.add(AccountType.Business.toString());

        /*if (currentUser.getType().equals(AccountType.Verified_Business)){
            types.add(AccountType.Verified_Business.toString().replace("_", " "));
            spAccountType.setEnabled(false);
        }*/

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /*spAccountType.setAdapter(adapter);
        spAccountType.setSelection(types.indexOf(currentUser.getType().toString().replace("_", " ")));

        spAccountType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (types.get(position).equals(AccountType.Business.toString())) {
                    if (currentUser.getType().equals(AccountType.Customer))
                        new AlertDialog.Builder(SettingsActivity.this, R.style.MyDialogTheme)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Switch to Customer?")
                                .setMessage("Switching from Business account to Customer account " +
                                        "will make your account public. Users of this app will now " +
                                        "be able to find you.\n\nDo you wish to continue?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    Toast.makeText(SettingsActivity.this, "Business Account Selected.\n\nPress 'Save' button to apply changes.", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("No", (dialog, which) -> spAccountType.setSelection(types.indexOf(currentUser.getType().toString().replace("_", " "))))
                                .show();
                } else if (types.get(position).equals(AccountType.Customer.toString())) {
                    if (currentUser.getType().equals(AccountType.Business))
                        new AlertDialog.Builder(SettingsActivity.this, R.style.MyDialogTheme)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Switch to Customer?")
                                .setMessage("Switching from Business account to Customer account " +
                                        "will make your account private. Your Customers will no longer " +
                                        "be able to find you.\n\nDo you wish to continue?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    Toast.makeText(SettingsActivity.this, "Change noted.\n\nPress 'Save' button to apply changes.", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("No", (dialog, which) -> spAccountType.setSelection(types.indexOf(currentUser.getType().toString().replace("_", " "))))
                                .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
*/
        save = findViewById(R.id.btnProfileSave);
        save.setOnClickListener(this);
    }

    private void editName() {
        final EditText txtUsername = new EditText(this);

        //txtUsername.setPadding(18,0, 18, 0);

        txtUsername.setText(currentUser.getName());
        txtUsername.setHint("New Username");

        new AlertDialog.Builder(this)
                .setTitle("Update Username")
                .setMessage("Enter the new Username:")
                .setView(txtUsername)
                .setPositiveButton("Update", (dialog, whichButton) -> {
                    lblProfileUname.setText(txtUsername.getText().toString());
                })
                .setNegativeButton("Cancel", (dialog, whichButton) -> {
                })
                .show();
    }

}
