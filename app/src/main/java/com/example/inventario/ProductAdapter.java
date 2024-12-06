package com.example.inventario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventario.models.Producto;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Producto> productos;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Producto producto);
    }

    public ProductAdapter(List<Producto> productos, OnProductClickListener listener) {
        this.productos = productos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.textView.setText(producto.getNombre());
        holder.itemView.setOnClickListener(v -> listener.onProductClick(producto));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
