package com.example.gymkratos;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegistroActivity extends AppCompatActivity {

    private Spinner spinnerDisciplina;
    private Spinner spinnerMonto;
    private EditText inputMontoEspecial;
    private ArrayAdapter<String> adaptadorMontos;
    private List<String> listaMontos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerDisciplina = findViewById(R.id.spinnerDisciplina);
        spinnerMonto = findViewById(R.id.spinnerMonto);
        inputMontoEspecial = findViewById(R.id.inputMontoEspecial);

        EditText campoNombre = findViewById(R.id.inputNombre);
        EditText campoTelefono = findViewById(R.id.inputTelefono);
        Button botonGuardar = findViewById(R.id.btnGuardar);
        Button botonVolver = findViewById(R.id.btnVolver);

        // --- 1. CONFIGURAR DISCIPLINAS ---
        String[] disciplinas = {"Selecciona una disciplina...", "Kickboxing", "Boxeo", "Jiujitsu", "Gym", "Personalizado", "Plan Especial"};
        ArrayAdapter<String> adaptadorDisciplinas = new ArrayAdapter<>(this, R.layout.molde_spinner, disciplinas);
        spinnerDisciplina.setAdapter(adaptadorDisciplinas);

        // --- 2. CONFIGURAR MONTOS DINÁMICOS ---
        listaMontos = new ArrayList<>();
        listaMontos.add("Elige primero la disciplina...");
        adaptadorMontos = new ArrayAdapter<>(this, R.layout.molde_spinner, listaMontos);
        spinnerMonto.setAdapter(adaptadorMontos);

        // --- 3. LÓGICA EN CASCADA ---
        spinnerDisciplina.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccion = disciplinas[position];
                listaMontos.clear();

                if (!seleccion.equals("Plan Especial")) {
                    spinnerMonto.setVisibility(View.VISIBLE);
                    inputMontoEspecial.setVisibility(View.GONE);
                }

                switch (seleccion) {
                    case "Kickboxing":
                        listaMontos.add("35000");
                        break;
                    case "Boxeo":
                        listaMontos.add("30000");
                        break;
                    case "Jiujitsu":
                        listaMontos.add("35000");
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
                    case "Plan Especial":
                        spinnerMonto.setVisibility(View.GONE);
                        inputMontoEspecial.setVisibility(View.VISIBLE);
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

        // --- 4. GUARDAR DATOS ---
        botonGuardar.setOnClickListener(v -> {
            String nombre = campoNombre.getText().toString().trim();
            String telefono_crudo = campoTelefono.getText().toString().trim().replace(" ", "");
            String disciplina = spinnerDisciplina.getSelectedItem().toString();
            String montoAguardar = "";

            if (disciplina.contains("Selecciona")) {
                Toast.makeText(RegistroActivity.this, "¡Faltan datos o elegir disciplina!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (disciplina.equals("Plan Especial")) {
                montoAguardar = inputMontoEspecial.getText().toString().trim();
                if (montoAguardar.isEmpty()) {
                    Toast.makeText(RegistroActivity.this, "Por favor escribe el precio del plan especial", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                montoAguardar = spinnerMonto.getSelectedItem().toString();
                if (montoAguardar.contains("Elige")) {
                    Toast.makeText(RegistroActivity.this, "¡Falta el precio!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (nombre.isEmpty() || telefono_crudo.isEmpty()) {
                Toast.makeText(RegistroActivity.this, "¡Faltan datos del cliente!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!telefono_crudo.startsWith("+569") || telefono_crudo.length() != 12) {
                Toast.makeText(RegistroActivity.this, "Error: Debe incluir el +569 y los 8 números", Toast.LENGTH_LONG).show();
                return;
            }

            String fechaActual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            String estado = "Activo";

            // SEGURIDAD: Enlace oculto para GitHub
            String url = "URL_PRIVADA_POR_SEGURIDAD";

            Toast.makeText(RegistroActivity.this, "Guardando cliente...", Toast.LENGTH_SHORT).show();

            final String telefonoParaGuardar = telefono_crudo;
            final String montoFinal = montoAguardar;

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        Toast.makeText(RegistroActivity.this, "¡Registrado con éxito!", Toast.LENGTH_LONG).show();
                        campoNombre.setText("");
                        campoTelefono.setText("");
                        inputMontoEspecial.setText("");
                        spinnerDisciplina.setSelection(0);
                    },
                    error -> Toast.makeText(RegistroActivity.this, "Error de conexión", Toast.LENGTH_LONG).show()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("fecha", fechaActual);
                    params.put("nombre", nombre);
                    params.put("telefono", telefonoParaGuardar);
                    params.put("disciplina", disciplina);
                    params.put("monto", montoFinal);
                    params.put("estado", estado);
                    return params;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(RegistroActivity.this);
            queue.add(stringRequest);
        });

        botonVolver.setOnClickListener(v -> finish());
    }
}