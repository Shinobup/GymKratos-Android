package com.example.gymkratos;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EliminarActivity extends AppCompatActivity {

    private Spinner spinnerClientes;
    private ArrayList<String> listaNombres;
    private ArrayAdapter<String> adaptador;
    
    // SEGURIDAD: Enlace oculto para GitHub
    private String urlAPI = "URL_PRIVADA_POR_SEGURIDAD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_eliminar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerClientes = findViewById(R.id.spinnerClientesEliminar);
        Button botonEliminar = findViewById(R.id.btnEliminarAccion);
        Button botonVolver = findViewById(R.id.btnVolverDesdeEliminar);

        listaNombres = new ArrayList<>();
        listaNombres.add("Cargando clientes...");
        adaptador = new ArrayAdapter<>(this, R.layout.molde_spinner, listaNombres);
        spinnerClientes.setAdapter(adaptador);

        cargarClientes();

        botonEliminar.setOnClickListener(v -> {
            String clienteSeleccionado = spinnerClientes.getSelectedItem().toString();

            if (clienteSeleccionado.contains("Selecciona") || clienteSeleccionado.contains("Cargando")) {
                Toast.makeText(EliminarActivity.this, "Por favor selecciona un cliente a eliminar", Toast.LENGTH_SHORT).show();
                return;
            }

            ejecutarBaja(clienteSeleccionado);
        });

        botonVolver.setOnClickListener(v -> finish());
    }

    private void cargarClientes() {
        StringRequest peticionGet = new StringRequest(Request.Method.GET, urlAPI,
                response -> {
                    try {
                        listaNombres.clear();
                        listaNombres.add("Selecciona un cliente...");
                        JSONArray jsonArray = new JSONArray(response);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject cliente = jsonArray.getJSONObject(i);
                            listaNombres.add(cliente.getString("nombre"));
                        }
                        adaptador.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error procesando lista", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show());

        RequestQueue cola = Volley.newRequestQueue(this);
        cola.add(peticionGet);
    }

    private void ejecutarBaja(String nombre) {
        Toast.makeText(this, "Eliminando a " + nombre + "...", Toast.LENGTH_SHORT).show();

        StringRequest peticionPost = new StringRequest(Request.Method.POST, urlAPI,
                response -> {
                    Toast.makeText(this, "🚨 " + nombre + " dado de baja correctamente", Toast.LENGTH_LONG).show();
                    cargarClientes(); 
                },
                error -> Toast.makeText(this, "Error al eliminar en la nube", Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "eliminar"); 
                params.put("nombre", nombre);
                return params;
            }
        };

        RequestQueue cola = Volley.newRequestQueue(this);
        cola.add(peticionPost);
    }
}
