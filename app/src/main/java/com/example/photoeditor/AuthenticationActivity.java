package com.example.photoeditor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthenticationActivity extends AppCompatActivity {
    EditText mEmail, mPassword;
    Button mBtSignIn, mBtSignUp;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        mAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.etLogin);
        mPassword = findViewById(R.id.etPassword);
        mBtSignIn = findViewById(R.id.btSignIn);
        mBtSignUp = findViewById(R.id.btSignUp);
        progressBar = findViewById(R.id.progressBar);

        mBtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();

        }

        mBtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email jest wymagany");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Hasło jest wymagane");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Hasło musi posiadać więcej niż 6 znaków");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AuthenticationActivity.this, R.string.RegistrationSuccessful, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            //Toast.makeText(AuthenticationActivity.this, R.string.RegistrationFailed, Toast.LENGTH_LONG).show();
                            Toast.makeText(AuthenticationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        mBtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email jest wymagany");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Hasło jest wymagane");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Hasło musi posiadać więcej niż 6 znaków");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AuthenticationActivity.this, R.string.LoginSuccessful, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            //Toast.makeText(AuthenticationActivity.this, R.string.RegistrationFailed, Toast.LENGTH_LONG).show();
                            Toast.makeText(AuthenticationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

}
