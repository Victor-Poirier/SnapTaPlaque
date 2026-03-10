package com.example.snaptaplaque.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.api.account.LoginResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.utils.FeedbackManager;
import com.example.snaptaplaque.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends Activity {

    private EditText username, password;
    private Button signUp, signIn;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        // Même singleton Retrofit que dans LaunchActivity
        apiService = ApiClient.getRetrofit().create(ApiService.class);
        // Même SharedPreferences ("snap_tap_plaque_session") que dans LaunchActivity
        sessionManager = new SessionManager(this);

        username = findViewById(R.id.inputIdentifiant);
        password = findViewById(R.id.inputMotDePasse);
        signIn = findViewById(R.id.buttonConnexion);
        signUp = findViewById(R.id.buttonInscription);

        signIn.setOnClickListener(v -> login());
        signUp.setOnClickListener(v -> {
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

                            Log.d("token", token);

                            // Sauvegarder dans les mêmes SharedPreferences
                            // On laisse le temps à la sauvegarde de se faire avant de rediriger
                            new Handler().postDelayed(() -> {
                                sessionManager.saveSession(token, _username, _password);

                                // Rediriger vers MainActivity, vider le back stack
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }, 500); // 500ms de délai pour s'assurer que la session est sauvegardée
                        } else {
                            FeedbackManager.showError(SignInActivity.this, "Invalid credentials", null);
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        FeedbackManager.showError(SignInActivity.this, "Connection error", null);
                    }
                });
    }
}