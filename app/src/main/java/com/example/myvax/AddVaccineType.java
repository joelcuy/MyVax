package com.example.myvax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myvax.Classes.Vaccine;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.Document;
import com.google.firestore.v1.WriteResult;

public class AddVaccineType extends AppCompatActivity {

    EditText editTextVaccineID;
    EditText editTextVaccineName;
    EditText editTextManufacturer;
    Button btnSubmit;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vaccine_type);

        editTextVaccineID = findViewById(R.id.edit_text_add_vaccine_id);
        editTextVaccineName = findViewById(R.id.edit_text_add_vaccine_name);
        editTextManufacturer = findViewById(R.id.edit_text_add_vaccine_manufacturer);
        btnSubmit = findViewById(R.id.btn_add_vaccine_submit);
        String centreName = AdminDashboard.centreName;

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String vaccineID = editTextVaccineID.getText().toString();
                String vaccineName = editTextVaccineName.getText().toString();
                String manufacturer = editTextManufacturer.getText().toString();
                if (vaccineID.trim().equals("") || vaccineName.trim().equals("") || manufacturer.trim().equals("")) {
                    Toast.makeText(AddVaccineType.this, "Please enter data in all fields!", Toast.LENGTH_SHORT).show();
                } else {
                    DocumentReference vaccineRef = db.collection("Vaccines").document(vaccineID);
                    vaccineRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Toast.makeText(AddVaccineType.this, "VaccineID already exists!"
                                            , Toast.LENGTH_SHORT).show();
                                    editTextVaccineID.setText("");
                                    editTextVaccineID.requestFocus();
                                } else {
                                    Vaccine vaccine = new Vaccine();
                                    vaccine.setVaccineID(editTextVaccineID.getText().toString());
                                    vaccine.setVaccineName(editTextVaccineName.getText().toString());
                                    vaccine.setManufacturer(editTextManufacturer.getText().toString());
                                    db.collection("Vaccines")
                                            .document(editTextVaccineID.getText().toString())
                                            .set(vaccine);
                                    DocumentReference centresRef = db.collection("HealthcareCentres").document(centreName);
                                    centresRef.update("vaccineID",
                                            FieldValue.arrayUnion(editTextVaccineID.getText().toString()));
                                    Toast.makeText(AddVaccineType.this, "Vaccine added successfully!", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }
                        }
                    });

                }
            }
        });
    }
}