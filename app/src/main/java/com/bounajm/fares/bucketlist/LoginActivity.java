package com.bounajm.fares.bucketlist;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    public static FirebaseAuth mAuth;

    public static FirebaseDatabase database;

    public static DatabaseReference connectedRef;

    public static DatabaseReference myRef;

    public static boolean connected;

    private static final String TAG = "EmailPassword";

    private EditText emailET, passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getDatabase();

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                connected = snapshot.getValue(Boolean.class);
                setOnline();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            afterLogin();
        }

        emailET = (EditText) findViewById(R.id.et_email);
        passwordET = (EditText) findViewById(R.id.et_password);
    }

    public static boolean isLoggedin(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            return true;
        }else{
            return false;
        }
    }

    public static String userID(){
        if(isLoggedin()){
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }else{
            return "";
        }
    }

    public static void setOnline(){
        if(isLoggedin()){
            Query query = myRef.child(userID()).child("bucketItem");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                            if(!issue.child("isOnline").getValue(boolean.class) && connected){
                                issue.child("isOnline").getRef().setValue(true);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    private void afterLogin(){
        setOnline();
        startActivity(new Intent(LoginActivity.this, ListAndHistoryActivity.class));
        finish();
    }

    public void getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }
        if(connectedRef == null){
            connectedRef = database.getReference(".info/connected");
        }
        if(myRef == null){
            myRef = database.getReference();
        }
    }

    private void createAccount(String email, String password) {

        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            afterLogin();
                        } else {
                            Toast.makeText(LoginActivity.this, "Account already exists", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn(String email, String password) {

        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Success: " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            afterLogin();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication failed!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        if(!connected){
            Toast.makeText(LoginActivity.this, "Unable to connect, please check internet.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        String email = emailET.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailET.setError("Required.");
            valid = false;
        } else {
            emailET.setError(null);
        }

        String password = passwordET.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordET.setError("Required.");
            valid = false;
        }else if (password.length() < 6){
            passwordET.setError("minimum characters: 6");
            valid = false;
        }else{
            passwordET.setError(null);
        }
        return valid;
    }

    public void signUp(View view){

        createAccount(emailET.getText().toString(), passwordET.getText().toString());

    }

    public void login(View view){

        signIn(emailET.getText().toString(), passwordET.getText().toString());

    }

    private boolean validateFormResetPassword() {
        boolean valid = true;

        if(!connected){
            Toast.makeText(LoginActivity.this, "Unable to connect, please check internet.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        String email = emailET.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailET.setError("Required.");
            valid = false;
        } else {
            emailET.setError(null);
        }
        return valid;
    }

    public void resetPassword(View view){

        if (!validateFormResetPassword()) {
            return;
        }

        mAuth.sendPasswordResetEmail(emailET.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(LoginActivity.this, "A link to reset your password has been sent to your email.",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
}





