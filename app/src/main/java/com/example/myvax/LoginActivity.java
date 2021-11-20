package com.example.myvax;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myvax.Classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton;

public class LoginActivity extends AppCompatActivity {

    public static User USER;
    EditText editTextEmail;
    EditText editTextPassword;
    Button btnLogin;
    CheckBox checkBoxShowPassword;
    CheckBox checkBoxRemember;
    TextView textViewSignUpNow;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    SharedPreferences sharedPreferences;

    //Huawei Login
    public static final String TAG = "HuaweiIdActivity";
    private AccountAuthService mAuthManager;
    private AccountAuthParams mAuthParam;
    HuaweiIdAuthButton huaweiLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.edit_text_login_email);
        editTextPassword = findViewById(R.id.edit_text_login_password);
        btnLogin = findViewById(R.id.btn_login);
        checkBoxShowPassword = findViewById(R.id.checkbox_show_password);
        checkBoxRemember = findViewById(R.id.checkbox_remember);
        textViewSignUpNow = findViewById(R.id.text_view_signup_now);
        huaweiLogin = findViewById(R.id.btn_huawei_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(LoginActivity.this, view);
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                if (email.trim().equals("") || password.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter data in all fields!", Toast.LENGTH_LONG).show();
                } else {
                    mAuth.signInWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        DocumentReference userRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());
                                        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                LoginActivity.USER = documentSnapshot.toObject(User.class);
                                                if (USER.getUserType().equals("patient")) {
                                                    startActivity(new Intent(LoginActivity.this, PatientDashboard.class));
                                                } else if (USER.getUserType().equals("admin")) {
                                                    startActivity(new Intent(LoginActivity.this, AdminDashboard.class));
                                                }
                                            }
                                        });
                                        //finish();
                                    } else {
                                        USER = null;
                                        Snackbar.make(findViewById(R.id.linear_layout_login),
                                                task.getException().getMessage(),
                                                BaseTransientBottomBar.LENGTH_SHORT)
                                                .setAction("close", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        editTextEmail.setText("");
                                                        editTextPassword.setText("");
                                                        editTextEmail.requestFocus();
                                                    }
                                                }).show();
                                    }
                                }
                            });
                }
            }
        });

        textViewSignUpNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, HomeSignUp.class));
            }
        });

        huaweiLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                huaweiSignIn();
            }
        });

        getLoginDetails();
    }

    public void saveLoginDetails() {
        sharedPreferences = getSharedPreferences("loginData", MODE_PRIVATE);
        String username = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        boolean remember = checkBoxRemember.isChecked();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("remember", remember);
        editor.apply();
    }

    public void clearLoginDetails() {
        sharedPreferences = getSharedPreferences("loginData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", "");
        editor.putString("password", "");
        editor.putBoolean("remember", false);
        editor.apply();
    }

    public void getLoginDetails() {
        try {
            sharedPreferences = getSharedPreferences("loginData", MODE_PRIVATE);
            String username = sharedPreferences.getString("username", "");
            String password = sharedPreferences.getString("password", "");
            boolean remember = sharedPreferences.getBoolean("remember", false);
            editTextEmail.setText(username);
            editTextPassword.setText(password);
            checkBoxRemember.setChecked(remember);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void showPasswordClick(View view) {
        if (checkBoxShowPassword.isChecked()) {
            editTextPassword.setTransformationMethod(null);
        } else {
            editTextPassword.setTransformationMethod(new PasswordTransformationMethod());
        }
    }

    public void rememberClick(View view) {
        if (checkBoxRemember.isChecked()) {
            saveLoginDetails();
        } else {
            clearLoginDetails();
        }
    }

    private void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    //Huawei Login
    private void huaweiSignIn() {
        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
//                .setAccessToken()
                .createParams();
        mAuthManager = AccountAuthManager.getService(LoginActivity.this, mAuthParam);
        startActivityForResult(mAuthManager.getSignInIntent(), 8888);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Process the authorization result and obtain an ID to**AuthAccount**thAccount.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8888) {
            com.huawei.hmf.tasks.Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                // The sign-in is successful, and the user's ID information and ID token are obtained.
                AuthAccount authAccount = authAccountTask.getResult();
                Log.i(TAG, "idToken:" + authAccount.getIdToken());
            } else {
                // The sign-in failed. No processing is required. Logs are recorded for fault locating.
                Log.e(TAG, "sign in failed : " +((ApiException) authAccountTask.getException()).getStatusCode());
            }
        }
    }


}