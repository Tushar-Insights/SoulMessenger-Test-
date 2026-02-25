package com.fromtushar.soulmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    EditText email,password;
    Button button;
    TextView logsignup;
    FirebaseAuth auth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        email=findViewById(R.id.editTextLogEmail);
        password=findViewById(R.id.editTextLogPassword);
        button=findViewById(R.id.Logbutton);
        logsignup=findViewById(R.id.logsignup);

        dialog=new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);

        logsignup.setOnClickListener(v -> {
            startActivity(new Intent(this,
                    registration.class));
            finish();
        });

        button.setOnClickListener(v -> loginUser());
    }

    private void loginUser(){

        String Email=email.getText().toString().trim();
        String Pass=password.getText().toString().trim();

        if(TextUtils.isEmpty(Email)){
            email.setError("Enter Email");
            return;
        }

        if(TextUtils.isEmpty(Pass)){
            password.setError("Enter Password");
            return;
        }

        dialog.show();

        auth.signInWithEmailAndPassword(Email,Pass)
                .addOnCompleteListener(task -> {

                    dialog.dismiss();

                    if(task.isSuccessful()){

                        Toast.makeText(this,
                                "Login Successful",
                                Toast.LENGTH_SHORT).show();

                        startActivity(
                                new Intent(this,
                                        MainActivity.class));
                        finish();

                    }else{
                        Toast.makeText(this,
                                "Invalid Email or Password",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
