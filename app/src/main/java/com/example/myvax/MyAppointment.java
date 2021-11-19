package com.example.myvax;

import static com.example.myvax.LoginActivity.USER;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myvax.Classes.Batch;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyAppointment extends AppCompatActivity {

    TextView textViewVaccinationID;
    TextView textViewVaccineName;
    TextView textViewCentreName;
    TextView textViewBatchNo;
    TextView textViewAppDate;
    TextView textViewStatus;
    TextView textViewRemarks;
    SharedPreferences sharedPreferences;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView imageViewClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointment);

        textViewVaccinationID = findViewById(R.id.text_view_my_app_vaccination_ID);
        textViewVaccineName = findViewById(R.id.text_view_my_app_vaccine_name);
        textViewCentreName = findViewById(R.id.text_view_my_app_centre_name);
        textViewBatchNo = findViewById(R.id.text_view_my_app_batch_no);
        textViewAppDate = findViewById(R.id.text_view_my_app_app_date);
        textViewStatus = findViewById(R.id.text_view_my_app_status);
        textViewRemarks = findViewById(R.id.text_view_my_app_remarks);
        imageViewClose = findViewById(R.id.image_view_close_btn);


        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyAppointment.this, PatientDashboard.class);
                startActivity(intent);
            }
        });

        //call function to retrieve vaccination info
       getVaccinationInfo();


    }



    private void getVaccinationInfo() {

        DocumentReference vaccinationRef = db.collection("Vaccinations").document(LoginActivity.USER.getVaccinationID());
        vaccinationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String vaccinationID = document.getString("vaccinationID");
                            String vaccineName = document.getString("vaccineName");
                            String centreName = document.getString("centreName");
                            String batchNo = document.getString("batchNo");
                            String appDate = document.getString("appointmentDate");
                            String status = document.getString("status");
                            String remarks = document.getString("remarks");

                            textViewVaccinationID.append(vaccinationID);
                            textViewVaccineName.append(vaccineName);
                            textViewCentreName.append(centreName);
                            textViewBatchNo.append(batchNo);
                            textViewAppDate.append(appDate);
                            textViewStatus.append(status);
                            textViewRemarks.append(remarks);

                        }
                        else{
                            Toast.makeText(MyAppointment.this, "Does not exist", Toast.LENGTH_SHORT);
                        }
                    }
                    else{
                        Toast.makeText(MyAppointment.this, "Unsuccessful", Toast.LENGTH_SHORT);
                    }


            }

        });

    }

}