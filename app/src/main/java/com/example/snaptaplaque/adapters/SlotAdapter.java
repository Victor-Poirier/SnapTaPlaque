package com.example.snaptaplaque.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snaptaplaque.R;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.ViewHolder> {
    private final String[] data;

    public SlotAdapter(String[] data) { this.data = data; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(data[position % data.length]); // % pour boucler sur A-Z
    }

    @Override
    public int getItemCount() { return Integer.MAX_VALUE; } // Infini

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(View v) { super(v); textView = v.findViewById(R.id.text_item); }
    }
}