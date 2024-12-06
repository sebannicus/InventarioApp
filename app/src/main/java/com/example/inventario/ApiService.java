package com.example.inventario.api;

import com.example.inventario.models.LoginRequest;
import com.example.inventario.models.Producto;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/productos/")  // Endpoint de la API
    Call<List<Producto>> getProductos();

    @POST("api/productos/")
    Call<Producto> addProducto(@Body Producto producto);

    @PUT("api/productos/{id}/")
    Call<Producto> updateProducto(@Path("id") int id, @Body Producto producto);

    @DELETE("api/productos/{id}/")
    Call<Void> deleteProducto(@Path("id") int id);

    @POST("api/login/")  // Login con JSON (username, password)
    Call<ResponseBody> login(@Body LoginRequest loginRequest);
}
