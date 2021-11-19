package com.example.myvax;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AdminDashboard extends AppCompatActivity {

    TextView textViewAdminDashboard;
    TextView textViewName;
    TextView textViewCentre;
    CardView cardViewRecordBatch;
    CardView cardViewViewBatchInfo;
    public static String centreName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        textViewAdminDashboard = findViewById(R.id.text_view_admin_dashboard);
        textViewName = findViewById(R.id.text_view_dashboard_name);
        textViewCentre = findViewById(R.id.text_view_dashboard_centre);

        String fullName = LoginActivity.USER.getFullName();
        String[] nameParts = fullName.split(" ");
        String firstName = nameParts[0];
        String banner = "Hello, " + firstName + "!";

        centreName = LoginActivity.USER.getCentreName();
        textViewAdminDashboard.setText(banner);
        textViewName.setText(LoginActivity.USER.getFullName());
        textViewCentre.setText(centreName);

        cardViewRecordBatch = findViewById(R.id.card_record_batch);

        cardViewRecordBatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminDashboard.this, RecordVaccineBatch.class));
            }
        });

        cardViewViewBatchInfo = findViewById(R.id.card_view_batch_info);
        cardViewViewBatchInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminDashboard.this, ViewBatchInfoActivity.class));
            }
        });
    }

    public void logoutButton(View view) {
        Intent intent = new Intent(AdminDashboard.this, LoginActivity.class);
        startActivity(intent);
    }
}