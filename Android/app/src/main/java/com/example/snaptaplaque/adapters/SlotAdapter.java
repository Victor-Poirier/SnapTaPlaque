package com.example.snaptaplaque.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snaptaplaque.R;

/**
 * Adaptateur {@link RecyclerView} simulant un défilement infini pour un slot
 * de caractères (par exemple A–Z).
 *
 * <p>Le défilement infini est obtenu en déclarant un nombre d'éléments égal à
 * {@link Integer#MAX_VALUE} et en résolvant la position réelle via l'opérateur
 * modulo ({@code position % data.length}), ce qui permet de boucler
 * indéfiniment sur le tableau {@code data}.</p>
 *
 * <p>Chaque élément est affiché dans un {@link TextView} gonflé depuis le layout
 * {@code item_slot.xml}.</p>
 *
 * @see SlotAdapter.ViewHolder
 */
public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.ViewHolder> {

    /**
     * Tableau des valeurs à afficher en boucle dans le slot
     * (par exemple {@code ["A", "B", ..., "Z"]}).
     */
    private final String[] data;

    /**
     * Crée un nouvel adaptateur avec le tableau de valeurs fourni.
     *
     * @param data tableau des valeurs à afficher ; ne doit pas être vide
     *             afin d'éviter une division par zéro dans {@link #onBindViewHolder}
     */
    public SlotAdapter(String[] data) { this.data = data; }

    /**
     * Gonfle le layout {@code item_slot.xml} et crée un nouveau {@link ViewHolder}.
     *
     * @param parent   le {@link ViewGroup} auquel la nouvelle vue sera rattachée
     * @param viewType le type de la vue (non utilisé ici, un seul type d'élément)
     * @return un nouveau {@link ViewHolder} contenant la vue gonflée
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Lie les données à un {@link ViewHolder} à la position donnée.
     *
     * <p>La valeur affichée est déterminée par {@code position % data.length},
     * ce qui permet de boucler en continu sur le tableau {@code data} quelle
     * que soit la position réelle dans le flux infini.</p>
     *
     * @param holder   le {@link ViewHolder} à mettre à jour
     * @param position la position de l'élément dans l'adaptateur
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(data[position % data.length]); // % pour boucler sur A-Z
    }

    /**
     * Retourne {@link Integer#MAX_VALUE} pour simuler une liste de défilement infini.
     *
     * @return nombre d'éléments virtuellement infini
     */
    @Override
    public int getItemCount() { return Integer.MAX_VALUE; } // Infini

    /**
     * ViewHolder représentant un élément du slot.
     *
     * <p>Contient une référence vers le {@link TextView} identifié par
     * {@code R.id.text_item} dans le layout {@code item_slot.xml}.</p>
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        /** Composant d'affichage de la valeur du slot. */
        TextView textView;

        /**
         * Initialise le ViewHolder et récupère la référence au {@link TextView}.
         *
         * @param v la vue racine de l'élément gonflé depuis {@code item_slot.xml}
         */
        public ViewHolder(View v) { super(v); textView = v.findViewById(R.id.text_item); }
    }
}