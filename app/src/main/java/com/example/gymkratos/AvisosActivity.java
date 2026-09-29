package com.example.gymkratos;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AvisosActivity extends AppCompatActivity {

    private String profeActual = "Paulo";
    String urlAPI = "PONER_AQUI_TU_ENLACE_DE_GOOGLE_APPS_SCRIPT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_avisos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Recibir quién abrió la pantalla
        profeActual = getIntent().getStringExtra("PROFE_ACTUAL");
        if (profeActual == null) profeActual = "Paulo";

        Button btnVolver = findViewById(R.id.btnVolverDesdeAvisos);
        btnVolver.setOnClickListener(v -> finish());

        buscarClientesPorVencer();
    }

    private void buscarClientesPorVencer() {
        TextView tvEstado = findViewById(R.id.tvEstadoAvisos);
        LinearLayout contenedor = findViewById(R.id.contenedorAvisos);

        StringRequest peticionGet = new StringRequest(Request.Method.GET, urlAPI,
                response -> {
                    try {
                        tvEstado.setVisibility(View.GONE);
                        contenedor.removeAllViews();

                        JSONArray jsonArray = new JSONArray(response);
                        SimpleDateFormat sdfGoogle = new SimpleDateFormat("yyyy-MM-dd");

                        boolean esAdmin = profeActual.equalsIgnoreCase("Paulo");
                        int contadorAvisos = 0;

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject cliente = jsonArray.getJSONObject(i);

                            // FILTRO DE PRIVACIDAD
                            String profesorCliente = cliente.optString("profesor", "Paulo");
                            if (!esAdmin && !profesorCliente.equalsIgnoreCase(profeActual)) {
                                continue;
                            }

                            String nombre = cliente.getString("nombre");
                            String telefono = cliente.getString("telefono");
                            String disciplina = cliente.getString("disciplina");
                            String fechaCruda = cliente.getString("fecha");

                            if (fechaCruda.contains("T")) {
                                String soloFecha = fechaCruda.split("T")[0];
                                Date fechaPago = sdfGoogle.parse(soloFecha);
                                Date hoy = new Date();

                                long diferenciaMilisegundos = hoy.getTime() - fechaPago.getTime();
                                long diasPasados = diferenciaMilisegundos / (1000 * 60 * 60 * 24);

                                // Si pasaron 25 días o más (faltan 5 días para cumplir el mes, o ya debe)
                                if (diasPasados >= 25) {
                                    contadorAvisos++;
                                    crearTarjetaAviso(contenedor, nombre, telefono, disciplina, diasPasados);
                                }
                            }
                        }

                        if (contadorAvisos == 0) {
                            tvEstado.setText("✅ Todos tus clientes están al día (menos de 25 días).");
                            tvEstado.setTextColor(Color.GREEN);
                            tvEstado.setVisibility(View.VISIBLE);
                        }

                    } catch (Exception e) {
                        tvEstado.setText("❌ Error al procesar los datos.");
                        tvEstado.setTextColor(Color.RED);
                        tvEstado.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    tvEstado.setText("❌ Error de conexión. Revisa tu internet.");
                    tvEstado.setTextColor(Color.RED);
                    tvEstado.setVisibility(View.VISIBLE);
                });

        // Tolerancia de 15 segundos
        peticionGet.setRetryPolicy(new DefaultRetryPolicy(
                15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        RequestQueue cola = Volley.newRequestQueue(this);
        cola.add(peticionGet);
    }

    private void crearTarjetaAviso(LinearLayout contenedor, String nombre, String telefono, String disciplina, long diasPasados) {
        // Contenedor principal de la tarjeta
        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setPadding(50, 40, 50, 40);

        // Fondo oscuro con borde rojo
        GradientDrawable fondoTarjeta = new GradientDrawable();
        fondoTarjeta.setColor(Color.parseColor("#111111"));
        fondoTarjeta.setCornerRadius(24f);
        fondoTarjeta.setStroke(3, Color.parseColor("#B71C1C"));
        tarjeta.setBackground(fondoTarjeta);

        LinearLayout.LayoutParams paramsTarjeta = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsTarjeta.setMargins(0, 0, 0, 32);
        tarjeta.setLayoutParams(paramsTarjeta);

        // Texto Nombre
        TextView tvNombre = new TextView(this);
        tvNombre.setText(nombre.toUpperCase());
        tvNombre.setTextColor(Color.WHITE);
        tvNombre.setTextSize(18);
        tvNombre.setTypeface(null, Typeface.BOLD);

        // Texto Estado (Rojo si ya venció, Amarillo si está por vencer)
        TextView tvEstado = new TextView(this);
        long diasParaVencer = 30 - diasPasados;
        if (diasParaVencer <= 0) {
            tvEstado.setText("⚠️ Vencido hace " + Math.abs(diasParaVencer) + " días");
            tvEstado.setTextColor(Color.parseColor("#E53935")); // Rojo
        } else {
            tvEstado.setText("⏳ Vence en " + diasParaVencer + " días");
            tvEstado.setTextColor(Color.parseColor("#FDD835")); // Amarillo
        }
        tvEstado.setTextSize(14);
        tvEstado.setPadding(0, 8, 0, 24);

        // Botón Enviar WhatsApp
        Button btnWhatsApp = new Button(this);
        btnWhatsApp.setText("📲 ENVIAR WHATSAPP");
        btnWhatsApp.setTextColor(Color.WHITE);
        btnWhatsApp.setBackgroundColor(Color.parseColor("#25D366")); // Verde WhatsApp oficial
        btnWhatsApp.setOnClickListener(v -> abrirWhatsApp(telefono, nombre, disciplina, diasParaVencer));

        tarjeta.addView(tvNombre);
        tarjeta.addView(tvEstado);
        tarjeta.addView(btnWhatsApp);
        contenedor.addView(tarjeta);
    }

    private void abrirWhatsApp(String telefonoCrudo, String nombre, String disciplina, long diasParaVencer) {
        try {
            // Limpiamos el número para que sea formato internacional puro: 569...
            String numeroLimpio = telefonoCrudo.replace("+", "").replace(" ", "").trim();

            // Mensaje automático personalizado
            String mensaje = "Hola " + nombre + " 🥊,\nTe escribimos de Gym Kratos. ";

            if (diasParaVencer <= 0) {
                mensaje += "Queríamos recordarte que tu mensualidad de " + disciplina + " ha vencido. ¡Te esperamos para renovar y seguir entrenando duro! 💪🔥";
            } else {
                mensaje += "Queríamos recordarte que tu mensualidad de " + disciplina + " vence en " + diasParaVencer + " días. ¡Nos vemos en el entrenamiento! 💪🔥";
            }

            // Crear el link de WhatsApp
            String url = "https://wa.me/" + numeroLimpio + "?text=" + URLEncoder.encode(mensaje, "UTF-8");

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }
}