package com.example.myvax;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HomeSignUp extends AppCompatActivity {

    Button btnSignUpAdmin;
    Button btnSignUpPatient;
    TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_sign_up);

        btnSignUpAdmin = findViewById(R.id.btn_signup_admin);
        btnSignUpPatient = findViewById(R.id.btn_signup_patient);
        textViewLogin = findViewById(R.id.text_view_login_here);

        btnSignUpAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeSignUp.this, AdminSignUp.class));
            }
        });

        btnSignUpPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeSignUp.this, PatientSignUp.class));
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeSignUp.this, LoginActivity.class));
            }
        });

    }
}