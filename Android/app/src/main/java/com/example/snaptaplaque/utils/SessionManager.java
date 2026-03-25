package com.example.snaptaplaque.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestion de la session utilisateur, stockage du token JWT et des informations de connexion.
 */
public class SessionManager {

    /**
     * Nom du fichier de préférences pour stocker les données de session.
     */
    private static final String PREF_NAME = "snap_ta_plaque_session";

    /**
     * Clé pour stocker le token JWT dans les SharedPreferences.
     */
    private static final String KEY_TOKEN = "jwt_token";

    /**
     * Clé pour stocker le nom d'utilisateur dans les SharedPreferences.
     */
    private static final String KEY_USERNAME = "username";

    /**
     * Clé pour stocker le mot de passe dans les SharedPreferences.
     */
    private static final String KEY_PASSWORD = "password";

    /**
     * Instance de SharedPreferences pour accéder aux données de session.
     */
    private final SharedPreferences prefs;

    /**
     * Éditeur de SharedPreferences pour modifier les données de session.
     */
    private final SharedPreferences.Editor editor;

    /**
     * Constructeur de SessionManager, initialisant les SharedPreferences et leur éditeur.
     *
     * @param context Le contexte de l'application, utilisé pour accéder aux SharedPreferences.
     */
    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = prefs.edit();
    }

    /**
     * Enregistre les informations de session de l'utilisateur, y compris le token JWT, le nom d'utilisateur et le mot de passe.
     *
     * @param token    Le token JWT à stocker.
     * @param username Le nom d'utilisateur à stocker.
     * @param password Le mot de passe à stocker.
     */
    public void saveSession(String token, String username, String password) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    /**
     * Récupère le token JWT stocké dans les SharedPreferences.
     *
     * @return Le token JWT, ou null s'il n'existe pas.
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * Récupère le nom d'utilisateur stocké dans les SharedPreferences.
     *
     * @return Le nom d'utilisateur, ou null s'il n'existe pas.
     */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    /**
     * Récupère le mot de passe stocké dans les SharedPreferences.
     *
     * @return Le mot de passe, ou null s'il n'existe pas.
     */
    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, null);
    }

    /**
     * Vérifie si l'utilisateur est actuellement connecté en vérifiant la présence d'un token JWT.
     *
     * @return true si un token JWT est présent, false sinon.
     */
    public boolean isLoggedIn() {
        return getToken() != null;
    }

    /**
     * Efface toutes les données de session, déconnectant ainsi l'utilisateur.
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

}
