package com.example.inventario.api;

import com.example.inventario.models.Producto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/productos/")  // Endpoint de la API
    Call<List<Producto>> getProductos();
}
