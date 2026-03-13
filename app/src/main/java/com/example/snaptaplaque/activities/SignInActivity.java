package com.example.snaptaplaque.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.api.account.LoginRequest;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.utils.FeedbackManager;
import com.example.snaptaplaque.utils.SessionManager;


public class SignInActivity extends Activity {

    private EditText username, password;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        sessionManager = new SessionManager(this);

        username = findViewById(R.id.inputIdentifiant);
        password = findViewById(R.id.inputMotDePasse);
        Button signIn = findViewById(R.id.buttonConnexion);
        Button signUp = findViewById(R.id.buttonInscription);

        signIn.setOnClickListener(v -> login());
        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String _username = username.getText().toString().trim();
        String _password = password.getText().toString().trim();

        LoginRequest loginRequest = new LoginRequest(_username, _password);

        if (_username.isEmpty() || _password.isEmpty()) {
            FeedbackManager.showError(this, getString(R.string.field_required), null);
            return;
        }

        AccountCall.login(this, loginRequest, sessionManager);
    }
}