package com.example.gymkratos;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
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

    private CheckBox cbKickboxing, cbBoxeo, cbJiujitsu, cbGym, cbPersonalizado, cbPlanEspecial;
    private EditText inputNombre, inputTelefono, inputMontoUnico;
    private Switch switchDestino;
    private LinearLayout grupoDisciplinasGenerales;

    private String profeActual = "Paulo";

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

        // Recibir al profesor de la sesión actual
        profeActual = getIntent().getStringExtra("PROFE_ACTUAL");
        if (profeActual == null) profeActual = "Paulo";

        // Enlazar vistas
        inputNombre = findViewById(R.id.inputNombre);
        inputTelefono = findViewById(R.id.inputTelefono);
        inputMontoUnico = findViewById(R.id.inputMontoUnico);

        cbKickboxing = findViewById(R.id.cbKickboxing);
        cbBoxeo = findViewById(R.id.cbBoxeo);
        cbJiujitsu = findViewById(R.id.cbJiujitsu);
        cbGym = findViewById(R.id.cbGym);
        cbPersonalizado = findViewById(R.id.cbPersonalizado);
        cbPlanEspecial = findViewById(R.id.cbPlanEspecial);

        switchDestino = findViewById(R.id.switchDestino);
        grupoDisciplinasGenerales = findViewById(R.id.grupoDisciplinasGenerales);

        // ----------------------------------------------------
        // LÓGICA DEL INTERRUPTOR (OCULTAR COSAS)
        // ----------------------------------------------------
        if (profeActual.equalsIgnoreCase("Paulo")) {
            // Paulo es admin, ve todo siempre
            switchDestino.setVisibility(View.GONE);
            grupoDisciplinasGenerales.setVisibility(View.VISIBLE);
        } else {
            // Es Rafa o Nico: configuramos el modo cajero
            switchDestino.setVisibility(View.VISIBLE);
            switchDestino.setChecked(false);
            grupoDisciplinasGenerales.setVisibility(View.GONE); // Ocultamos las disciplinas por defecto

            switchDestino.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    switchDestino.setText("Modo: Lista General (Gimnasio)");
                    grupoDisciplinasGenerales.setVisibility(View.VISIBLE);
                } else {
                    switchDestino.setText("Modo: Mi Lista (Personalizados)");
                    grupoDisciplinasGenerales.setVisibility(View.GONE);
                    // Si vuelven a "Mi lista", desmarcamos las disciplinas que no son de ellos por seguridad
                    cbKickboxing.setChecked(false);
                    cbBoxeo.setChecked(false);
                    cbJiujitsu.setChecked(false);
                    cbGym.setChecked(false);
                    cbPlanEspecial.setChecked(false);
                }
            });
        }

        Button botonGuardar = findViewById(R.id.btnGuardar);
        Button botonVolver = findViewById(R.id.btnVolver);

        botonGuardar.setOnClickListener(v -> guardarCliente());
        botonVolver.setOnClickListener(v -> finish());
    }

    private void guardarCliente() {
        String nombre = inputNombre.getText().toString().trim();
        String telefonoCrudo = inputTelefono.getText().toString().trim().replace(" ", "");
        String montoFinal = inputMontoUnico.getText().toString().trim();

        // Recopilar disciplinas marcadas
        List<String> disciplinasMarcadas = new ArrayList<>();
        if (cbKickboxing.isChecked()) disciplinasMarcadas.add("Kickboxing");
        if (cbBoxeo.isChecked()) disciplinasMarcadas.add("Boxeo");
        if (cbJiujitsu.isChecked()) disciplinasMarcadas.add("Jiujitsu");
        if (cbGym.isChecked()) disciplinasMarcadas.add("Gym");
        if (cbPersonalizado.isChecked()) disciplinasMarcadas.add("Personalizado");
        if (cbPlanEspecial.isChecked()) disciplinasMarcadas.add("Plan Especial");

        if (nombre.isEmpty() || telefonoCrudo.isEmpty()) {
            Toast.makeText(this, "¡Faltan datos del cliente!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!telefonoCrudo.startsWith("+569") || telefonoCrudo.length() != 12) {
            Toast.makeText(this, "El teléfono debe tener 8 número después del +569.", Toast.LENGTH_LONG).show();
            return;
        }
        if (disciplinasMarcadas.isEmpty()) {
            Toast.makeText(this, "¡Debes seleccionar al menos una disciplina/plan!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (montoFinal.isEmpty()) {
            Toast.makeText(this, "¡Falta ingresar el valor de la mensualidad!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ----------------------------------------------------
        // DECIDIR A QUÉ PESTAÑA DE EXCEL SE VA
        // ----------------------------------------------------
        String profesorDestino = profeActual;
        // Si no es Paulo, y el interruptor está activado, el dinero va a la lista de Paulo
        if (!profeActual.equalsIgnoreCase("Paulo") && switchDestino.isChecked()) {
            profesorDestino = "Paulo";
        }

        String disciplinaFinal = TextUtils.join(" + ", disciplinasMarcadas);
        String fechaActual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        String urlAPI = "PONER_AQUI_TU_ENLACE_DE_GOOGLE_APPS_SCRIPT";

        Toast.makeText(this, "Guardando cliente...", Toast.LENGTH_SHORT).show();

        // Es necesario declarar una variable 'final' para usarla dentro de la petición
        final String destinoFinal = profesorDestino;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(RegistroActivity.this, "¡Registrado con éxito en la lista de " + destinoFinal + "!", Toast.LENGTH_LONG).show();
                    // Limpiar el formulario
                    inputNombre.setText("");
                    inputTelefono.setText("");
                    inputMontoUnico.setText("");
                    cbKickboxing.setChecked(false);
                    cbBoxeo.setChecked(false);
                    cbJiujitsu.setChecked(false);
                    cbGym.setChecked(false);
                    cbPersonalizado.setChecked(false);
                    cbPlanEspecial.setChecked(false);
                },
                error -> Toast.makeText(RegistroActivity.this, "Error de conexión. Intenta nuevamente.", Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fecha", fechaActual);
                params.put("nombre", nombre);
                params.put("telefono", telefonoCrudo);
                params.put("disciplina", disciplinaFinal);
                params.put("monto", montoFinal);
                params.put("estado", "Activo");
                params.put("profesor", destinoFinal); // Aquí viaja la hoja exacta donde se guardará
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}