package com.example.gymkratos;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
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
    private ArrayList<String> listaNombresClientes;
    private ArrayAdapter<String> adapterClientes;

    String urlAPI = "PONER_AQUI_TU_ENLACE_DE_GOOGLE_APPS_SCRIPT";
    private String profeActual = "Paulo";

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

        // Recibir quién abrió la pantalla
        profeActual = getIntent().getStringExtra("PROFE_ACTUAL");
        if (profeActual == null) profeActual = "Paulo";

        spinnerClientes = findViewById(R.id.spinnerClientesEliminar);
        listaNombresClientes = new ArrayList<>();
        listaNombresClientes.add("Cargando clientes...");

        adapterClientes = new ArrayAdapter<>(this, R.layout.molde_spinner, listaNombresClientes);
        spinnerClientes.setAdapter(adapterClientes);

        obtenerClientesParaEliminar();

        Button btnEliminar = findViewById(R.id.btnEliminarAccion);
        Button btnVolver = findViewById(R.id.btnVolverDesdeEliminar);

        btnEliminar.setOnClickListener(v -> confirmarEliminacion());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void obtenerClientesParaEliminar() {
        StringRequest peticionGet = new StringRequest(Request.Method.GET, urlAPI,
                response -> {
                    try {
                        listaNombresClientes.clear();
                        listaNombresClientes.add("Selecciona un cliente...");

                        JSONArray jsonArray = new JSONArray(response);
                        boolean esAdmin = profeActual.equalsIgnoreCase("Paulo");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject cliente = jsonArray.getJSONObject(i);

                            // FILTRO DE PRIVACIDAD
                            String profesorCliente = cliente.optString("profesor", "Paulo");
                            if (!esAdmin && !profesorCliente.equalsIgnoreCase(profeActual)) {
                                continue;
                            }

                            String nombre = cliente.getString("nombre");
                            listaNombresClientes.add(nombre);
                        }
                        adapterClientes.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(EliminarActivity.this, "Error al cargar la lista", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(EliminarActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show());

        peticionGet.setRetryPolicy(new DefaultRetryPolicy(
                15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        RequestQueue cola = Volley.newRequestQueue(this);
        cola.add(peticionGet);
    }

    private void confirmarEliminacion() {
        String cliente = spinnerClientes.getSelectedItem().toString();

        if (cliente.contains("Cargando") || cliente.contains("Selecciona")) {
            Toast.makeText(this, "Por favor selecciona un cliente válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ventana de confirmación para evitar borrados por error
        new AlertDialog.Builder(this)
                .setTitle("¿Estás seguro?")
                .setMessage("Vas a eliminar a " + cliente + ". Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> eliminarClienteEnGoogle(cliente))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarClienteEnGoogle(String nombre) {
        Toast.makeText(this, "Eliminando cliente...", Toast.LENGTH_SHORT).show();

        StringRequest peticionPost = new StringRequest(Request.Method.POST, urlAPI,
                response -> {
                    Toast.makeText(EliminarActivity.this, "¡Cliente eliminado!", Toast.LENGTH_LONG).show();
                    finish(); // Cierra la pantalla y vuelve al menú
                },
                error -> Toast.makeText(EliminarActivity.this, "Error al eliminar", Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "eliminar"); // Le avisa al Google Script qué debe hacer
                params.put("nombre", nombre);
                params.put("profesor", profeActual); // Le avisa en qué pestaña buscar
                return params;
            }
        };

        peticionPost.setRetryPolicy(new DefaultRetryPolicy(
                15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        RequestQueue cola = Volley.newRequestQueue(this);
        cola.add(peticionPost);
    }
}