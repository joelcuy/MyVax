package com.example.myvax;


import static com.example.myvax.LoginActivity.USER;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myvax.Classes.Batch;
import com.example.myvax.Classes.HealthcareCentre;
import com.example.myvax.Classes.Vaccination;
import com.example.myvax.Classes.Vaccine;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class RequestAppointment extends AppCompatActivity {

    TextView textViewExpiryDate;
    TextView textViewNoOfAvailableVaccines;
    TextView editTextSelectedDate;
    Button buttonRequestAppointment;
    Vaccination vaccination;
    Batch batch;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String expiryDateString;
    ImageButton imageButtonCalendar;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_appointment);

        textViewExpiryDate = findViewById(R.id.text_view_expiry_date);
        textViewNoOfAvailableVaccines = findViewById(R.id.text_view_no_of_available_vaccines);
        editTextSelectedDate = findViewById(R.id.edit_text_selected_date);
        buttonRequestAppointment = findViewById(R.id.button_request_appointment);
        imageButtonCalendar = findViewById(R.id.image_button_calendar);

        getBatchInfo();
        selectAppointmentDate();

        buttonRequestAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editTextSelectedDate.getText().toString().isEmpty()) {
                    if (vaccination == null) {
                        //if expired, display error message
                        if (checkDates() == false)
                            Toast.makeText(RequestAppointment.this,
                                    "The batch would have expired! Please select a different date", Toast.LENGTH_SHORT).show();
                        else {
                            try {
                                vaccination = new Vaccination();
                                vaccination.setAppointmentDate(editTextSelectedDate.getText().toString());
                                //generate random 5 digit number as vaccinationID
                                Random r = new Random();
                                vaccination.setVaccinationID("VAX" + (r.nextInt(99999-10000)+10000));
                                vaccination.setStatus("Pending");
                                vaccination.setUsername(USER.getUsername());
                                batch = (Batch) getIntent().getSerializableExtra("batch");
                                vaccination.setBatchNo(batch.getBatchNo());
                                vaccination.setRemarks("-");

                                //retrieve vaccineName and centreName
                                sharedPreferences = getSharedPreferences("saveData", MODE_PRIVATE);
                                String vaccineName = sharedPreferences.getString("vaccineName", "");
                                String centreName = sharedPreferences.getString("centreName", "");
                                vaccination.setVaccineName(vaccineName);
                                vaccination.setCentreName(centreName);
                            } catch (Exception ex) {
                                Toast.makeText(RequestAppointment.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            DocumentReference userRef = db.collection("Users").document(USER.getUserID());
                            userRef.update("vaccinationID", vaccination.getVaccinationID());
                            LoginActivity.USER.setVaccinationID(vaccination.getVaccinationID());

                            DocumentReference batchRef = db.collection("Batches").document(batch.getBatchNo());
                            //quantityPending will increase by 1, quantityAvailable will decrease by 1
                            batchRef.update("quantityPending", FieldValue.increment(1));
                            batchRef.update("quantityAvailable", FieldValue.increment(-1));


                            db.collection("Vaccinations")
                                    .document(vaccination.getVaccinationID())
                                    .set(vaccination)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Intent intent = new Intent(RequestAppointment.this, AppointmentSuccess.class);
                                            startActivity(intent);
                                            finish();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RequestAppointment.this,
                                                    e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }


                    }

                }
            }

        });
    }

    private void getBatchInfo() {
        //retrieve batch obj
        batch = (Batch) getIntent().getSerializableExtra("batch");
        DocumentReference batchRef = db.collection("Batches").document(batch.getBatchNo());
        batchRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        expiryDateString = document.getString("expiryDate");
                        int quantityAvailable = document.getLong("quantityAvailable").intValue();
                        textViewExpiryDate.append(expiryDateString);
                        textViewNoOfAvailableVaccines.append(String.valueOf(quantityAvailable));

                    } else {
                        Toast.makeText(RequestAppointment.this,
                                "Does not exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RequestAppointment.this,
                            "Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //convert dates from String to Date format to check whether expiry date is before or after selected date
    //if it is before selectedDate, returns false because it has expired, otherwise returns true
    private boolean checkDates() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date expiryDate = null;
        try {
            expiryDate = formatter.parse(expiryDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String selectedDateString = editTextSelectedDate.getText().toString();
        Date selectedDate = null;
        try {
            selectedDate = formatter.parse(selectedDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (expiryDate.compareTo(selectedDate) < 0) {
            return false;
        }
        return true;
    }

    private void selectAppointmentDate(){

        imageButtonCalendar.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int todayYear = calendar.get(Calendar.YEAR);
            int todayMonth = calendar.get(Calendar.MONTH);
            int todayDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(RequestAppointment.this,
                    (view, year1, month1, dayOfMonth1) -> editTextSelectedDate.setText(dayOfMonth1 + "/" + (month1 + 1) + "/" + year1),
                    todayYear, todayMonth, todayDayOfMonth);
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            datePickerDialog.show();

        });

    }


}