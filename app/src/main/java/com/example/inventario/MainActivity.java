package com.example.inventario;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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

    private static final int REQUEST_CODE_VOICE = 1;

    private enum VoiceState { NAME, DESCRIPTION, PRICE, QUANTITY, NONE }
    private VoiceState currentVoiceState = VoiceState.NONE;

    private RecyclerView recyclerView;
    private TextView errorText;
    private EditText productNameInput, productDescriptionInput, productPriceInput, productQuantityInput;
    private ProductAdapter adapter;
    private List<Producto> productos = new ArrayList<>();
    private List<Producto> filteredProductos = new ArrayList<>();
    private int selectedProductId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.productList);
        errorText = findViewById(R.id.errorText);
        productNameInput = findViewById(R.id.productNameInput);
        productDescriptionInput = findViewById(R.id.productDescriptionInput);
        productPriceInput = findViewById(R.id.productPriceInput);
        productQuantityInput = findViewById(R.id.productQuantityInput);

        adapter = new ProductAdapter(filteredProductos, this::onProductSelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button voiceAddButton = findViewById(R.id.voiceAddButton);
        voiceAddButton.setOnClickListener(v -> startVoiceRecognition());

        fetchProducts();
    }

    private void fetchProducts() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getProductos().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful()) {
                    productos.clear();
                    productos.addAll(response.body());
                    filteredProductos.clear();
                    filteredProductos.addAll(productos);
                    adapter.notifyDataSetChanged();
                } else {
                    errorText.setText("Error en la respuesta del servidor.");
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                errorText.setText("Error al conectar con la API.");
            }
        });
    }

    private void startVoiceRecognition() {
        currentVoiceState = VoiceState.NAME;
        promptVoiceInput("Dime el nombre del producto.");
    }

    private void promptVoiceInput(String prompt) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        startActivityForResult(intent, REQUEST_CODE_VOICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_VOICE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String input = results.get(0);
                processVoiceInput(input);
            }
        }
    }

    private void processVoiceInput(String input) {
        switch (currentVoiceState) {
            case NAME:
                productNameInput.setText(input);
                currentVoiceState = VoiceState.DESCRIPTION;
                promptVoiceInput("Dime la descripción del producto.");
                break;

            case DESCRIPTION:
                productDescriptionInput.setText(input);
                currentVoiceState = VoiceState.PRICE;
                promptVoiceInput("Dime el precio del producto.");
                break;

            case PRICE:
                try {
                    Double.parseDouble(input); // Validar que sea un número
                    productPriceInput.setText(input);
                    currentVoiceState = VoiceState.QUANTITY;
                    promptVoiceInput("Dime la cantidad del producto.");
                } catch (NumberFormatException e) {
                    promptVoiceInput("El precio debe ser un número. Inténtalo de nuevo.");
                }
                break;

            case QUANTITY:
                try {
                    Integer.parseInt(input); // Validar que sea un número entero
                    productQuantityInput.setText(input);
                    currentVoiceState = VoiceState.NONE;
                    addProduct();
                    Toast.makeText(this, "Producto agregado con éxito.", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    promptVoiceInput("La cantidad debe ser un número entero. Inténtalo de nuevo.");
                }
                break;

            default:
                currentVoiceState = VoiceState.NAME;
                promptVoiceInput("Dime el nombre del producto.");
        }
    }

    private void addProduct() {
        String name = productNameInput.getText().toString();
        String description = productDescriptionInput.getText().toString();
        String price = productPriceInput.getText().toString();
        String quantity = productQuantityInput.getText().toString();

        if (name.isEmpty() || description.isEmpty() || price.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        Producto producto = new Producto(name, description, Integer.parseInt(quantity), Double.parseDouble(price), true);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.addProducto(producto).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful()) {
                    fetchProducts(); // Refresca la lista
                } else {
                    Toast.makeText(MainActivity.this, "Error al agregar producto.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onProductSelected(Producto producto) {
        selectedProductId = producto.getId();
        productNameInput.setText(producto.getNombre());
        productDescriptionInput.setText(producto.getDescripcion());
        productPriceInput.setText(String.valueOf(producto.getPrecio()));
        productQuantityInput.setText(String.valueOf(producto.getCantidad()));
    }
}
