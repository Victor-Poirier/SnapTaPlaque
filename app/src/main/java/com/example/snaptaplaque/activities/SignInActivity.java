package com.example.snaptaplaque.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.snaptaplaque.R;

public class SignInActivity extends Activity {

    private EditText username, password;

    private Button signUp, signIn;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        username = findViewById(R.id.inputIdentifiant);
        password = findViewById(R.id.inputMotDePasse);
        signIn = findViewById(R.id.buttonConnexion);
        signUp = findViewById(R.id.buttonInscription);

        signIn.setOnClickListener(v -> login());
        signUp.setOnClickListener( v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });

    }

    private void login() {

        String _username = username.getText().toString().trim();
        String _password = password.getText().toString().trim();

        if (_username.isEmpty() || _password.isEmpty()) {
            FeedbackManager.showError(this, "All fields are required", null);
            return;
        }

        apiService.login(_username, _password)
                .enqueue(new Callback<LoginResponse>() {

                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            String token = response.body().getAccessToken();

                            SessionManager session = new SessionManager(LoginActivity.this);
                            session.saveSession(token, role, username);

                            redirectUser(role);

                        } else {
                            FeedbackManager.showError(LoginActivity.this, "Invalid credentials", null);
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        FeedbackManager.showError(LoginActivity.this, "Connection error", null);
                    }
                });
    }
}
