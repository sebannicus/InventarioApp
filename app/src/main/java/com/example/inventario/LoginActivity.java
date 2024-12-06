package com.example.inventario;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventario.api.ApiService;
import com.example.inventario.models.LoginRequest;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa vistas
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        // Configura acción para el botón de login
        loginButton.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa todas las credenciales", Toast.LENGTH_SHORT).show();
            return;
        }

        // Realiza la autenticación con la API
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        LoginRequest loginRequest = new LoginRequest(username, password);

        Call<ResponseBody> call = apiService.login(loginRequest);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Parsear la respuesta JSON
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);

                        String message = jsonObject.getString("message");
                        int userId = jsonObject.getInt("user_id");

                        // Guardar el user_id en SharedPreferences
                        SharedPreferences preferences = getSharedPreferences("InventarioApp", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("user_id", userId);
                        editor.apply();

                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                        // Redirigir al MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar la respuesta", e);
                        Toast.makeText(LoginActivity.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Error en el inicio de sesión: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error al conectar con la API", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error de conexión: ", t);
            }
        });
    }
}
