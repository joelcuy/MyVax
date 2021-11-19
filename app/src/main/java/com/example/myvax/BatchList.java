package com.example.myvax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myvax.Classes.Batch;
import com.example.myvax.Classes.HealthcareCentre;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BatchList extends AppCompatActivity {

    ListView listViewBatches;
    List<Batch> batchesArray;
    ArrayAdapter adapter;
    LinearLayout linearLayoutBatches;
    HealthcareCentre healthcareCentre;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    String expiryDateString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_list);
        listViewBatches = findViewById(R.id.list_view_batches);
        linearLayoutBatches = findViewById(R.id.linear_layout_batches);
        //call function to display list of batches from the selected healthcare centre
        getBatches();

        listViewBatches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(BatchList.this, RequestAppointment.class);
                Batch batch = batchesArray.get(position);
                intent.putExtra("batch", batch);
                startActivity(intent);
            }
        });
    }

    private void getBatches() {
        //retrieve vaccineID that was saved in shared preference earlier
        sharedPreferences = getSharedPreferences("saveData", MODE_PRIVATE);
        String vaccineID = sharedPreferences.getString("vaccineID", "");
        //retrieve healthcare centre obj from intent
        healthcareCentre = (HealthcareCentre) getIntent().getSerializableExtra("healthcareCentre");

        db.collection("Batches")
                .whereEqualTo("vaccineID", vaccineID)
                .whereEqualTo("centreName", healthcareCentre.getCentreName())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        try{
                            if (task.isSuccessful()) {
                                batchesArray = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Batch batch = document.toObject(Batch.class);
                                    expiryDateString = batch.getExpiryDate();
                                    //if batch has more than 0 quantity and not yet expired, add to list to be displayed
                                    if(batch.getQuantityAvailable()>0 && checkBatchExpiry())
                                        batchesArray.add(batch);
                                }

                                adapter = new ArrayAdapter(BatchList.this,
                                        android.R.layout.simple_list_item_2,
                                        android.R.id.text1, batchesArray) {
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getView(position, convertView, parent);
                                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                                        text1.setText(batchesArray.get(position).getBatchNo());
                                        text1.setTextAppearance(R.style.itemTitleStyle);
                                        text2.setText("Expires on " + batchesArray.get(position).getExpiryDate());
                                        text2.setTextAppearance(R.style.subItemStyle);
                                        return view;
                                    }
                                };
                                listViewBatches.setAdapter(adapter);

                                //if no query is returned or list is empty, display toast msg
                                if(batchesArray.isEmpty())
                                    Toast.makeText(BatchList.this, "There are no batches available!", Toast.LENGTH_SHORT).show();

                            }
                            else
                                Toast.makeText(BatchList.this, "Error", Toast.LENGTH_LONG).show();
                        }catch(Exception ex){
                            Toast.makeText(BatchList.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        }


                    }
                });
    }

    private boolean checkBatchExpiry(){

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        //retrieve current date in string format
        String currentDateString = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        Date currentDate = null;
        try {
            //convert to Date format
            currentDate = formatter.parse(currentDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date expiryDate = null;
        try {
            //convert to Date format
            expiryDate = formatter.parse(expiryDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //if expiry date is before current date, return false. else return true
        if(expiryDate.compareTo(currentDate) < 0){
            return false;
        }
        return true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        getBatches();
    }

}