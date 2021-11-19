package com.example.myvax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.myvax.Classes.HealthcareCentre;
import com.example.myvax.Classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminSignUp extends AppCompatActivity {

    RadioGroup radioGroupCentres;
    RadioButton radioButtonExisting;
    RadioButton radioButtonNew;
    Spinner spinnerCentre;
    EditText editTextCentreName;
    EditText editTextCentreAddress;
    EditText editTextUsername;
    EditText editTextPassword;
    EditText editTextFullName;
    EditText editTextEmail;
    EditText editTextStaffID;
    Button btnSubmit;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    User user;
    List<String> centres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sign_up);

        radioGroupCentres = findViewById(R.id.radio_group_centre);
        radioButtonExisting = findViewById(R.id.radio_btn_existing_centre);
        radioButtonNew = findViewById(R.id.radio_btn_new_centre);
        spinnerCentre = (Spinner) findViewById(R.id.spinner_centre);
        editTextCentreName = findViewById(R.id.edit_text_centre_name);
        editTextCentreAddress = findViewById(R.id.edit_text_centre_address);
        editTextUsername = findViewById(R.id.edit_text_admin_signup_username);
        editTextPassword = findViewById(R.id.edit_text_admin_signup_password);
        editTextFullName = findViewById(R.id.edit_text_admin_signup_fullname);
        editTextEmail = findViewById(R.id.edit_text_admin_signup_email);
        editTextStaffID = findViewById(R.id.edit_text_admin_signup_staffID);
        btnSubmit = findViewById(R.id.btn_admin_signup_submit);

        centres = new ArrayList<>();
        CollectionReference centresRef = db.collection("HealthcareCentres");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, centres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCentre.setAdapter(adapter);
        centresRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String centreName = document.getString("centreName");
                        centres.add(centreName);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

        if (radioButtonExisting.isChecked()) {
            editTextCentreName.setEnabled(false);
            editTextCentreAddress.setEnabled(false);
            spinnerCentre.setEnabled(true);
        }

        radioButtonExisting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextCentreName.setEnabled(false);
                editTextCentreAddress.setEnabled(false);
                spinnerCentre.setEnabled(true);
            }
        });

        radioButtonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextCentreName.setEnabled(true);
                editTextCentreAddress.setEnabled(true);
                spinnerCentre.setEnabled(false);
            }
        });


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (radioButtonExisting.isChecked()) {
                        String username = editTextUsername.getText().toString();
                        String password = editTextPassword.getText().toString();
                        String fullName = editTextFullName.getText().toString();
                        String email = editTextEmail.getText().toString();
                        String staffID = editTextStaffID.getText().toString();
                        if (username.trim().equals("") || password.trim().equals("") || fullName.trim().equals("") || email.trim().equals("") || staffID.trim().equals("")) {
                            Toast.makeText(getApplicationContext(), "Please enter data in all fields!", Toast.LENGTH_SHORT).show();
                        } else {
                            mAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                                    .addOnCompleteListener(AdminSignUp.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                DocumentReference newUserRef = db.collection("Users").document();
                                                user = new User();
                                                user.setUserID(mAuth.getCurrentUser().getUid());
                                                user.setUsername(editTextUsername.getText().toString());
                                                user.setPassword(editTextPassword.getText().toString());
                                                user.setEmail(editTextEmail.getText().toString());
                                                user.setFullName(editTextFullName.getText().toString());
                                                user.setUserType("admin");
                                                user.setIcpassport("");
                                                user.setStaffID(editTextStaffID.getText().toString());
                                                user.setCentreName(spinnerCentre.getSelectedItem().toString());
                                                db.collection("Users")
                                                        .document(mAuth.getCurrentUser().getUid())
                                                        .set(user);
                                                Toast.makeText(AdminSignUp.this, "Admin created successfully!", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(AdminSignUp.this, LoginActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(AdminSignUp.this,
                                                        task.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                        }
                    } else if (radioButtonNew.isChecked()) {
                        String centreName = editTextCentreName.getText().toString();
                        String centreAddress = editTextCentreAddress.getText().toString();
                        String username = editTextUsername.getText().toString();
                        String password = editTextPassword.getText().toString();
                        String fullName = editTextFullName.getText().toString();
                        String email = editTextEmail.getText().toString();
                        String staffID = editTextStaffID.getText().toString();
                        if(centreName.trim().equals("") || centreAddress.trim().equals("") || username.trim().equals("") || password.trim().equals("") || fullName.trim().equals("") || email.trim().equals("") || staffID.trim().equals("")){
                            Toast.makeText(getApplicationContext(), "Please enter data in all fields!", Toast.LENGTH_SHORT).show();
                        } else {
                            mAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                                    .addOnCompleteListener(AdminSignUp.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()){
                                                user = new User();
                                                user.setUserID(mAuth.getCurrentUser().getUid());
                                                user.setUsername(editTextUsername.getText().toString());
                                                user.setPassword(editTextPassword.getText().toString());
                                                user.setEmail(editTextEmail.getText().toString());
                                                user.setFullName(editTextFullName.getText().toString());
                                                user.setUserType("admin");
                                                user.setIcpassport("");
                                                user.setVaccinationID(" ");
                                                user.setStaffID(editTextStaffID.getText().toString());
                                                user.setCentreName(editTextCentreName.getText().toString());
                                                db.collection("Users")
                                                        .document(mAuth.getCurrentUser().getUid())
                                                        .set(user);
                                                HealthcareCentre centre = new HealthcareCentre();
                                                centre.setCentreName(editTextCentreName.getText().toString());
                                                centre.setCentreAddress(editTextCentreAddress.getText().toString());
                                                centre.setVaccineID(Arrays.asList());
                                                centresRef.document(editTextCentreName.getText().toString()).set(centre);
                                                Toast.makeText(AdminSignUp.this, "Admin created successfully!", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(AdminSignUp.this, LoginActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(AdminSignUp.this,
                                                        task.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }
                    }
//                    mAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
//                            .addOnCompleteListener(AdminSignUp.this, new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    if (task.isSuccessful()) {
//                                        if (radioButtonExisting.isChecked()) {
//                                            DocumentReference newUserRef = db.collection("Users").document();
//                                            user = new User();
//                                            user.setUserID(mAuth.getCurrentUser().getUid());
//                                            user.setUsername(editTextUsername.getText().toString());
//                                            user.setPassword(editTextPassword.getText().toString());
//                                            user.setEmail(editTextEmail.getText().toString());
//                                            user.setFullName(editTextFullName.getText().toString());
//                                            user.setUserType("admin");
//                                            user.setICPassport("");
//                                            user.setStaffID(editTextStaffID.getText().toString());
//                                            user.setCentreName(spinnerCentre.getSelectedItem().toString());
//                                            db.collection("Users")
//                                                    .document(mAuth.getCurrentUser().getUid())
//                                                    .set(user);
//                                            startActivity(new Intent(AdminSignUp.this, LoginActivity.class));
//                                            finish();
//                                        } else if (radioButtonNew.isChecked()) {
//                                            user = new User();
//                                            user.setUserID(mAuth.getCurrentUser().getUid());
//                                            user.setUsername(editTextUsername.getText().toString());
//                                            user.setPassword(editTextPassword.getText().toString());
//                                            user.setEmail(editTextEmail.getText().toString());
//                                            user.setFullName(editTextFullName.getText().toString());
//                                            user.setUserType("admin");
//                                            user.setICPassport("");
//                                            user.setStaffID(editTextStaffID.getText().toString());
//                                            user.setCentreName(editTextCentreName.getText().toString());
//                                            db.collection("Users")
//                                                    .document(mAuth.getCurrentUser().getUid())
//                                                    .set(user);
//                                            HealthcareCentre centre = new HealthcareCentre();
//                                            centre.setCentreName(editTextCentreName.getText().toString());
//                                            centre.setCentreAddress(editTextCentreAddress.getText().toString());
//                                            centre.setVaccineID(Arrays.asList());
//                                            centresRef.document(editTextCentreName.getText().toString()).set(centre);
//                                            startActivity(new Intent(AdminSignUp.this, LoginActivity.class));
//                                            finish();
//                                        }
//                                    } else {
//                                        Toast.makeText(AdminSignUp.this,
//                                                task.getException().getMessage(),
//                                                Toast.LENGTH_SHORT).show();
//
//                                    }
//                                }
//                            });
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }


        });
    }

}