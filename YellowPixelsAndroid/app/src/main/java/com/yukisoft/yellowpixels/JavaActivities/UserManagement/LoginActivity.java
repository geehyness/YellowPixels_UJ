package com.yukisoft.yellowpixels.JavaActivities.UserManagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.yukisoft.yellowpixels.JavaActivities.Home.HomeActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.UIElements.MyProgressDialog;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final FirebaseAuth fbAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Button login = findViewById(R.id.btnLogin);
        TextView reg = findViewById(R.id.tvRegister);

        login.setOnClickListener(this);
        reg.setOnClickListener(this);

        /*final ProgressDialog progressBar = new ProgressDialog(this);
        progressBar.setMessage("Checking user status");
        progressBar.show();
        if(fbAuth.getCurrentUser() != null){
            startActivity(new Intent(this, HomeActivity.class));
        }
        progressBar.hide();*/
    }

    @Override
    public void onClick(View v) {
        if (v == findViewById(R.id.btnLogin)){
            login();
        }
        if (v == findViewById(R.id.tvRegister)){
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Go back to App?")
                .setMessage("Are you sure you want to continue without logging in?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void login(){
        final UserModel[] currUser = {null};
        EditText username = findViewById(R.id.txtEmail),
                password = findViewById(R.id.txtPassword);

        final String uemail = username.getText().toString().trim();
        String upass = password.getText().toString().trim();

        // Validating input
        if (TextUtils.isEmpty(uemail)) {
            Toast.makeText(LoginActivity.this, "Email cannot be empty!", Toast.LENGTH_SHORT).show();
            username.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(uemail).matches()) {
            Toast.makeText(LoginActivity.this, "Email is invalid!", Toast.LENGTH_SHORT).show();
            username.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(upass)) {
            Toast.makeText(LoginActivity.this, "Password cannot be empty!", Toast.LENGTH_SHORT).show();
            password.requestFocus();
            return;
        }

        if (upass.length() < 8) {
            Toast.makeText(LoginActivity.this, "Password should be atleast 8 characters long!", Toast.LENGTH_SHORT).show();
            password.requestFocus();
            return;
        }

        final MyProgressDialog progressDialog = new MyProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        try{
            fbAuth.signInWithEmailAndPassword(uemail, upass)
                    .addOnSuccessListener(authResult -> {
                        FirebaseFirestore ff = FirebaseFirestore.getInstance();
                        final String loginId = fbAuth.getUid();

                        DocumentReference user = ff.document(CollectionName.USERS+"/"+loginId);
                        user.get().addOnSuccessListener(documentSnapshot -> {
                            progressDialog.dismiss();
                            UserModel userModel = documentSnapshot.toObject(UserModel.class);
                            Toast.makeText(LoginActivity.this, "Welcome back " + userModel.getName(), Toast.LENGTH_SHORT).show();

                            String userJSON = (new Gson()).toJson(userModel);
                            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                            i.putExtra(MainActivity.CURRENT_USER, userJSON);
                            startActivity(i);
                            LoginActivity.this.finish();
                        }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            fbAuth.signOut();
                            Toast.makeText(LoginActivity.this, "Error - " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Error - " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

        } catch (Exception e){
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
