package com.example.myvax.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myvax.Classes.Batch;
import com.example.myvax.Classes.Vaccine;
import com.example.myvax.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CustomBatchListViewAdapter extends ArrayAdapter<Batch> { //custom list view settings

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CustomBatchListViewAdapter(@NonNull Context context, @NonNull ArrayList<Batch> batchClassArrayList) {
        super(context, R.layout.activity_custom_batch_list_view_adapter, batchClassArrayList);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Batch batchClass = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_custom_batch_list_view_adapter, parent, false);
        }

        TextView textViewBatchNo = convertView.findViewById(R.id.textView_customBatchList_batchNo);
        TextView textViewVaccineName = convertView.findViewById(R.id.textView_customBatchList_vaccineName);
        TextView textViewAdditionalInfo = convertView.findViewById(R.id.textView_customBatchList_additionalInfo);

        //Get vaccine names via each batch, it works but inefficient, will update it got better way
        db.collection("Vaccines")
                .whereEqualTo("vaccineID", batchClass.getVaccineID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Vaccine vaccineClass = document.toObject(Vaccine.class);
                                textViewVaccineName.setText("Vaccine: " + vaccineClass.getVaccineName());
                            }
                        }
                    }
                });
        textViewBatchNo.setText(batchClass.getBatchNo());
        textViewAdditionalInfo.setText(String.valueOf(batchClass.getQuantityPending()) + " Pending");

        return convertView;
    }
}
