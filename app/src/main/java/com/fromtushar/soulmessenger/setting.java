package com.fromtushar.soulmessenger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class setting extends AppCompatActivity {

    ImageView setprofile;
    EditText setname, setstatus;
    Button donebut;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri setImageUri;
    String email, password, profilePicUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        setprofile = findViewById(R.id.settingprofile);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        donebut = findViewById(R.id.donebut);

        if (auth.getUid() == null) return;

        // 🔥 Dhyaan dein: Yahan "User" hona chahiye kyunki aapne Firebase mein isi naam se save kiya hai
        DatabaseReference reference = database.getReference().child("User").child(auth.getUid());
        StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

        // 1. Current User Data Fetch Karein (Jo login hai uska name/status dikhayega)
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("userName").exists() ? snapshot.child("userName").getValue().toString() : "";
                    String status = snapshot.child("status").exists() ? snapshot.child("status").getValue().toString() : "";
                    profilePicUrl = snapshot.child("profilepic").exists() ? snapshot.child("profilepic").getValue().toString() : "";

                    // Ye aapka purana data set kar dega
                    setname.setText(name);
                    setstatus.setText(status);

                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Picasso.get().load(profilePicUrl).placeholder(R.drawable.avtar_dp).into(setprofile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(setting.this, "Data fetch failed", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Profile Image Change karne ke liye
        setprofile.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        });

        // 3. Update Button Logic (Click karne par name/status update ho jayega)
        donebut.setOnClickListener(view -> {
            String name = setname.getText().toString();
            String status = setstatus.getText().toString();

            if (name.isEmpty()) {
                Toast.makeText(setting.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(setting.this, "Updating Profile...", Toast.LENGTH_SHORT).show();

            if (setImageUri != null) {
                // Agar nayi photo select ki hai toh pehle storage mein upload hogi
                storageReference.putFile(setImageUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            updateToDatabase(name, status, uri.toString(), reference);
                        });
                    }
                });
            } else {
                // Bina photo change kiye sirf text update
                updateToDatabase(name, status, profilePicUrl, reference);
            }
        });
    }

    private void updateToDatabase(String name, String status, String image, DatabaseReference reference) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userName", name);
        map.put("status", status);
        map.put("profilepic", image);

        reference.updateChildren(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(setting.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                // Update ke baad MainActivity par wapas le jayega
                Intent intent = new Intent(setting.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(setting.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            setImageUri = data.getData();
            setprofile.setImageURI(setImageUri);
        }
    }
}