package com.example.myvax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myvax.Classes.Vaccination;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppointmentSuccess extends AppCompatActivity {

    Vaccination vaccination;
    TextView textViewStatus;
    TextView textViewVaccinationID;
    Button buttonViewAppointment;
    String vaccinationID;
    String status;
    SharedPreferences sharedPreferences;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_success);
        textViewVaccinationID = findViewById(R.id.text_view_vaccination_ID);
        textViewStatus = findViewById(R.id.text_view_status);
        buttonViewAppointment = findViewById(R.id.button_view_appointment);

        buttonViewAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppointmentSuccess.this, MyAppointment.class);
                startActivity(intent);
            }
        });

        //call the function to display vaccinationID and status
        getAppointmentSuccessInfo();
    }

    private void getAppointmentSuccessInfo() {
        DocumentReference vaccinationRef = db.collection("Vaccinations").document(LoginActivity.USER.getVaccinationID());
        vaccinationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        vaccinationID = document.getString("vaccinationID");
                        status = document.getString("status");
                        textViewVaccinationID.setText(vaccinationID);
                        textViewStatus.setText(status);

                    } else {
                        Toast.makeText(AppointmentSuccess.this,
                                "Does not exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AppointmentSuccess.this,
                            "Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}