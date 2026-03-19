package com.example.snaptaplaque.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;


import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.fragments.PrivacyPolicyDialogFragment;
import com.example.snaptaplaque.models.api.account.RegisterRequest;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.utils.FeedbackManager;

public class SignUpActivity extends BaseActivity {

    private EditText username, fullName, mail, password;

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

        TextView privacyPolicyLink = findViewById(R.id.privacyPolicyLink);
        privacyPolicyLink.setOnClickListener(v -> {
            DialogFragment privacyDialog = new PrivacyPolicyDialogFragment();
            privacyDialog.show(getSupportFragmentManager(), "PrivacyPolicyDialog");
        });

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

        AccountCall.register(this, registerRequest);
    }
}