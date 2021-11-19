package com.example.myvax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myvax.Classes.Batch;
import com.example.myvax.Classes.Vaccine;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RecordVaccineBatch extends AppCompatActivity {

    FloatingActionButton floatingActionButtonAddVaccine;
    Spinner spinnerVaccine;
    EditText editTextBatchNumber;
    EditText editTextExpiryDate;
    EditText editTextQuantityAvailable;
    TextView textViewVaccineInfo;
    Button btnSubmit;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> vaccines;
    ArrayAdapter adapter;
    ImageButton imageButtonCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_vaccine_batch);

        floatingActionButtonAddVaccine = findViewById(R.id.floating_action_button_add_vaccine);
        spinnerVaccine = findViewById(R.id.spinner_vaccine);
        editTextBatchNumber = findViewById(R.id.edit_text_batch_number);
        editTextExpiryDate = findViewById(R.id.edit_text_expiry_date);
        editTextQuantityAvailable = findViewById(R.id.edit_text_quantity_of_doses);
        textViewVaccineInfo = findViewById(R.id.text_view_vaccine_info);
        btnSubmit = findViewById(R.id.btn_record_batch_submit);
        imageButtonCalendar = findViewById(R.id.image_button_add_batch_calendar);
        String centreName = LoginActivity.USER.getCentreName();


        db.collection("Vaccines")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    vaccines = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String vaccineID = document.getString("vaccineID");
                        vaccines.add(vaccineID);
                    }
                    adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, vaccines);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerVaccine.setAdapter(adapter);
                }
            }
        });

        spinnerVaccine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String vaccineId = parent.getItemAtPosition(pos).toString();
                DocumentReference vacRef = db.collection("Vaccines").document(vaccineId);
                vacRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Vaccine vaccine = documentSnapshot.toObject(Vaccine.class);
                        String vaccineInfo = "Vaccine Name: " + vaccine.getVaccineName()
                                + "\n" + "Manufacturer: " + vaccine.getManufacturer();
                        textViewVaccineInfo.setText(vaccineInfo);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        floatingActionButtonAddVaccine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RecordVaccineBatch.this, AddVaccineType.class));
            }
        });

        imageButtonCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int todayYear = calendar.get(Calendar.YEAR);
                int todayMonth = calendar.get(Calendar.MONTH);
                int todayDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(RecordVaccineBatch.this,
                        (view, year1, month1, dayOfMonth1) -> editTextExpiryDate.setText(dayOfMonth1 + "/" + (month1 + 1) + "/" + year1),
                        todayYear, todayMonth, todayDayOfMonth);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String batchNo = editTextBatchNumber.getText().toString();
                String expiryDate = editTextExpiryDate.getText().toString();
                String quantityAvailableStr = editTextQuantityAvailable.getText().toString();

                if (batchNo.trim().equals("") || expiryDate.trim().equals("") || quantityAvailableStr.trim().equals("")) {
                    Toast.makeText(RecordVaccineBatch.this, "Please enter data in all fields!", Toast.LENGTH_SHORT).show();
                } else {
                    int quantityAvailable = Integer.parseInt(editTextQuantityAvailable.getText().toString());
                    if (quantityAvailable <= 0) {
                        Toast.makeText(RecordVaccineBatch.this, "Quantity must be a positive number!", Toast.LENGTH_SHORT).show();
                        editTextQuantityAvailable.setText("");
                        editTextQuantityAvailable.requestFocus();
                    } else {
                        DocumentReference batchRef = db.collection("Batches").document(batchNo);
                        batchRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Toast.makeText(RecordVaccineBatch.this, "Batch number already exists!"
                                                , Toast.LENGTH_SHORT).show();
                                        editTextBatchNumber.setText("");
                                        editTextBatchNumber.requestFocus();
                                    } else {
                                        Batch batch = new Batch();
                                        batch.setBatchNo(editTextBatchNumber.getText().toString());
                                        batch.setExpiryDate(editTextExpiryDate.getText().toString());
                                        batch.setQuantityAvailable(Integer.parseInt(editTextQuantityAvailable.getText().toString()));
                                        batch.setQuantityAdministered(0);
                                        batch.setQuantityPending(0);
                                        batch.setCentreName(centreName);
                                        batch.setVaccineID(spinnerVaccine.getSelectedItem().toString());
                                        db.collection("Batches")
                                                .document(editTextBatchNumber.getText().toString())
                                                .set(batch);
                                        DocumentReference centresRef = db.collection("HealthcareCentres").document(centreName);
                                        centresRef.update("vaccineID",
                                                FieldValue.arrayUnion(spinnerVaccine.getSelectedItem().toString()));
                                        Toast.makeText(RecordVaccineBatch.this, "Batch created successfully!", Toast.LENGTH_LONG).show();
                                        finish();

                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public void refreshSpinner() {
        db.collection("Vaccines")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    vaccines = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String vaccineID = document.getString("vaccineID");
                        vaccines.add(vaccineID);
                    }
                    adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, vaccines);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerVaccine.setAdapter(adapter);
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSpinner();

    }
}