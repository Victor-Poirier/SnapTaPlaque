package com.example.snaptaplaque.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.fragments.PrivacyPolicyDialogFragment;
import com.example.snaptaplaque.models.api.account.LoginRequest;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.utils.FeedbackManager;
import com.example.snaptaplaque.utils.SessionManager;

/**
 * Activité de connexion de l'application SnapTaPlaque.
 *
 * <p>Présente un formulaire permettant à l'utilisateur de s'authentifier
 * avec son identifiant et son mot de passe. Selon l'action choisie :</p>
 * <ul>
 *     <li>Clic sur <b>Connexion</b> -> validation des champs puis appel à
 *         {@link AccountCall#login(android.content.Context, LoginRequest, SessionManager)}</li>
 *     <li>Clic sur <b>Inscription</b> -> redirection vers {@link SignUpActivity}</li>
 *     <li>Clic sur <b>Politique de confidentialité</b> -> affichage de
 *         {@link PrivacyPolicyDialogFragment}</li>
 * </ul>
 *
 * <p>Le token de session retourné par l'API lors d'une connexion réussie est
 * persisté via {@link SessionManager}, permettant aux activités suivantes
 * d'effectuer des requêtes authentifiées.</p>
 *
 * @see AccountCall#login(android.content.Context, LoginRequest, SessionManager)
 * @see SessionManager
 * @see SignUpActivity
 */
public class SignInActivity extends BaseActivity {

    /** Champ de saisie de l'identifiant utilisateur. */
    private EditText username;

    /** Champ de saisie du mot de passe utilisateur. */
    private EditText password;

    /** Gestionnaire de session chargé de persister le token d'authentification. */
    private SessionManager sessionManager;

    /**
     * Initialise le layout de connexion et configure les interactions utilisateur.
     *
     * <p>Cette méthode effectue les opérations suivantes :
     * <ol>
     *     <li>Gonfle le layout {@code sign_in.xml}</li>
     *     <li>Instancie le {@link SessionManager}</li>
     *     <li>Récupère les références vers les champs de saisie et les boutons</li>
     *     <li>Associe le bouton <b>Connexion</b> à {@link #login()}</li>
     *     <li>Associe le bouton <b>Inscription</b> à l'ouverture de {@link SignUpActivity}</li>
     *     <li>Associe le lien <b>Politique de confidentialité</b> à l'affichage
     *         de {@link PrivacyPolicyDialogFragment}</li>
     * </ol>
     * </p>
     *
     * @param savedInstanceState état sauvegardé de l'activité, ou {@code null}
     *                           lors du premier lancement
     */
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

        TextView privacyPolicyLink = findViewById(R.id.privacyPolicyLink);
        privacyPolicyLink.setOnClickListener(v -> {
            DialogFragment privacyDialog = new PrivacyPolicyDialogFragment();
            privacyDialog.show(getSupportFragmentManager(), "PrivacyPolicyDialog");
        });
    }

    /**
     * Récupère les valeurs saisies dans le formulaire et déclenche la requête
     * de connexion si les champs sont valides.
     *
     * <p>Comportement selon l'état des champs :</p>
     * <ul>
     *     <li>Champ(s) vide(s) -> affichage d'un message d'erreur via
     *         {@link FeedbackManager#showError(android.content.Context, String, String)}
     *         et interruption de la procédure</li>
     *     <li>Champs renseignés -> construction d'un {@link LoginRequest} et appel à
     *         {@link AccountCall#login(android.content.Context, LoginRequest, SessionManager)}</li>
     * </ul>
     *
     * <p>Les valeurs sont nettoyées des espaces superflus via {@link String#trim()}
     * avant toute validation.</p>
     */
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