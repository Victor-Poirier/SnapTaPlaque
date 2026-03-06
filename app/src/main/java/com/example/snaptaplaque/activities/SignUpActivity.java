package com.example.snaptaplaque.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.api.RegisterRequest;
import com.example.snaptaplaque.models.api.RegisterResponse;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.utils.FeedbackManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends Activity {

    private EditText username, fullName, mail, password;

    private Button rgpd, signIn, signUp;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        username = findViewById(R.id.inputIdentifiant);
        fullName = findViewById(R.id.inputFullName);
        mail = findViewById(R.id.inputMail);
        password = findViewById(R.id.inputMotDePasse);
        rgpd = findViewById(R.id.buttonRGPD);
        signIn = findViewById(R.id.buttonConnexion);
        signUp = findViewById(R.id.buttonInscription);

        signUp.setOnClickListener(v -> register());
        signIn.setOnClickListener( v -> {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        });

    }

    private void register() {
        String _fullName = fullName.getText().toString().trim();
        String _email = mail.getText().toString().trim();
        String _motDePasse = password.getText().toString().trim();
        String _identifiant = username.getText().toString().trim();
        Boolean admin = Boolean.FALSE;
        Boolean _consent = rgpd.isActivated();

        if (_identifiant.isEmpty() || _email.isEmpty() || _motDePasse.isEmpty() || _identifiant.isEmpty()) {
            FeedbackManager.showError(this, "All fields are required", null);
            return;
        }

        RegisterRequest registerRequest = new RegisterRequest(_identifiant, _email, _motDePasse, _fullName, admin, _consent);

        apiService.register(registerRequest)
                .enqueue(new Callback<RegisterResponse>() {
                    @Override public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            FeedbackManager.showSuccess(SignUpActivity.this, "Registration successful");
                            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            FeedbackManager.showError(SignUpActivity.this, "Registration failed: " + response.message(), null);
                        }
                    }

                    @Override public void onFailure(Call<RegisterResponse> call, Throwable t) {
                        FeedbackManager.showError(SignUpActivity.this, "Network error: " + t.getMessage(), null);
                    }
                });


    }
}
