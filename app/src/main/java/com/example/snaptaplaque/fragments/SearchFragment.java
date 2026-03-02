package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.snaptaplaque.R;

/**
 * Fragment dédié à la recherche de véhicules par plaque d'immatriculation.
 * <p>
 * Ce fragment affiche l'interface de recherche permettant à l'utilisateur
 * de saisir ou scanner un numéro de plaque d'immatriculation afin d'obtenir
 * les informations associées au véhicule correspondant.
 * </p>
 * <p>
 * Actuellement, ce fragment se limite au gonflage du layout {@code fragment_search}.
 * Les fonctionnalités de recherche (champ de saisie, bouton de scan, appels API, etc.)
 * devront être implémentées dans une version ultérieure.
 * </p>
 *
 * @author SnapTaPlaque's Team
 * @version 1.0
 * @see Fragment
 */
public class SearchFragment extends Fragment {

    /**
     * Initialise et retourne la hiérarchie de vues associée à ce fragment.
     * <p>
     * Gonfle le layout {@code fragment_search} qui contient l'interface
     * de recherche de véhicules.
     * </p>
     *
     * @param inflater          le {@link LayoutInflater} utilisé pour gonfler la vue du fragment
     * @param container         le {@link ViewGroup} parent auquel la vue sera éventuellement
     *                          rattachée, ou {@code null} si aucun parent n'est disponible
     * @param savedIntanceState le {@link Bundle} contenant l'état précédemment sauvegardé
     *                          du fragment, ou {@code null} lors d'une première création
     * @return la {@link View} racine du fragment représentant l'écran de recherche
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedIntanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
}