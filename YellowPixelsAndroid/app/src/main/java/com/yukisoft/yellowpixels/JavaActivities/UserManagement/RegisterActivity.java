package com.yukisoft.yellowpixels.JavaActivities.UserManagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.yukisoft.yellowpixels.JavaActivities.AppSpecific.PrivacyActivity;
import com.yukisoft.yellowpixels.JavaActivities.AppSpecific.TermsActivity;
import com.yukisoft.yellowpixels.JavaActivities.Home.HomeActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.AccountType;
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.CollectionName;
import com.yukisoft.yellowpixels.JavaRepositories.UIElements.MyProgressDialog;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.JavaActivities.MainActivity;
import com.yukisoft.yellowpixels.R;

import java.util.Date;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private final FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    EditText editTextName,
            editTextEmail,
            editTextPassword;
    //Spinner spinnerUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        editTextName = findViewById(R.id.txtRegName);
        editTextEmail = findViewById(R.id.txtRegEmail);
        editTextPassword = findViewById(R.id.txtRegPassword);
        //spinnerUserType = findViewById(R.id.spUserType);

        Button reg = findViewById(R.id.btnRegister);
        TextView login = findViewById(R.id.lblLogin);
        TextView terms = findViewById(R.id.tvTnC);
        TextView privacy = findViewById(R.id.tvPP);

        reg.setOnClickListener(this);
        login.setOnClickListener(this);
        terms.setOnClickListener(this);
        privacy.setOnClickListener(this);

        /*final ProgressDialog progressBar = new ProgressDialog(this);
        progressBar.setMessage("Checking user status");
        progressBar.show();
        if(fbAuth.getCurrentUser() != null){
            startActivity(new Intent(this, HomeActivity.class));
        }
        progressBar.hide();*/

        String[] types = {"Select Account Type", AccountType.Customer.toString(), AccountType.Business.toString()};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinnerUserType.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (v == findViewById(R.id.btnRegister)){
            registerUser();
        }
        if (v == findViewById(R.id.lblLogin)){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        if(v == findViewById(R.id.tvTnC)){
            startActivity(new Intent(RegisterActivity.this, TermsActivity.class));
        }
        if(v == findViewById(R.id.tvPP)){
            startActivity(new Intent(RegisterActivity.this, PrivacyActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, R.style.MyDialogTheme)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Go back to App?")
            .setMessage("Do you wish to continue without Registering?")
            .setPositiveButton("Yes", (dialog, which) -> {
                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                finish();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void registerUser() {
        final String uname = editTextName.getText().toString().trim();
        final String uemail = editTextEmail.getText().toString().trim();
        String upass = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(uname)) {
            editTextName.setError("Name cannot be empty!");
            return;
        }

        if (TextUtils.isEmpty(uemail)) {
            editTextEmail.setError("Email cannot be empty!");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(uemail).matches()) {
            editTextEmail.setError("Email is invalid!");
            return;
        }

        if (TextUtils.isEmpty(upass)) {
            editTextPassword.setError("Password cannot be empty!");
            return;
        }

        if (upass.length() < 8) {
            editTextPassword.setError("Password should be atleast 8 characters long!");
            return;
        }

        /*if (spinnerUserType.getSelectedItemPosition() == 0) {
            ((TextView)spinnerUserType.getSelectedView()).setError("Select Account Type!");
            return;
        }*/

        CheckBox tnc = findViewById(R.id.cbTnC);
        CheckBox pp = findViewById(R.id.cbPP);

        if(!tnc.isChecked()){
            Toast.makeText(RegisterActivity.this, "You have to agree to the Terms & Conditions before you continue.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!pp.isChecked()){
            Toast.makeText(RegisterActivity.this, "You have to agree to the Privacy Policy before you continue.", Toast.LENGTH_SHORT).show();
            return;
        }

        final MyProgressDialog progressDialog = new MyProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        try{
            fbAuth.createUserWithEmailAndPassword(uemail, upass)
                    .addOnSuccessListener(authResult -> {
                        Date date = new Date();
                        UserModel user = new UserModel(fbAuth.getUid(), uname, uemail, AccountType.Business, date);

                        FirebaseFirestore ff = FirebaseFirestore.getInstance();

                        ff.collection(CollectionName.USERS).document(user.getId()).set(user).addOnSuccessListener(aVoid -> {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class)
                                    .putExtra(MainActivity.CURRENT_USER, (new Gson()).toJson(user)));
                            finish();
                        }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registration Failed.\n\nError - " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e){
            Toast.makeText(RegisterActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }

    }
}