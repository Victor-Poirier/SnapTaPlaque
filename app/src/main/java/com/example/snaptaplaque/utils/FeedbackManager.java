package com.example.snaptaplaque.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class FeedbackManager {

    // Message d'erreur simple
    public static void showError(Activity activity, String message, View anchorView) {
        if (anchorView != null) {
            Snackbar.make(anchorView, message, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }
    }

    public static void showErrorContext(Context context, String message, View anchorView) {
        if (anchorView != null) {
            Snackbar.make(anchorView, message, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    // Message succès
    public static void showSuccess(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    // Message succès
    public static void showSuccessContext(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // Erreur spécifique sur champ
    public static void showFieldError(EditText editText, String message) {
        editText.setError(message);
        editText.requestFocus();
    }

}
