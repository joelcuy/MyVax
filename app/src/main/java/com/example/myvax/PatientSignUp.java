package com.example.myvax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myvax.Classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class PatientSignUp extends AppCompatActivity {

    EditText editTextUsername;
    EditText editTextPassword;
    EditText editTextFullName;
    EditText editTextEmail;
    EditText editTextICPassport;
    Button buttonSubmit;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_sign_up);
        editTextUsername = findViewById(R.id.edit_text_patient_signup_username);
        editTextPassword = findViewById(R.id.edit_text_patient_signup_password);
        editTextFullName = findViewById(R.id.edit_text_patient_signup_fullname);
        editTextEmail = findViewById(R.id.edit_text_patient_signup_email);
        editTextICPassport = findViewById(R.id.edit_text_patient_signup_icpassport);
        buttonSubmit = findViewById(R.id.btn_patient_signup_submit);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                String fullName = editTextFullName.getText().toString();
                String email = editTextEmail.getText().toString();
                String icPassport = editTextICPassport.getText().toString();
                if (username.trim().equals("") || password.trim().equals("") || fullName.trim().equals("")
                        || email.trim().equals("") || icPassport.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter data in all fields!", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                            .addOnCompleteListener(PatientSignUp.this, new OnCompleteListener<AuthResult>() {
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
                                        user.setUserType("patient");
                                        user.setIcpassport(editTextICPassport.getText().toString());
                                        user.setStaffID("");
                                        user.setCentreName("");
                                        user.setVaccinationID(" ");
                                        db.collection("Users")
                                                .document(mAuth.getCurrentUser().getUid())
                                                .set(user);
                                        Toast.makeText(PatientSignUp.this, "Patient created successfully!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(PatientSignUp.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(PatientSignUp.this,
                                                task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}