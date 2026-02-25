package com.fromtushar.soulmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class registration extends AppCompatActivity {

    TextView loginbut;
    EditText rg_username, rg_email, rg_password, rg_conpassword;
    Button rg_signup;
    CircleImageView rg_profileImag;

    FirebaseAuth auth;
    FirebaseDatabase database;

    Uri imageURI;
    ProgressDialog progressDialog;

    private static final String TAG = "SOUL_MSG"; //  Filter ke liye Tag

    String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "df3awnwrv");
            config.put("api_key", "343655828831438");


            MediaManager.init(this, config);
            Log.d(TAG, "Cloudinary Initialized");
        } catch (Exception e) {
            Log.d(TAG, "Cloudinary already init or failed: " + e.getMessage());
        }

        loginbut = findViewById(R.id.loginbutton);
        rg_username = findViewById(R.id.rgusername);
        rg_email = findViewById(R.id.rgEmail);
        rg_password = findViewById(R.id.rgpassword);
        rg_conpassword = findViewById(R.id.rgconpassword);
        rg_profileImag = findViewById(R.id.profilerg0);
        rg_signup = findViewById(R.id.signupbutton);

        loginbut.setOnClickListener(v -> {
            startActivity(new Intent(this, login.class));
            finish();
        });

        rg_profileImag.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(i, 10);
        });

        rg_signup.setOnClickListener(v -> signupUser());
    }

    private void signupUser() {
        String name = rg_username.getText().toString().trim();
        String email = rg_email.getText().toString().trim();
        String pass = rg_password.getText().toString().trim();
        String cpass = rg_conpassword.getText().toString().trim();
        String status = "Hey I'm Using This Application";

        if (imageURI == null) {
            Toast.makeText(this, "Select Profile Image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(cpass)) {
            Toast.makeText(this, "Enter Valid Info", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.matches(emailPattern)) {
            rg_email.setError("Valid Email Required");
            return;
        }

        if (pass.length() < 6) {
            rg_password.setError("Min 6 Characters");
            return;
        }

        if (!pass.equals(cpass)) {
            rg_conpassword.setError("Password Not Match");
            return;
        }

        progressDialog.show();
        Log.d(TAG, "Signup started... Attempting Cloudinary Upload");

        MediaManager.get()
                .upload(imageURI)
                .unsigned("ml_default")   // ← ye line add karo
                .option("resource_type", "auto")
                .callback(new UploadCallback() {

                    @Override public void onStart(String requestId) {
                        Log.d(TAG, "Cloudinary: Upload started");
                    }

                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        Log.d(TAG, "Cloudinary: Success! URL: " + imageUrl);
                        createFirebaseUser(name, email, pass, status, imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Log.e(TAG, "Cloudinary: Error -> " + error.getDescription());
                        Toast.makeText(registration.this, "Image Error: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void createFirebaseUser(String name, String email, String password, String status, String imageUrl) {
        Log.d(TAG, "Firebase: Attempting Auth for " + email);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String id = task.getResult().getUser().getUid();
                        Log.d(TAG, "Firebase Auth: Success! UID: " + id);

                        DatabaseReference ref = database.getReference().child("User").child(id);

                        Users user = new Users();
                        user.setUserId(id);
                        user.setUserName(name);
                        user.setMail(email);
                        user.setPassword(password);
                        user.setStatus(status);
                        user.setProfilepic(imageUrl);

                        Log.d(TAG, "Firebase DB: Saving user data...");
                        ref.setValue(user).addOnCompleteListener(t -> {
                            progressDialog.dismiss();
                            if (t.isSuccessful()) {
                                Log.d(TAG, "Firebase DB: Success!");
                                Toast.makeText(registration.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(registration.this, MainActivity.class));
                                finish();
                            } else {
                                Log.e(TAG, "Firebase DB: Failed -> " + t.getException().getMessage());
                                Toast.makeText(registration.this, "Database Error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Log.e(TAG, "Firebase Auth: Failed -> " + task.getException().getMessage());
                        Toast.makeText(registration.this, "Auth Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageURI = data.getData();
            rg_profileImag.setImageURI(imageURI);
            Log.d(TAG, "Image selected successfully");
        }
    }
}