package com.example.snaptaplaque.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;


import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.fragments.PrivacyPolicyDialogFragment;
import com.example.snaptaplaque.models.api.account.RegisterRequest;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.utils.FeedbackManager;

/**
 * Activité d'inscription de l'application SnapTaPlaque.
 *
 * <p>Présente un formulaire permettant à un nouvel utilisateur de créer un compte
 * en renseignant son identifiant, son nom complet, son adresse e-mail, son mot de
 * passe et en acceptant la politique de confidentialité. Selon l'action choisie :</p>
 * <ul>
 *     <li>Clic sur <b>Inscription</b> -> validation des champs puis appel à
 *         {@link AccountCall#register(android.content.Context, RegisterRequest)}</li>
 *     <li>Clic sur <b>Connexion</b> -> redirection vers {@link SignInActivity}</li>
 *     <li>Clic sur <b>Politique de confidentialité</b> -> affichage de
 *         {@link PrivacyPolicyDialogFragment}</li>
 * </ul>
 *
 * <p>L'identifiant saisi est automatiquement nettoyé de tout caractère non
 * alphanumérique avant envoi à l'API.</p>
 *
 * @see AccountCall#register(android.content.Context, RegisterRequest)
 * @see SignInActivity
 */
public class SignUpActivity extends BaseActivity {

    /** Champ de saisie de l'identifiant utilisateur. */
    private EditText username;

    /** Champ de saisie du nom complet de l'utilisateur. */
    private EditText fullName;

    /** Champ de saisie de l'adresse e-mail. */
    private EditText mail;

    /** Champ de saisie du mot de passe. */
    private EditText password;

    /** Interrupteur de consentement à la politique de confidentialité (RGPD). */
    private SwitchCompat rgpd;

    /**
     * Initialise le layout d'inscription et configure les interactions utilisateur.
     *
     * <p>Cette méthode effectue les opérations suivantes :
     * <ol>
     *     <li>Gonfle le layout {@code sign_up.xml}</li>
     *     <li>Récupère les références vers les champs de saisie, le switch RGPD
     *         et les boutons</li>
     *     <li>Associe le bouton <b>Inscription</b> à {@link #register()}</li>
     *     <li>Associe le bouton <b>Connexion</b> à l'ouverture de {@link SignInActivity}</li>
     *     <li>Associe le lien <b>Politique de confidentialité</b> à l'affichage
     *         de {@link PrivacyPolicyDialogFragment}</li>
     * </ol>
     * </p>
     *
     * @param savedInstanceState état sauvegardé de l'activité, ou {@code null}
     *                           lors du premier lancement
     */
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

    /**
     * Récupère et valide les valeurs saisies dans le formulaire, puis déclenche
     * la requête d'inscription si toutes les conditions sont remplies.
     *
     * <p>Comportement selon l'état des champs :</p>
     * <ul>
     *     <li>Champ(s) vide(s) ou consentement RGPD non accordé -> affichage d'un
     *         message d'erreur via
     *         {@link FeedbackManager#showError(android.content.Context, String, String)}
     *         et interruption de la procédure</li>
     *     <li>Tous les champs renseignés et consentement accordé -> construction d'un
     *         {@link RegisterRequest} et appel à
     *         {@link AccountCall#register(android.content.Context, RegisterRequest)}</li>
     * </ul>
     *
     * <p>Les valeurs sont nettoyées des espaces superflus via {@link String#trim()}.
     * L'identifiant est en outre purgé de tout caractère non alphanumérique
     * ({@code [^a-zA-Z0-9]}) afin de garantir sa conformité avec les contraintes
     * de l'API.</p>
     *
     * <p>Le champ {@code admin} est systématiquement fixé à {@code false} :
     * le rôle administrateur ne peut pas être auto-attribué lors de l'inscription.</p>
     */
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