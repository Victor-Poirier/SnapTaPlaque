package com.example.snaptaplaque.utils;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

/**
 * Gestionnaire centralisé des retours utilisateurs (UI Feedback).
 * <p>Cette classe fournit des méthodes statiques pour afficher des messages de succès
 * ou d'erreur, en utilisant soit des {@link Toast}, soit des {@link Snackbar}
 * selon le contexte de l'écran.</p>
 */
public class FeedbackManager {

    /**
     * Affiche un message d'erreur à l'utilisateur.
     * <p>Si une {@code anchorView} est fournie, une {@link Snackbar} est utilisée
     * (recommandé pour les Material Design layouts). Sinon, un {@link Toast} classique
     * est affiché.</p>
     *
     * @param activity   L'activité hôte affichant le message.
     * @param message    Le texte de l'erreur à afficher.
     * @param anchorView La vue de référence pour la Snackbar (peut être {@code null}).
     */
    public static void showError(Activity activity, String message, View anchorView) {
        if (anchorView != null) {
            Snackbar.make(anchorView, message, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Affiche un message de succès bref à l'utilisateur.
     * <p>Utilise un {@link Toast} de courte durée pour confirmer une action
     * réussie (ex: "Inscription réussie", "Favori ajouté").</p>
     *
     * @param activity L'activité hôte.
     * @param message  Le texte de confirmation à afficher.
     */
    public static void showSuccess(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
}
