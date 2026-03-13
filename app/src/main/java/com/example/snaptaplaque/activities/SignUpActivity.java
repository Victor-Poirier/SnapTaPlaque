package com.example.snaptaplaque.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;


import androidx.appcompat.widget.SwitchCompat;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.api.account.RegisterRequest;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.utils.FeedbackManager;
import com.example.snaptaplaque.utils.SessionManager;

public class SignUpActivity extends Activity {

    private EditText username, fullName, mail, password;

    SessionManager sessionManager;

    private SwitchCompat rgpd;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        username = findViewById(R.id.inputIdentifiant);
        fullName = findViewById(R.id.inputFullName);
        mail = findViewById(R.id.inputMail);
        password = findViewById(R.id.inputMotDePasse);
        rgpd = findViewById(R.id.buttonRGPD);
        Button signIn = findViewById(R.id.buttonConnexion);
        Button signUp = findViewById(R.id.buttonInscription);

        signUp.setOnClickListener(v -> register());
        signIn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        });

        sessionManager = new SessionManager(this);

    }

    private void register() {
        String _fullName = fullName.getText().toString().trim();
        String _email = mail.getText().toString().trim();
        String _motDePasse = password.getText().toString().trim();
        // On supprime tous les caractères qui ne sont pas alphanumériques
        String _identifiant = username.getText().toString().trim().replaceAll("[^a-zA-Z0-9]", "");
        boolean admin = false;
        boolean _consent = rgpd.isChecked();

        if (_identifiant.isEmpty() || _email.isEmpty() || _motDePasse.isEmpty() || _fullName.isEmpty()
            || !_consent) {
            FeedbackManager.showError(this, getString(R.string.field_required), null);
            return;
        }

        RegisterRequest registerRequest = new RegisterRequest(_email, _identifiant, _motDePasse, _fullName, admin, _consent);

        AccountCall.register(this, registerRequest, sessionManager);
    }
}