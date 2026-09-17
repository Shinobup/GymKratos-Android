package com.example.gymkratos;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RenovarActivity extends AppCompatActivity {

    private Spinner spinnerClientes;
    private Spinner spinnerDisciplina;
    private Spinner spinnerMonto;

    private ArrayList<String> listaNombresClientes;
    private ArrayAdapter<String> adapterClientes;

    private ArrayAdapter<String> adaptadorMontos;
    private List<String> listaMontos;

    private String urlAPI = "https://script.google.com/macros/s/AKfycbxDV4ogyyFwqsUNrvhoy7O0gTJgpZTFx6_Rn3N4WIMEMgWLKkI10AimFQdPgBfQCI7o/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_renovar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. CONFIGURAR SPINNER DE CLIENTES
        spinnerClientes = findViewById(R.id.spinnerClientesRenovar);
        listaNombresClientes = new ArrayList<>();
        listaNombresClientes.add("Cargando clientes...");
        adapterClientes = new ArrayAdapter<>(this, R.layout.molde_spinner, listaNombresClientes);
        spinnerClientes.setAdapter(adapterClientes);

        // 2. CONFIGURAR SPINNER DE DISCIPLINAS
        spinnerDisciplina = findViewById(R.id.spinnerDisciplinaRenovar);
        String[] disciplinas = {"Selecciona nueva disciplina...", "Kickboxing", "Boxeo", "Jiujitsu", "Gym", "Personalizado"};
        ArrayAdapter<String> adaptadorDisciplinas = new ArrayAdapter<>(this, R.layout.molde_spinner, disciplinas);
        spinnerDisciplina.setAdapter(adaptadorDisciplinas);

        // 3. CONFIGURAR SPINNER DE MONTOS (EN CASCADA)
        spinnerMonto = findViewById(R.id.spinnerMontoRenovar);
        listaMontos = new ArrayList<>();
        listaMontos.add("Elige primero la disciplina...");
        adaptadorMontos = new ArrayAdapter<>(this, R.layout.molde_spinner, listaMontos);
        spinnerMonto.setAdapter(adaptadorMontos);

        spinnerDisciplina.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccion = disciplinas[position];
                listaMontos.clear();

                switch (seleccion) {
                    case "Kickboxing":
                    case "Jiujitsu":
                        listaMontos.add("35000");
                        break;
                    case "Boxeo":
                        listaMontos.add("30000");
                        break;
                    case "Gym":
                        listaMontos.add("30000");
                        listaMontos.add("35000");
                        break;
                    case "Personalizado":
                        listaMontos.add("70000");
                        listaMontos.add("90000");
                        listaMontos.add("110000");
                        break;
                    default:
                        listaMontos.add("Elige primero la disciplina...");
                        break;
                }
                adaptadorMontos.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Cargar clientes desde Google
        obtenerClientesYCalcularVencimientos();

        // 4. BOTONES DE ACCIÓN
        Button botonRenovar = findViewById(R.id.btnRenovarAccion);
        Button botonVolver = findViewById(R.id.btnVolverDesdeRenovar);

        botonRenovar.setOnClickListener(v -> {
            String cliente = spinnerClientes.getSelectedItem().toString();
            String disciplina = spinnerDisciplina.getSelectedItem().toString();
            String monto = spinnerMonto.getSelectedItem().toString();

            if (cliente.contains("Cargando") || cliente.contains("Selecciona") || disciplina.contains("Selecciona") || monto.contains("Elige")) {
                Toast.makeText(RenovarActivity.this, "Por favor selecciona cliente, disciplina y precio", Toast.LENGTH_SHORT).show();
            } else {
                renovarClienteEnGoogle(cliente, disciplina, monto);
            }
        });

        botonVolver.setOnClickListener(v -> finish());
    }

    private void obtenerClientesYCalcularVencimientos() {
        LinearLayout contenedorVencidos = findViewById(R.id.contenedorVencidos);

        StringRequest peticionGet = new StringRequest(Request.Method.GET, urlAPI,
                response -> {
                    try {
                        listaNombresClientes.clear();
                        listaNombresClientes.add("Selecciona un cliente...");
                        contenedorVencidos.removeAllViews();

                        JSONArray jsonArray = new JSONArray(response);
                        SimpleDateFormat sdfGoogle = new SimpleDateFormat("yyyy-MM-dd");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject cliente = jsonArray.getJSONObject(i);
                            String nombre = cliente.getString("nombre");
                            String fechaCruda = cliente.getString("fecha");

                            listaNombresClientes.add(nombre);

                            if (fechaCruda.contains("T")) {
                                String soloFecha = fechaCruda.split("T")[0];
                                Date fechaPago = sdfGoogle.parse(soloFecha);
                                Date hoy = new Date();

                                long diferenciaMilisegundos = hoy.getTime() - fechaPago.getTime();
                                long diasPasados = diferenciaMilisegundos / (1000 * 60 * 60 * 24);

                                if (diasPasados >= 30) {
                                    crearTarjetaVencido(contenedorVencidos, nombre, diasPasados);
                                }
                            }
                        }
                        adapterClientes.notifyDataSetChanged();

                        if (contenedorVencidos.getChildCount() == 0) {
                            TextView tvCero = new TextView(this);
                            tvCero.setText("✅ Todos los clientes están al día.");
                            tvCero.setTextColor(Color.GREEN);
                            contenedorVencidos.addView(tvCero);
                        }

                    } catch (Exception e) {
                        Toast.makeText(RenovarActivity.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(RenovarActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show());

        RequestQueue cola = Volley.newRequestQueue(this);
        cola.add(peticionGet);
    }

    private void crearTarjetaVencido(LinearLayout contenedor, String nombre, long diasPasados) {
        TextView tarjeta = new TextView(this);
        tarjeta.setText("❌ " + nombre + "\n(Vencido hace " + (diasPasados - 30) + " días)");
        tarjeta.setTextColor(Color.WHITE);
        tarjeta.setBackgroundColor(Color.parseColor("#420000"));
        tarjeta.setPadding(30, 20, 30, 20);
        tarjeta.setTextSize(16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        tarjeta.setLayoutParams(params);

        tarjeta.setOnClickListener(v -> {
            int posicion = adapterClientes.getPosition(nombre);
            if(posicion >= 0) {
                spinnerClientes.setSelection(posicion);
                Toast.makeText(this, "Cliente seleccionado arriba ☝️", Toast.LENGTH_SHORT).show();
            }
        });

        contenedor.addView(tarjeta);
    }

    private void renovarClienteEnGoogle(String nombre, String disciplina, String monto) {
        Toast.makeText(this, "Actualizando en la base de datos...", Toast.LENGTH_SHORT).show();
        String fechaActual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        StringRequest peticionPost = new StringRequest(Request.Method.POST, urlAPI,
                response -> {
                    Toast.makeText(RenovarActivity.this, "¡Membresía renovada con éxito!", Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> Toast.makeText(RenovarActivity.this, "Error al renovar", Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "renovar");
                params.put("nombre", nombre);
                params.put("fecha", fechaActual);
                params.put("disciplina", disciplina);
                params.put("monto", monto);
                return params;
            }
        };

        RequestQueue cola = Volley.newRequestQueue(this);
        cola.add(peticionPost);
    }
}