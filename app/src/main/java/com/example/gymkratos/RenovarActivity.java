package com.example.gymkratos;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RenovarActivity extends AppCompatActivity {

    private Spinner spinnerClientes;
    private CheckBox cbKickboxing, cbBoxeo, cbJiujitsu, cbGym, cbPersonalizado, cbPlanEspecial;
    private EditText inputMontoUnico;

    private ArrayList<String> listaNombresClientes;
    private ArrayAdapter<String> adapterClientes;

    String urlAPI = "PONER_AQUI_TU_ENLACE_DE_GOOGLE_APPS_SCRIPT";
    private String profeActual = "Paulo";

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

        // Recibir quién abrió la pantalla
        profeActual = getIntent().getStringExtra("PROFE_ACTUAL");
        if (profeActual == null) profeActual = "Paulo";

        // Enlazar vistas de diseño
        spinnerClientes = findViewById(R.id.spinnerClientesRenovar);
        cbKickboxing = findViewById(R.id.cbKickboxingRenovar);
        cbBoxeo = findViewById(R.id.cbBoxeoRenovar);
        cbJiujitsu = findViewById(R.id.cbJiujitsuRenovar);
        cbGym = findViewById(R.id.cbGymRenovar);
        cbPersonalizado = findViewById(R.id.cbPersonalizadoRenovar);
        cbPlanEspecial = findViewById(R.id.cbPlanEspecialRenovar);
        inputMontoUnico = findViewById(R.id.inputMontoUnicoRenovar);

        // Configurar Spinner de clientes
        listaNombresClientes = new ArrayList<>();
        listaNombresClientes.add("Cargando clientes...");
        adapterClientes = new ArrayAdapter<>(this, R.layout.molde_spinner, listaNombresClientes);
        spinnerClientes.setAdapter(adapterClientes);

        obtenerClientesYCalcularVencimientos();

        Button botonRenovar = findViewById(R.id.btnRenovarAccion);
        Button botonVolver = findViewById(R.id.btnVolverDesdeRenovar);

        botonRenovar.setOnClickListener(v -> renovarCliente());
        botonVolver.setOnClickListener(v -> finish());
    }

    private void renovarCliente() {
        String cliente = spinnerClientes.getSelectedItem().toString();
        String montoFinal = inputMontoUnico.getText().toString().trim();

        // Recopilar disciplinas marcadas
        List<String> disciplinasMarcadas = new ArrayList<>();
        if (cbKickboxing.isChecked()) disciplinasMarcadas.add("Kickboxing");
        if (cbBoxeo.isChecked()) disciplinasMarcadas.add("Boxeo");
        if (cbJiujitsu.isChecked()) disciplinasMarcadas.add("Jiujitsu");
        if (cbGym.isChecked()) disciplinasMarcadas.add("Gym");
        if (cbPersonalizado.isChecked()) disciplinasMarcadas.add("Personalizado");
        if (cbPlanEspecial.isChecked()) disciplinasMarcadas.add("Plan Especial");

        // Validaciones
        if (cliente.contains("Cargando") || cliente.contains("Selecciona")) {
            Toast.makeText(this, "Por favor selecciona un cliente de la lista", Toast.LENGTH_SHORT).show();
            return;
        }
        if (disciplinasMarcadas.isEmpty()) {
            Toast.makeText(this, "¡Debes seleccionar al menos una disciplina!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (montoFinal.isEmpty()) {
            Toast.makeText(this, "¡Falta ingresar el valor de la renovación!", Toast.LENGTH_SHORT).show();
            return;
        }

        String disciplinaFinal = TextUtils.join(" + ", disciplinasMarcadas);
        renovarClienteEnGoogle(cliente, disciplinaFinal, montoFinal);
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
                        boolean esAdmin = profeActual.equalsIgnoreCase("Paulo");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject cliente = jsonArray.getJSONObject(i);

                            // FILTRO DE PRIVACIDAD
                            String profesorCliente = cliente.optString("profesor", "Paulo");
                            if (!esAdmin && !profesorCliente.equalsIgnoreCase(profeActual)) {
                                continue;
                            }

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
                            tvCero.setText("✅ Todos tus clientes están al día.");
                            tvCero.setTextColor(Color.GREEN);
                            contenedorVencidos.addView(tvCero);
                        }

                    } catch (Exception e) {
                        Toast.makeText(RenovarActivity.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(RenovarActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show());

        peticionGet.setRetryPolicy(new DefaultRetryPolicy(
                15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
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
                params.put("profesor", profeActual);
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