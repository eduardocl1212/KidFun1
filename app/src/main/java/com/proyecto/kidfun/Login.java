package com.proyecto.kidfun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.TwitterAuthCredential;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class Login extends AppCompatActivity {

    private FloatingActionButton FaceLog,GoogleLog;
    private TwitterLoginButton TwitterLog;
    private EditText userEmail, userPassword;
    private Button btnSend;
    private ProgressBar pgrLogin;
    private TextView NoTengo;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Intent HomeActivity, Registros;
    private FirebaseFirestore db;
    private final static  int RC_SIGN_IN = 83;
    private CallbackManager callbackManager;
    private boolean twittertt = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        /*
        TwitterAuthConfig mTwitterAuthConfig = new TwitterAuthConfig(getString(R.string.ApiKey),
                getString(R.string.ApiKeySecret));
        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .twitterAuthConfig(mTwitterAuthConfig)
                .build();
        Twitter.initialize(twitterConfig);


         */
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        userEmail = findViewById(R.id.logEmail);
        userPassword = findViewById(R.id.logPassword);
        HomeActivity = new Intent(this, HomeActivity.class);
        Registros = new Intent(this, registro.class);
        btnSend = findViewById(R.id.probtnSend);
        db = FirebaseFirestore.getInstance();
        NoTengo = findViewById(R.id.btnNotengo);
        FaceLog = findViewById(R.id.fab_fb);
        GoogleLog = findViewById(R.id.fab_google);
        //TwitterLog = findViewById(R.id.fab_twitter);
        callbackManager = CallbackManager.Factory.create();
        createRequest();

        pgrLogin = findViewById(R.id.pgrLogin);

        //pgrLogin.setVisibility(View.INVISIBLE);

        /*
        TwitterLog.setEnabled(true);

        TwitterLog.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.e("Twitter", "Paso por aqui");
                twPro(result.data);

            }

            @Override
            public void failure(TwitterException exception) {

            }
        });

         */

        GoogleLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        printKeyHash();
        FaceLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Facebook", "Si estoy aqui");
                LoginManager.getInstance().logInWithReadPermissions(Login.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.e("Facebook", "Si estoy aqui2");
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.e("Facebook", "Cancelado");

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.e("Facebook", "Error");

                    }
                });
            }
        });


        NoTengo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Registros.putExtra("Plataforma","NO");
                startActivity(Registros);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSend.setVisibility(View.INVISIBLE);
                pgrLogin.setVisibility(View.VISIBLE);
                final String mail = userEmail.getText().toString();
                final String pass = userPassword.getText().toString();

                if (mail.isEmpty() || pass.isEmpty()){
                    Toast.makeText(Login.this, "Falta algo", Toast.LENGTH_SHORT).show();
                    btnSend.setVisibility(View.VISIBLE);
                    pgrLogin.setVisibility(View.INVISIBLE);
                }
                else{
                    EntrarSistema(mail,pass);

                }
            }
        });

    }

    private void twPro(TwitterSession session) {
        AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token,session.getAuthToken().secret);
        mAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.d("Twitter.com", "signInWithCredential:success");
                FirebaseUser user = mAuth.getCurrentUser();
                twittertt = true;
                updateUI();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(twittertt) {
            TwitterLog.onActivityResult(requestCode, resultCode, data);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("GOOGLE.COM", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("GOOGLE.COM", "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("GOOGLE.COM", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("GOOGLE.COM", "signInWithCredential:failure", task.getException());
                            updateUI();
                        }

                        // ...
                    }
                });
    }

    /*
    ######

    VERIFICAR QUE EL CODIGO COMPILADO SEA EL MISMO REGISTRADO EN FACEBOOK
    ESTE CODIO LO GENERARA


    ######
     */
    private void printKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Log.e("KeyHash:", e.toString());
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e("Facebook", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e("Facebook", "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "La cuenta parece estar vinculada con otro proveedor",
                                    Toast.LENGTH_SHORT).show();
                            updateUI();
                        }

                        // ...
                    }
                });
    }

    private void EntrarSistema(String mail, String pass) {
        mAuth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    btnSend.setVisibility(View.VISIBLE);
                    pgrLogin.setVisibility(View.INVISIBLE);
                    updateUI();
                } else {
                    btnSend.setVisibility(View.VISIBLE);
                    pgrLogin.setVisibility(View.INVISIBLE);
                    //MostrarMensaje("No hemos podido encontrar tu cuenta verifica tus datos");
                    Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            updateUI();
        }

    }

    private void updateUI() {
        Log.e("Error", "Si puedes verme");
        FirebaseUser verificado = mAuth.getCurrentUser();
        String Proveedor[] = new String[5];
        Proveedor[2] = "na";
        String Nombre = "";
        String Email = "";
        String Foto = "";
        String ID = "";
        //String Telefono= "";
        int i = 0;
        if(verificado != null){
            for(UserInfo profile: verificado.getProviderData()) {
                Log.e("LOGIN", "Con que chingados inicias: " + profile.getProviderId());

                Log.e("CUENTAME", "Estamos en: " + i);
                Proveedor[i] = profile.getProviderId();
                Log.e("CUENTAME", "Estamos en: " + i + "Proveedor " + Proveedor[i]);
                //Log.e("Facebook", "A ver tu ID: " + profile.getUid());
                //Log.e("Facebook", "Como se llama tu nombre: " + profile.getDisplayName());
                Nombre = profile.getDisplayName();
                //Log.e("Facebook", "Email: " + profile.getEmail());
                Email = profile.getEmail();
                //Log.e("Facebook", "Foto: " + profile.getPhotoUrl());
                Foto = String.valueOf(profile.getPhotoUrl());
                i ++;

                //Log.e("Facebook", "Prodria Existir=: " + profile.getPhoneNumber());
            }
            if(Proveedor[2].equals("password") || Proveedor[1].equals("password")){

                boolean data = true;
                if (data) {
                    finish();
                    startActivity(HomeActivity);

                } else {
                    mAuth.signOut();
                    userPassword.setText("");

                }
            }
            else if(Proveedor[1].equals("facebook.com") || Proveedor[1].equals("google.com") || Proveedor[1].equals("twitter.com")){
                DocumentReference docIdRef = db.collection("Usuarios").document(mAuth.getCurrentUser().getUid().toString());
                String finalNombre = Nombre;
                String finalEmail = Email;
                String finalFoto = Foto;
                String finalID = mAuth.getCurrentUser().getUid();
                String finalProveedor = Proveedor[2];
                docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                //¿SI EXISTE ES PORQUE YA PASO EL REGISTRO NO?
                                finish();
                                startActivity(HomeActivity);
                            } else {
                                Log.d("EXISTES?", "No Existe");
                                Intent registro = new Intent(getApplicationContext(), registro.class);
                                registro.putExtra("Name", finalNombre);
                                registro.putExtra("Email", finalEmail);
                                registro.putExtra("Foto", finalFoto + "?type=large");
                                registro.putExtra("ID", finalID);
                                registro.putExtra("Plataforma", "Yes");
                                registro.putExtra("Cual", finalProveedor);
                                startActivity(registro);
                            }
                        } else {

                        }
                    }
                });
                //showAlerterSuccess("LOGIN","Entraste con " + Proveedor);
                Toast.makeText(this, Proveedor[1]+": Espere mientras buscamos la cuenta..", Toast.LENGTH_SHORT).show();
                //mAuth.signOut();
            }
            else{

                mAuth.signOut();
            }
        }
        else{
            Log.w("Facebook", "NI MADRES");
        }

        /*
        boolean data = verificado.isEmailVerified();
            if (data) {
                startActivity(HomeActivity);
                //mixpanel.flush();
                finish();
            } else {
                AlertDialog.Builder dialogo2 = new AlertDialog.Builder(LoginActivity.this);
                dialogo2.setTitle("Cuenta no verificada");
                dialogo2.setMessage("Revisa tu correo o correo no deseado lo enviamos a: " + mAuth.getCurrentUser().getEmail());
                dialogo2.setCancelable(false);
                dialogo2.setPositiveButton("Vale", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo2, int id) {
                        dialogo2.dismiss();
                    }
                });
                dialogo2.show();
                mAuth.signOut();
                userPassword.setText("");
            }
         */
    }




}