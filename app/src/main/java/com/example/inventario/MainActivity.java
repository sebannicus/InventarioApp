package com.example.inventario;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventario.ApiClient;
import com.example.inventario.api.ApiService;
import com.example.inventario.models.Producto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ListView productList;
    private TextView errorText;
    private ArrayAdapter<String> adapter;
    private List<String> productNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa las vistas
        productList = findViewById(R.id.productList);
        errorText = findViewById(R.id.errorText);

        // Configura el adaptador para la lista
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productNames);
        productList.setAdapter(adapter);

        // Llama a la API
        fetchProducts();
    }

    private void fetchProducts() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Producto>> call = apiService.getProductos();

        call.enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    errorText.setVisibility(View.GONE);
                    productList.setVisibility(View.VISIBLE);

                    // Agrega los nombres de los productos a la lista
                    for (Producto producto : response.body()) {
                        productNames.add(producto.getNombre());
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    errorText.setText("Error en la respuesta: " + response.code());
                    Log.e(TAG, "Error en la respuesta: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                errorText.setText("Error al conectar con la API: " + t.getMessage());
                Log.e(TAG, "Error al conectar con la API", t);
            }
        });
    }
}
