package com.example.myvax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myvax.Classes.HealthcareCentre;
import com.example.myvax.Classes.Vaccination;
import com.example.myvax.Classes.Vaccine;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PatientDashboard extends AppCompatActivity {

    CardView cardViewMakeAppointment;
    CardView cardViewMyAppointment;
    TextView textViewPatientFirstName;
    TextView textViewPatientFullName;
    TextView textViewPatientUsername;
    String fullName;
    String username;
    String[] nameParts;
    String firstName;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        cardViewMakeAppointment = findViewById(R.id.card_view_make_appointment);
        cardViewMyAppointment = findViewById(R.id.card_view_my_app);
        textViewPatientFirstName = findViewById(R.id.text_view_patient_first_name);
        textViewPatientFullName = findViewById(R.id.text_view_patient_full_name);
        textViewPatientUsername = findViewById(R.id.text_view_patient_username);

        //call functions to display patient info and check for existing appointment
         displayPatientInfo();

        cardViewMakeAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if card view is deactivated, it means that the user already has a pending appointment
                //and will not be allowed to make another appointment
                if (cardViewMakeAppointment.isActivated() == false) {
                    Toast.makeText(PatientDashboard.this,
                            "You already have an appointment!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(PatientDashboard.this, AvailableVaccines.class);
                    startActivity(intent);
                }

            }
        });


        cardViewMyAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PatientDashboard.this, MyAppointment.class);
                startActivity(intent);
            }
        });

    }

    public void displayPatientInfo() {

        DocumentReference userRef = db.collection("Users").document(LoginActivity.USER.getUserID());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        fullName = document.getString("fullName");
                        username = document.getString("username");
                        nameParts = fullName.split(" ");
                        firstName = nameParts[0];
                        textViewPatientFirstName.append(firstName + "!");
                        textViewPatientFullName.setText(fullName);
                        textViewPatientUsername.setText(username);

                        //call function to check whether patient has existing pending appointment
                        checkExistingAppointment();


                    }
                }

            }

        });
    }

    public void checkExistingAppointment() {

        db.collection("Vaccinations")
                .whereEqualTo("username", username)
                .whereEqualTo("status", "Pending")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //if results are returned, deactivate the Make Appointment card view
                            if (!task.getResult().isEmpty()) {
                                cardViewMakeAppointment.setActivated(false);
                            }
                            else
                                cardViewMakeAppointment.setActivated(true);
                        }
                    }
                });

    }

    public void logoutButton(View view) {
        Intent intent = new Intent(PatientDashboard.this, LoginActivity.class);
        startActivity(intent);
    }
}