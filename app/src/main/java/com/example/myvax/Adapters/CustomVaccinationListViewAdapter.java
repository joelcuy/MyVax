package com.example.myvax.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.myvax.BatchDetailsActivity;
import com.example.myvax.Classes.Batch;
import com.example.myvax.Classes.User;
import com.example.myvax.Classes.Vaccination;
import com.example.myvax.Classes.Vaccine;
import com.example.myvax.R;
import com.example.myvax.emailConfig.SendMailTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

//This class is basically extending the original adapter to customize the list view into the way we want
public class CustomVaccinationListViewAdapter extends ArrayAdapter<Vaccination> {
    //Components within each list view item (not popup)
    private TextView listViewVaccineID;
    private TextView listViewAppointmentDate;
    private TextView listViewVaccineStatus;
    private Button buttonStatusAction; //Button in each list item that launches pop up

    //Alert Dialog for popup
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private View confirmPopupView;
    private View administerPopupView;

    //Components inside popup
    private Button buttonRejectAppointment;
    private Button buttonConfirmAppointment;
    private Button buttonAdministerVaccine;
    private TextView textViewFullName;
    private TextView textViewIcPassport;
    private TextView textViewVaccineName;
    private TextView textViewManufacturer;
    private TextView textViewBatchNo;
    private TextView textViewExpiryDate;
    private EditText editTextRemarks;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CustomVaccinationListViewAdapter(@NonNull Context context, @NonNull ArrayList<Vaccination> vaccinationClassArrayList) {
        super(context, R.layout.activity_custom_vaccination_list_view_adapter, vaccinationClassArrayList);
    }

    @NonNull
    @Override
    //Compulsory to override this method to get each individual list item
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //initialize current vaccination based on position
        Vaccination selectedVaccinationClass = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_custom_vaccination_list_view_adapter, parent, false);
        }

        //Getting each view based on id, convertview refers to the individual customized listview (get from method parameter)
        //Need to use convertView because this is adapter class, not currently in activity class
        listViewVaccineID = convertView.findViewById(R.id.textView_customVaccinationList_vaccineID); //textview for each list item
        listViewAppointmentDate = convertView.findViewById(R.id.textView_customVaccinationList_appointmentDate); //textview for each list item
        listViewVaccineStatus = convertView.findViewById(R.id.textView_customVaccinationList_status); //textview for each list item
        buttonStatusAction = convertView.findViewById(R.id.button_customVaccinationList_actionStatus); //Button in each list item that launches pop up

        //Set textview to vaccination data
        listViewVaccineID.setText(selectedVaccinationClass.getVaccinationID());
        listViewAppointmentDate.setText("Date: " + selectedVaccinationClass.getAppointmentDate());
        listViewVaccineStatus.setText("Status: " + selectedVaccinationClass.getStatus());

        setManageMode(selectedVaccinationClass); //Enable or disable list view buttons
        setPopup(selectedVaccinationClass, parent); //Set logic for each list item button to launch pop up


        return convertView;
    }


    private void setManageMode(Vaccination selectedVaccinationClass) {
        //Enable or disable buttons depending on manage button
        if (BatchDetailsActivity.isManageable) {
            //TODO take note teammates change status names
            //When able to manage the vaccination
            switch (selectedVaccinationClass.getStatus()) {
                case "Pending":
                    buttonStatusAction.setText("Confirm");
                    buttonStatusAction.setEnabled(true);
                    break;

                case "Confirmed":
                    buttonStatusAction.setText("Administer");
                    buttonStatusAction.setEnabled(true);
                    break;

                case "Administered": //No button cuz nothing to do
                case "Rejected": //No button cuz nothing to do
                    buttonStatusAction.setText("");
                    buttonStatusAction.setVisibility(View.GONE);
                    break;
            }
        } else {
            //TODO take note teammates change status names
            //When unable to manage vaccination
            switch (selectedVaccinationClass.getStatus()) {
                case "Pending":
                    buttonStatusAction.setText("Confirm");
                    buttonStatusAction.setEnabled(false);
                    break;

                case "Confirmed":
                    buttonStatusAction.setText("Administer");
                    buttonStatusAction.setEnabled(false);
                    break;

                case "Administered":
                case "Rejected":
                    buttonStatusAction.setText("");
                    buttonStatusAction.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void setPopup(Vaccination selectedVaccinationClass, ViewGroup parent) {
        dialogBuilder = new AlertDialog.Builder(parent.getContext());
        dialogBuilder.setCancelable(true);

        //Logic for button within each list item (to launch popup)
        buttonStatusAction.setOnClickListener(v -> {
            //Remember this way to start new activity from non activity class (get parent context)
            //Intent zoom = new Intent(parent.getContext(), ManageVaccinationActivity.class);
            //parent.getContext().startActivity(zoom);

            //If vaccination status is pending launch confirm popup
            if (selectedVaccinationClass.getStatus().equals("Pending")) {
                confirmPopupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_confirm_vaccine, parent, false); //used to get the popup layout
                dialogBuilder.setView(confirmPopupView);
                //get text views of pop up confirm //TODO this layout not efficient, if extra time will utilize import code for repeated layouts
                textViewFullName = confirmPopupView.findViewById(R.id.textView_popupConfirm_value_fullName);
                textViewVaccineName = confirmPopupView.findViewById(R.id.textView_popupConfirm_value_vaccineName);
                textViewBatchNo = confirmPopupView.findViewById(R.id.textView_popupConfirm_value_batchNo);
                textViewIcPassport = confirmPopupView.findViewById(R.id.textView_popupConfirm_value_ICPassport);
                textViewManufacturer = confirmPopupView.findViewById(R.id.textView_popupConfirm_value_manufacturer);
                textViewExpiryDate = confirmPopupView.findViewById(R.id.textView_popupConfirm_value_expiryDate);


                //get buttons of pop up to administer a confirmed vaccination
                buttonRejectAppointment = confirmPopupView.findViewById(R.id.button_popupConfirm_reject);
                buttonConfirmAppointment = confirmPopupView.findViewById(R.id.button_popupConfirm_confirm);
                editTextRemarks = confirmPopupView.findViewById(R.id.editText_popupConfirm_remarks);

                //set buttons of pop up confirm
                buttonConfirmAppointment.setOnClickListener(v1 -> {
                    db.collection("Vaccinations").document(selectedVaccinationClass.getVaccinationID())
                            .update("status", "Confirmed",
                                    "remarks", editTextRemarks.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //Only when successfully confirmed, -1 pending
                                    db.collection("Batches").document(selectedVaccinationClass.getBatchNo())
                                            .update("quantityPending", FieldValue.increment(-1));

                                    db.collection("Users")
                                            .whereEqualTo("username", selectedVaccinationClass.getUsername())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            User userClass = document.toObject(User.class);

                                                            List<String> toEmailList = new ArrayList<String>();
                                                            toEmailList.add(userClass.getEmail());

                                                            new SendMailTask((Activity)parent.getContext()).execute("management.myVax2021@gmail.com",
                                                                    "myvax12345", toEmailList, "Vaccination Appointment Confirmed!", "This email is to notify that your vaccination appointment has been confirmed." +
                                                                            "\nLog in to My Vax app to check the full details.");
                                                        }
                                                    }
                                                }
                                            });
                                    //Snackbar to show successfully confirmed
                                    Snackbar snackbar = Snackbar.make(parent.findViewById(R.id.listView_batchDetails_vaccinationAppointmentList),
                                            "Appointment Confirmed", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(parent.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                    dialog.cancel();
                });

                buttonRejectAppointment.setOnClickListener(v12 -> {
                    db.collection("Vaccinations").document(selectedVaccinationClass.getVaccinationID())
                            .update("status", "Rejected",
                                    "remarks", editTextRemarks.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //Only when successfully rejected, will -1 pending, +1 available
                                    db.collection("Batches").document(selectedVaccinationClass.getBatchNo())
                                            .update("quantityPending", FieldValue.increment(-1),
                                                    "quantityAvailable", FieldValue.increment(1));

                                    //Snackbar to show successfully rejected
                                    Snackbar snackbar = Snackbar.make(parent.findViewById(R.id.listView_batchDetails_vaccinationAppointmentList),
                                            "Appointment rejected", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(parent.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                    dialog.cancel();

                });

                //If vaccination status is confirmed launch administer popup
            } else if (selectedVaccinationClass.getStatus().equals("Confirmed")) {
                administerPopupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_administer_vaccine, parent, false); //used to get the popup layout
                dialogBuilder.setView(administerPopupView);

                //get text views of pop up confirm
                textViewFullName = administerPopupView.findViewById(R.id.textView_popupAdminister_value_fullName);
                textViewVaccineName = administerPopupView.findViewById(R.id.textView_popupAdminister_value_vaccineName);
                textViewBatchNo = administerPopupView.findViewById(R.id.textView_popupAdminister_value_batchNo);
                textViewIcPassport = administerPopupView.findViewById(R.id.textView_popupAdminister_value_ICPassport);
                textViewManufacturer = administerPopupView.findViewById(R.id.textView_popupAdminister_value_manufacturer);
                textViewExpiryDate = administerPopupView.findViewById(R.id.textView_popupAdminister_value_expiryDate);

                //get buttons of pop up to administer a confirmed vaccination
                buttonAdministerVaccine = administerPopupView.findViewById(R.id.button_popupAdminister_administer);
                editTextRemarks = administerPopupView.findViewById(R.id.editText_popupAdminister_remarks);

                //set buttons of pop up administer
                buttonAdministerVaccine.setOnClickListener(v12 -> {
                    db.collection("Vaccinations").document(selectedVaccinationClass.getVaccinationID())
                            .update("status", "Administered",
                                    "remarks", editTextRemarks.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //Only when successfully administered, will +1 available
                                    db.collection("Batches").document(selectedVaccinationClass.getBatchNo())
                                            .update("quantityAdministered", FieldValue.increment(1));
                                    //snackbar when successfully administered
                                    Snackbar snackbar = Snackbar.make(parent.findViewById(R.id.listView_batchDetails_vaccinationAppointmentList), "Vaccination Administered", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(parent.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                    dialog.cancel();
                });
            }

            getPopupData(selectedVaccinationClass);

            dialog = dialogBuilder.create();
            dialog.show();
        });
    }

    //getting all data needed for the popup details
    private void getPopupData(Vaccination selectedVaccinationClass) {
        db.collection("Users")
                .whereEqualTo("username", selectedVaccinationClass.getUsername())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User userClass = document.toObject(User.class);
                                textViewFullName.setText(userClass.getFullName());
                                textViewIcPassport.setText(userClass.getIcpassport());
                            }
                        }
                    }
                });

        db.collection("Batches")
                .whereEqualTo("batchNo", selectedVaccinationClass.getBatchNo())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Batch selectedBatchClass = document.toObject(Batch.class);
                                textViewBatchNo.setText(selectedBatchClass.getBatchNo());
                                textViewExpiryDate.setText(selectedBatchClass.getExpiryDate());

                                //For each batch, get vaccineID to display name and manufacturer
                                db.collection("Vaccines")
                                        .whereEqualTo("vaccineID", selectedBatchClass.getVaccineID())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Vaccine vaccineClass = document.toObject(Vaccine.class);
                                                        textViewVaccineName.setText(vaccineClass.getVaccineName());
                                                        textViewManufacturer.setText(vaccineClass.getManufacturer());
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });


    }
}