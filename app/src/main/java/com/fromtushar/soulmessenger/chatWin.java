package com.fromtushar.soulmessenger;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatWin extends AppCompatActivity {

    String reciverimg, reciverUid, reciverName, SenderUID;
    CircleImageView profile;
    TextView reciverNName;
    ImageView sendbtn;
    EditText textmsg;

    FirebaseAuth firebase;
    FirebaseDatabase database;

    // 🔥 Inhe static rakhne ki zaroorat nahi, hum seedhe adapter ko denge
    String sImage;
    String rImage;

    String senderRoom, reciverRoom;

    RecyclerView mmessangesAdpter;
    ArrayList<msgModelclass> messagessArraylist;
    messagesAdpter messagesAdpter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_win);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        database = FirebaseDatabase.getInstance();
        firebase = FirebaseAuth.getInstance();

        // Intent data
        reciverName = getIntent().getStringExtra("nameee");
        reciverimg = getIntent().getStringExtra("reciverImg");
        reciverUid = getIntent().getStringExtra("uid");

        rImage = reciverimg; // Receiver ki image yahan mil gayi

        // Views initialization
        mmessangesAdpter = findViewById(R.id.msgadpter);
        textmsg = findViewById(R.id.textmsg);
        sendbtn = findViewById(R.id.sendbtn);
        profile = findViewById(R.id.profileimgg);
        reciverNName = findViewById(R.id.recivername);

        SenderUID = firebase.getUid();
        senderRoom = SenderUID + reciverUid;
        reciverRoom = reciverUid + SenderUID;

        // Header set karein
        reciverNName.setText(reciverName);
        Picasso.get().load(reciverimg).placeholder(R.drawable.avtar_dp).into(profile);

        messagessArraylist = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mmessangesAdpter.setLayoutManager(layoutManager);

        // 🔥 STEP 1: Pehle apni (Sender) image fetch karein Firebase se
        DatabaseReference userRef = database.getReference().child("User").child(SenderUID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    sImage = snapshot.child("profilepic").getValue().toString();

                    // 🔥 STEP 2: Jab sender image mil jaye, tabhi adapter set karein
                    messagesAdpter = new messagesAdpter(chatWin.this, messagessArraylist, sImage, rImage);
                    mmessangesAdpter.setAdapter(messagesAdpter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Messages fetch logic
        DatabaseReference chatRef = database.getReference().child("chats").child(senderRoom).child("messages");
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagessArraylist.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    msgModelclass msg = snap.getValue(msgModelclass.class);
                    messagessArraylist.add(msg);
                }
                if (messagesAdpter != null) {
                    messagesAdpter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Send Button
        sendbtn.setOnClickListener(view -> {
            String message = textmsg.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(chatWin.this, "Enter Message", Toast.LENGTH_SHORT).show();
                return;
            }
            textmsg.setText("");

            msgModelclass msg = new msgModelclass(message, SenderUID, new Date().getTime());

            database.getReference().child("chats").child(senderRoom).child("messages").push().setValue(msg)
                    .addOnCompleteListener(task -> {
                        database.getReference().child("chats").child(reciverRoom).child("messages").push().setValue(msg);
                    });

            // ChatList update
            DatabaseReference chatList = database.getReference().child("ChatList");
            chatList.child(SenderUID).child(reciverUid).setValue(true);
            chatList.child(reciverUid).child(SenderUID).setValue(true);
        });
    }
}