package com.proyecto.kidfun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class registro extends AppCompatActivity {
    private EditText userEmail, userPassword, userNumero , userName, userVPassword;
    private Button btnLoad;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        Intent intent=getIntent();
        userName = findViewById(R.id.proName);
        userEmail = findViewById(R.id.proEmail);
        userPassword = findViewById(R.id.regPassword);
        userVPassword = findViewById(R.id.regVPassword);
        userNumero = findViewById(R.id.proTelefonico);
        btnLoad = findViewById(R.id.probtnSend);

        if(intent.getStringExtra("Plataforma").equals("Yes")){
            userName.setEnabled(false);
            userEmail.setEnabled(false);
            userPassword.setEnabled(false);
            userVPassword.setEnabled(false);
            userName.setText(intent.getStringExtra("Name"));
            userEmail.setText(intent.getStringExtra("Email"));
            userPassword.setText("NOSEPEROSEVERIAPRO");
            userVPassword.setText("NOSEPEROSEVERIAPRO");
            //ImgUser.setEnabled(false);
            Log.e("Facebook", "A ver A ver:" + intent.getStringExtra("ID"));
            //Glide.with(getApplicationContext()).load(intent.getStringExtra("Foto")).into(ImgUser);
        }

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLoad.setVisibility(View.INVISIBLE);
                final String email = userEmail.getText().toString();
                final String password = userPassword.getText().toString();
                final String VPassword = userVPassword.getText().toString();
                final String name = userName.getText().toString();
                final String telefono = userNumero.getText().toString();
                if(intent.getStringExtra("Plataforma").equals("Yes")){
                    if(!telefono.isEmpty()){
                        RegistroPorPlataforma(name, email, telefono);
                    }
                    else{
                        //showAlerterError("Algo falta", "Algo falta verificalo");
                        btnLoad.setVisibility(View.VISIBLE);

                    }

                }
                else{
                    if(email.isEmpty() || password.isEmpty() || VPassword.isEmpty() || name.isEmpty() || telefono.isEmpty()){
                        Toast.makeText(registro.this, "Falta algo Checa tus datos", Toast.LENGTH_SHORT).show();
                        btnLoad.setVisibility(View.VISIBLE);
                    }
                    else{
                        if(password.equals(VPassword)){
                            CrearUsuario(email, name, password);
                        }
                        else{
                            userPassword.setText("");
                            userVPassword.setText("");
                            Toast.makeText(registro.this, "Las constraseña no son las mismas", Toast.LENGTH_SHORT).show();
                            btnLoad.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

    }

    private void RegistroPorPlataforma(String name, String email, String telefono) {
        DocumentReference almacen = db.collection("Usuarios").document(mAuth.getCurrentUser().getUid());
        Map<String, Object> userData = new HashMap<>();
        userData.put("UserName", name);
        userData.put("Email", email);
        userData.put("Telefono", userNumero.getText().toString());
        almacen.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                UpdateData(name, mAuth.getCurrentUser());
                Log.d("Registro", "onSuccess: Usuario Creado" + mAuth.getCurrentUser().getUid());
                /*Intent GoHome = new Intent(getApplicationContext(), HomeActivity.class);
                GoHome.putExtra("telefono", userNumero.getText().toString());
                GoHome.putExtra("CualEs", getIntent().getStringExtra("Cual"));
                startActivity(GoHome);

                 */
            }
        });

    }


    private void CrearUsuario(String email, String name, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
              if (task.isSuccessful()){
                  DocumentReference almacen = db.collection("Usuarios").document(mAuth.getCurrentUser().getUid());
                  Map<String, Object> userData = new HashMap<>();
                  userData.put("UserName", name);
                  userData.put("Email", email);
                  userData.put("Telefono", userNumero.getText().toString());
                  almacen.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void aVoid) {
                          UpdateData(name, mAuth.getCurrentUser());
                          Log.d("Registro", "onSuccess: Usuario Creado" + mAuth.getCurrentUser().getUid());
                      }
                  });
              }
              else{
                  Toast.makeText(registro.this, "Algo salio mal", Toast.LENGTH_SHORT).show();
              }
            }
        });
    }

    private void UpdateData(String name, FirebaseUser currentUser) {
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        currentUser.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    updateUI();
                }
            }
        });
    }

    private void updateUI() {
        Intent GoHome = new Intent(registro.this, HomeActivity.class);
        GoHome.putExtra("telefono", userNumero.getText().toString());
        GoHome.putExtra("email", userEmail.getText().toString());
        GoHome.putExtra("password", userVPassword.getText().toString());
        GoHome.putExtra("CualEs", "password");
        startActivity(GoHome);
        finish();
    }
}