package com.example.inventario;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventario.api.ApiService;
import com.example.inventario.models.Producto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private TextView errorText;
    private EditText productNameInput, productPriceInput, productQuantityInput;
    private Button addProductButton, updateProductButton, deleteProductButton;
    private ProductAdapter adapter;
    private List<Producto> productos = new ArrayList<>();
    private int selectedProductId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa las vistas
        recyclerView = findViewById(R.id.productList);
        errorText = findViewById(R.id.errorText);
        productNameInput = findViewById(R.id.productNameInput);
        productPriceInput = findViewById(R.id.productPriceInput);
        productQuantityInput = findViewById(R.id.productQuantityInput);

        addProductButton = findViewById(R.id.addProductButton);
        updateProductButton = findViewById(R.id.updateProductButton);
        deleteProductButton = findViewById(R.id.deleteProductButton);

        // Configura el RecyclerView
        adapter = new ProductAdapter(productos, this::onProductSelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Llama a la API para obtener los productos
        fetchProducts();

        // Configura los botones
        addProductButton.setOnClickListener(v -> addProduct());
        updateProductButton.setOnClickListener(v -> updateProduct());
        deleteProductButton.setOnClickListener(v -> deleteProduct());
    }

    private void fetchProducts() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Producto>> call = apiService.getProductos();

        call.enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    errorText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    productos.clear();
                    productos.addAll(response.body());
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

    private void addProduct() {
        String name = productNameInput.getText().toString();
        String price = productPriceInput.getText().toString();
        String quantity = productQuantityInput.getText().toString();

        if (name.isEmpty() || price.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Producto producto = new Producto(name, "Descripción", Integer.parseInt(quantity), Double.parseDouble(price), true);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.addProducto(producto).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Producto agregado", Toast.LENGTH_SHORT).show();
                    fetchProducts(); // Refresca la lista
                    clearInputs();
                } else {
                    Toast.makeText(MainActivity.this, "Error al agregar producto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProduct() {
        if (selectedProductId == -1) {
            Toast.makeText(MainActivity.this, "Seleccione un producto para modificar", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = productNameInput.getText().toString();
        String price = productPriceInput.getText().toString();
        String quantity = productQuantityInput.getText().toString();

        if (name.isEmpty() || price.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Producto updatedProduct = new Producto(name, "Descripción actualizada", Integer.parseInt(quantity), Double.parseDouble(price), true);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.updateProducto(selectedProductId, updatedProduct).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Producto modificado", Toast.LENGTH_SHORT).show();
                    fetchProducts(); // Refresca la lista
                    clearInputs();
                } else {
                    Toast.makeText(MainActivity.this, "Error al modificar producto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProduct() {
        if (selectedProductId == -1) {
            Toast.makeText(MainActivity.this, "Seleccione un producto para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.deleteProducto(selectedProductId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    fetchProducts(); // Refresca la lista
                    clearInputs();
                } else {
                    Toast.makeText(MainActivity.this, "Error al eliminar producto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearInputs() {
        productNameInput.setText("");
        productPriceInput.setText("");
        productQuantityInput.setText("");
        selectedProductId = -1;
    }

    private void onProductSelected(Producto producto) {
        selectedProductId = producto.getId();
        productNameInput.setText(producto.getNombre());
        productPriceInput.setText(String.valueOf(producto.getPrecio()));
        productQuantityInput.setText(String.valueOf(producto.getCantidad()));
    }
}
