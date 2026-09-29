package com.example.gymkratos;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VerClientesActivity extends AppCompatActivity {

    private Button btnReconectar;
    private Switch switchVerLista;
    private TextView tvEstado;
    private LinearLayout contenedorClientes;

    private String profeActual = "Paulo";

    // Memoria caché para no tener que descargar de internet cada vez que tocan el interruptor
    private JSONArray clientesGlobales = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ver_clientes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profeActual = getIntent().getStringExtra("PROFE_ACTUAL");
        if (profeActual == null) profeActual = "Paulo";

        // Enlazar vistas
        btnReconectar = findViewById(R.id.btnReconectar);
        switchVerLista = findViewById(R.id.switchVerLista);
        tvEstado = findViewById(R.id.tvEstado);
        contenedorClientes = findViewById(R.id.contenedorClientes);

        Button botonVolver = findViewById(R.id.btnVolverDesdeLista);
        botonVolver.setOnClickListener(v -> finish());
        btnReconectar.setOnClickListener(v -> cargarClientes());

        // ----------------------------------------------------
        // LÓGICA DEL INTERRUPTOR
        // ----------------------------------------------------
        if (profeActual.equalsIgnoreCase("Paulo")) {
            switchVerLista.setVisibility(View.GONE); // Paulo ve todo siempre
        } else {
            switchVerLista.setVisibility(View.VISIBLE);
            switchVerLista.setChecked(false);

            switchVerLista.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    switchVerLista.setText("Modo: Lista General (Gimnasio)");
                } else {
                    switchVerLista.setText("Modo: Mis Clientes (Personalizados)");
                }
                // Dibuja la lista al instante usando la memoria caché
                mostrarClientesEnPantalla();
            });
        }

        // Descargamos los datos al abrir la pantalla
        cargarClientes();
    }

    private void cargarClientes() {
        tvEstado.setVisibility(View.VISIBLE);
        tvEstado.setText("⏳ Descargando base de datos...");
        tvEstado.setTextColor(Color.parseColor("#AAAAAA"));
        btnReconectar.setVisibility(View.GONE);
        contenedorClientes.removeAllViews();

        String urlAPI = "PONER_AQUI_TU_ENLACE_DE_GOOGLE_APPS_SCRIPT";

        StringRequest peticion = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // Guardamos todos los datos en la memoria del teléfono
                        clientesGlobales = new JSONArray(response);
                        mostrarClientesEnPantalla();
                    } catch (Exception e) {
                        tvEstado.setText("❌ Error al procesar los datos.");
                        tvEstado.setTextColor(Color.parseColor("#B71C1C"));
                        btnReconectar.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    tvEstado.setText("❌ Error de conexión. Revisa tu internet.");
                    tvEstado.setTextColor(Color.parseColor("#B71C1C"));
                    btnReconectar.setVisibility(View.VISIBLE);
                });

        peticion.setRetryPolicy(new DefaultRetryPolicy(
                15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        RequestQueue cola = Volley.newRequestQueue(this);
        cola.add(peticion);
    }

    private void mostrarClientesEnPantalla() {
        contenedorClientes.removeAllViews();
        tvEstado.setVisibility(View.GONE);

        if (clientesGlobales.length() == 0) {
            tvEstado.setText("No hay clientes registrados aún.");
            tvEstado.setVisibility(View.VISIBLE);
            return;
        }

        boolean esAdmin = profeActual.equalsIgnoreCase("Paulo");
        String profesorObjetivo = profeActual;

        // Si Rafa/Nico activan el interruptor, el objetivo de búsqueda es la lista de Paulo
        if (!esAdmin && switchVerLista.isChecked()) {
            profesorObjetivo = "Paulo";
        }

        List<JSONObject> listaClientesFiltrados = new ArrayList<>();

        for (int i = 0; i < clientesGlobales.length(); i++) {
            try {
                JSONObject cliente = clientesGlobales.getJSONObject(i);

                // FILTRO DE PRIVACIDAD
                String profesorCliente = cliente.optString("profesor", "Paulo");

                if (!esAdmin && !profesorCliente.equalsIgnoreCase(profesorObjetivo)) {
                    continue; // Se salta este cliente si no coincide con el objetivo actual
                }

                listaClientesFiltrados.add(cliente);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (listaClientesFiltrados.isEmpty()) {
            tvEstado.setText("No hay clientes en esta lista.");
            tvEstado.setVisibility(View.VISIBLE);
            return;
        }

        // ORDENAR ALFABÉTICAMENTE
        Collections.sort(listaClientesFiltrados, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject c1, JSONObject c2) {
                try {
                    String nombre1 = c1.getString("nombre").toLowerCase();
                    String nombre2 = c2.getString("nombre").toLowerCase();
                    return nombre1.compareTo(nombre2);
                } catch (Exception e) {
                    return 0;
                }
            }
        });

        // DIBUJAR TARJETAS
        for (JSONObject cliente : listaClientesFiltrados) {
            try {
                String nombre = cliente.getString("nombre");
                String telefono = cliente.getString("telefono");
                String disciplina = cliente.getString("disciplina");
                String monto = cliente.getString("monto");
                String estado = cliente.getString("estado");
                String fechaCruda = cliente.getString("fecha");
                String profesorCliente = cliente.optString("profesor", "Paulo");

                if (!telefono.startsWith("+")) {
                    telefono = "+" + telefono;
                }

                String fechaLimpia = fechaCruda;
                if (fechaCruda.contains("T")) {
                    String soloFecha = fechaCruda.split("T")[0];
                    String[] partes = soloFecha.split("-");
                    if (partes.length == 3) {
                        fechaLimpia = partes[2] + "/" + partes[1] + "/" + partes[0];
                    }
                }

                LinearLayout tarjeta = new LinearLayout(this);
                tarjeta.setOrientation(LinearLayout.VERTICAL);
                tarjeta.setPadding(50, 40, 50, 40);

                GradientDrawable fondoTarjeta = new GradientDrawable();
                fondoTarjeta.setColor(Color.parseColor("#111111"));
                fondoTarjeta.setCornerRadius(24f);
                fondoTarjeta.setStroke(3, Color.parseColor("#B71C1C"));
                tarjeta.setBackground(fondoTarjeta);

                LinearLayout.LayoutParams paramsTarjeta = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                );
                paramsTarjeta.setMargins(0, 0, 0, 32);
                tarjeta.setLayoutParams(paramsTarjeta);

                TextView tvNombre = new TextView(this);
                tvNombre.setText(nombre.toUpperCase());
                tvNombre.setTextColor(Color.WHITE);
                tvNombre.setTextSize(18);
                tvNombre.setTypeface(null, Typeface.BOLD);
                tvNombre.setPadding(0, 0, 0, 16);

                TextView tvDetalles = new TextView(this);

                // CONSTRUIR EL TEXTO PASO A PASO
                String textoDetalles = "📅 Ingreso: " + fechaLimpia +
                        "\n📱 " + telefono +
                        "\n🥊 " + disciplina;

                // REGLA DE ORO: Si NO es Nico, le mostramos el dinero
                if (!profeActual.equalsIgnoreCase("Nico")) {
                    textoDetalles += "\n💰 $" + monto;
                }

                textoDetalles += "\n⚡ Estado: " + estado;

                if (esAdmin) {
                    textoDetalles += "\n👨‍🏫 Perfil: " + profesorCliente;
                }

                tvDetalles.setText(textoDetalles);
                tvDetalles.setTextColor(Color.parseColor("#CCCCCC"));
                tvDetalles.setTextSize(14);
                tvDetalles.setLineSpacing(0, 1.2f);

                tarjeta.addView(tvNombre);
                tarjeta.addView(tvDetalles);
                contenedorClientes.addView(tarjeta);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}