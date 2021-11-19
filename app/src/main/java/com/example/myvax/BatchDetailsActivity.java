package com.example.myvax;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myvax.Adapters.CustomVaccinationListViewAdapter;
import com.example.myvax.Classes.Batch;
import com.example.myvax.Classes.Vaccination;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class BatchDetailsActivity extends AppCompatActivity {
    //View components
    private ListView listViewVaccinationClass;
    private TextView textView_expiryDate_value;
    private TextView textView_pending_value;
    private TextView textView_availability_value;
    private TextView textView_administered_value;
    private Button manageVaccination;

    //Other variables
    private ArrayList<Vaccination> vaccinationClassArrayList; //store list of vaccination obj related to current batch
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration vaccinationListenerRegistration; // for live listener
    private ListenerRegistration batchListenerRegistration;
    private ListAdapter listAdapter;
    private Batch selectedBatchClass; //to store current batch obj

    //To decide if list item buttons enabled or disabled
    //public static cuz need to be accessed in another class without creating obj
    public static Boolean isManageable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_details);

        //Store batch object from previous activity
        selectedBatchClass = (Batch) getIntent().getSerializableExtra("batchClass");

        initializeBatchDetails(); //set and display all batch details
        getCurrentBatchVaccinationAppointment(); //set and display all vaccination details
        initializeVaccinationManagement(); //set ability to manage vaccinations

        // calling the action bar and display back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onSupportNavigateUp() { //sets the function for back button on action bar
        finish();
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //When press back button, make sure manageable false and text is set back to manage
        //Otherwise if go into another batch it will still be on manage mode
        isManageable = false;
        manageVaccination.setText("Manage");

        //Detach live listener, to stop listening for data changes when activity stops
        if (vaccinationListenerRegistration != null) {
            vaccinationListenerRegistration.remove();
        }
        if (batchListenerRegistration != null) {
            batchListenerRegistration.remove();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getCurrentBatchVaccinationAppointment(); //start listening for live data changes again
        initializeBatchDetails(); //start listening for live data changes again
    }


    private void initializeVaccinationManagement() {
        //idk why it doesnt work when i put this few lines of code here, had to move it to getCurrentBatchVaccinationAppointment()
        //cuz list put as global, logically should be able to retrieve list, but shows null when executing it outside of the firebasedb method
//        if (vaccinationClassArrayList.size() == 0) {
//            manageVaccination.setEnabled(false);
//        }
        manageVaccination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isManageable = isManageable ? false : true; //it is false onCreate
                String buttonText = isManageable ? "Done" : "Manage";
                manageVaccination.setText(buttonText);
                getCurrentBatchVaccinationAppointment();
            }
        });
    }

    //get all views and buttons via id and set the values
    //Change to live listener, for real time updates
    private void initializeBatchDetails() {
        //find view by ID
        listViewVaccinationClass = findViewById(R.id.listView_batchDetails_vaccinationAppointmentList);
        textView_expiryDate_value = findViewById(R.id.textView_batchDetails_value_expiryDate);
        textView_pending_value = findViewById(R.id.textView_batchDetails_value_pending);
        textView_availability_value = findViewById(R.id.textView_batchDetails_value_availability);
        textView_administered_value = findViewById(R.id.textView_batchDetails_value_administered);
        manageVaccination = findViewById(R.id.button_batchDetails_manageVaccinationAppointment);

        //For live updates of batch details purposes
        batchListenerRegistration = db.collection("Batches")
                .whereEqualTo("batchNo", selectedBatchClass.getBatchNo())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        for (QueryDocumentSnapshot doc : value) {
                            Batch batchClass = doc.toObject(Batch.class);
                            textView_expiryDate_value.setText(String.valueOf(batchClass.getExpiryDate()));
                            textView_pending_value.setText(String.valueOf(batchClass.getQuantityPending()));
                            textView_availability_value.setText(String.valueOf(batchClass.getQuantityAvailable()));
                            textView_administered_value.setText(String.valueOf(batchClass.getQuantityAdministered()));
                        }
                    }
                });
        getSupportActionBar().setTitle(selectedBatchClass.getBatchNo()); //set activity title
    }

    //Selects vaccinations based on batchNo to display in listview
    //Uses realtime update listener for live data changes
    public void getCurrentBatchVaccinationAppointment() {
        vaccinationListenerRegistration = db.collection("Vaccinations")
                .whereEqualTo("batchNo", selectedBatchClass.getBatchNo())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        vaccinationClassArrayList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Vaccination vaccinationClass = doc.toObject(Vaccination.class);
                            vaccinationClassArrayList.add(vaccinationClass);
                        }

                        if (vaccinationClassArrayList.size() == 0) {
                            manageVaccination.setEnabled(false);
                        } //set manage button to disabled if list is empty

                        listAdapter = new CustomVaccinationListViewAdapter(BatchDetailsActivity.this, vaccinationClassArrayList);
                        listViewVaccinationClass.setAdapter(listAdapter);
                    }
                });
    }
}