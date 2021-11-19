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

import com.example.myvax.Classes.Vaccine;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AvailableVaccines extends AppCompatActivity {

    ListView listViewVaccines;
    List<Vaccine> vaccinesArray;
    ArrayAdapter adapter;
    LinearLayout linearLayoutVaccines;
    String vaccineName;
    String vaccineID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_vaccines);

        listViewVaccines = findViewById(R.id.list_view_vaccines);
        linearLayoutVaccines = findViewById(R.id.linear_layout_vaccines);
        //call function to display the list of vaccines
        getAvailableVaccines();

        listViewVaccines.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AvailableVaccines.this, HealthcareCentreList.class);
                Vaccine vaccine = vaccinesArray.get(position);
                intent.putExtra("vaccine", vaccine);
                //for shared preference
                vaccineName = vaccinesArray.get(position).getVaccineName();
                vaccineID = vaccinesArray.get(position).getVaccineID();
                startActivity(intent);
                saveVaccineInfo();
            }
        });

    }

    private void getAvailableVaccines() {
        db.collection("Vaccines")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            vaccinesArray = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Vaccine vaccine = document.toObject(Vaccine.class);
                                vaccinesArray.add(vaccine);
                            }

                            adapter = new ArrayAdapter(AvailableVaccines.this,
                                    android.R.layout.simple_list_item_2,
                                    android.R.id.text1, vaccinesArray) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                                    text1.setText(vaccinesArray.get(position).getVaccineName());
                                    text2.setText(vaccinesArray.get(position).getManufacturer());
                                    //to set the style of the item and subitem
                                    text1.setTextAppearance(R.style.itemTitleStyle);
                                    text2.setTextAppearance(R.style.subItemStyle);
                                    return view;
                                }
                            };
                            listViewVaccines.setAdapter(adapter);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAvailableVaccines();
    }

    //to be used for querying in BatchList and setting vaccination info in RequestAppointment
    public void saveVaccineInfo() {
        sharedPreferences = getSharedPreferences("saveData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("vaccineName", vaccineName);
        editor.putString("vaccineID", vaccineID);
        editor.apply();
    }
}