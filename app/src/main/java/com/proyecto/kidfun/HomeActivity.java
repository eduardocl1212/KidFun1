package com.proyecto.kidfun;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private Button mSalir;
    private FirebaseAuth mAuth;
    private TextView lblEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        lblEmail = findViewById(R.id.lblName);
        mSalir = findViewById(R.id.btnSalir);
        mSalir.setText("Salir");

        lblEmail.setText("Email: " + mAuth.getCurrentUser().getEmail() + "\r\nNombre: " + mAuth.getCurrentUser().getDisplayName());

        mSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
                Intent login = new Intent(getApplicationContext(), Login.class);
                startActivity(login);
            }
        });
    }
}