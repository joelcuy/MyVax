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

import com.example.myvax.Classes.HealthcareCentre;
import com.example.myvax.Classes.Vaccine;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HealthcareCentreList extends AppCompatActivity {

    ListView listViewHealthcareCentres;
    List<HealthcareCentre> healthcareCentreArray;
    ArrayAdapter adapter;
    LinearLayout linearLayoutHealthcareCentre;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Vaccine vaccine;
    String centreName;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthcare_centre_list);

        listViewHealthcareCentres = findViewById(R.id.list_view_healthcare_centres);
        linearLayoutHealthcareCentre = findViewById(R.id.linear_layout_healthcare_centres);

        //call function to retrieve list of centres offering the vaccine selected
        getHealthcareCentres();

        listViewHealthcareCentres.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(HealthcareCentreList.this, BatchList.class);
                HealthcareCentre healthcareCentre = healthcareCentreArray.get(position);
                intent.putExtra("healthcareCentre", healthcareCentre);
                //for shared preference
                centreName = healthcareCentreArray.get(position).getCentreName();
                saveHealthcareCentreName();
                startActivity(intent);
            }
        });



    }
    private void getHealthcareCentres() {
        //retrieve vaccine obj
        vaccine = (Vaccine) getIntent().getSerializableExtra("vaccine");
        db.collection("HealthcareCentres")
                .whereArrayContains("vaccineID", vaccine.getVaccineID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            healthcareCentreArray = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HealthcareCentre healthcareCentre = document.toObject(HealthcareCentre.class);
                                healthcareCentreArray.add(healthcareCentre);
                            }

                            adapter = new ArrayAdapter(HealthcareCentreList.this,
                                    android.R.layout.simple_list_item_2,
                                    android.R.id.text1, healthcareCentreArray) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                                    text1.setText(healthcareCentreArray.get(position).getCentreName());
                                    text2.setText(healthcareCentreArray.get(position).getCentreAddress());
                                    text1.setTextAppearance(R.style.itemTitleStyle);
                                    text2.setTextAppearance(R.style.subItemStyle);
                                    return view;
                                }
                            };
                            listViewHealthcareCentres.setAdapter(adapter);

                        }


                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getHealthcareCentres();
    }

    //to be used for setting vaccination info in RequestAppointment
    public void saveHealthcareCentreName() {
        sharedPreferences = getSharedPreferences("saveData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("centreName", centreName);
        editor.apply();
    }

}