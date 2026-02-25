package com.fromtushar.soulmessenger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    RecyclerView recyclerView;
    UserAdpter adapter;
    FirebaseDatabase database;

    ArrayList<Users> usersList;
    ArrayList<Users> backupList;

    EditText searchUser;
    ImageView logout;

    // Bottom Navigation Icons
    ImageView cameraBtn, settingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Initialize Views
        recyclerView = findViewById(R.id.mainUserRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchUser = findViewById(R.id.searchUser);
        logout = findViewById(R.id.logoutimg);

        // Bottom bar buttons (Make sure these IDs match your XML)
        cameraBtn = findViewById(R.id.camBut);
        settingBtn = findViewById(R.id.settingBut);

        usersList = new ArrayList<>();
        backupList = new ArrayList<>();

        adapter = new UserAdpter(this, usersList);
        recyclerView.setAdapter(adapter);

        loadChatUsers();   // Load initial chat list



        // 1. Settings button click
        settingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, setting.class);
            startActivity(intent);
        });

        // 2. Camera button click
        cameraBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 101);
            } catch (Exception e) {
                Toast.makeText(this, "Camera open nahi ho raha", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Logout click
        logout.setOnClickListener(v -> showLogoutDialog());

        // 4. Search logic
        searchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s){}
            @Override
            public void onTextChanged(CharSequence s,int a,int b,int c){
                searchUsers(s.toString());
            }
        });
    }

    private void loadChatUsers(){
        if(auth.getUid() == null) return;

        DatabaseReference ref = database.getReference()
                .child("ChatList")
                .child(auth.getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                backupList.clear();
                for(DataSnapshot snap : snapshot.getChildren()){
                    String uid = snap.getKey();
                    database.getReference().child("User").child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Users user = snapshot.getValue(Users.class);
                                    if(user!=null){
                                        user.setUserId(uid);
                                        usersList.add(user);
                                        backupList.add(user);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                @Override public void onCancelled(@NonNull DatabaseError error){}
                            });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error){}
        });
    }

    private void searchUsers(String text){
        DatabaseReference ref = database.getReference().child("User");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot snap : snapshot.getChildren()){
                    Users user = snap.getValue(Users.class);
                    if(user == null) continue;
                    user.setUserId(snap.getKey());
                    if(user.getUserId().equals(auth.getUid())) continue;

                    if(user.getUserName().toLowerCase().contains(text.toLowerCase())
                            || user.getMail().toLowerCase().contains(text.toLowerCase())) {
                        usersList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error){}
        });
    }

    private void showLogoutDialog(){
        Dialog dialog = new Dialog(this, R.style.dialoge);
        dialog.setContentView(R.layout.dialog_layout);

        Button yes = dialog.findViewById(R.id.yesbtn);
        Button no  = dialog.findViewById(R.id.nobtn);

        yes.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(MainActivity.this, login.class));
            finish();
        });

        no.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}