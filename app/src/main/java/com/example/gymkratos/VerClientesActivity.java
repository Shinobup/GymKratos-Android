package com.example.gymkratos;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class VerClientesActivity extends AppCompatActivity {

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

        Button botonVolver = findViewById(R.id.btnVolverDesdeLista);
        botonVolver.setOnClickListener(v -> finish());

        cargarClientes();
    }

    private void cargarClientes() {
        TextView tvEstado = findViewById(R.id.tvEstado);
        LinearLayout contenedor = findViewById(R.id.contenedorClientes);

        String url = "https://script.google.com/macros/s/AKfycbxDV4ogyyFwqsUNrvhoy7O0gTJgpZTFx6_Rn3N4WIMEMgWLKkI10AimFQdPgBfQCI7o/exec";

        StringRequest peticion = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        tvEstado.setVisibility(View.GONE);
                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() == 0) {
                            tvEstado.setText("No hay clientes registrados aún.");
                            tvEstado.setVisibility(View.VISIBLE);
                            return;
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject cliente = jsonArray.getJSONObject(i);

                            String nombre = cliente.getString("nombre");
                            String telefono = cliente.getString("telefono");

                            // AHORA LEEMOS LAS DOS COLUMNAS NUEVAS
                            String disciplina = cliente.getString("disciplina");
                            String monto = cliente.getString("monto");

                            String estado = cliente.getString("estado");
                            String fechaCruda = cliente.getString("fecha");

                            // Arreglar Teléfono
                            if (!telefono.startsWith("+")) {
                                telefono = "+" + telefono;
                            }

                            // Limpiar Fecha
                            String fechaLimpia = fechaCruda;
                            if (fechaCruda.contains("T")) {
                                String soloFecha = fechaCruda.split("T")[0];
                                String[] partes = soloFecha.split("-");
                                if (partes.length == 3) {
                                    fechaLimpia = partes[2] + "/" + partes[1] + "/" + partes[0];
                                }
                            }

                            // Crear Tarjeta Visual
                            LinearLayout tarjeta = new LinearLayout(this);
                            tarjeta.setOrientation(LinearLayout.VERTICAL);
                            tarjeta.setBackgroundColor(Color.parseColor("#1A1A1A"));
                            tarjeta.setPadding(40, 40, 40, 40);

                            LinearLayout.LayoutParams paramsTarjeta = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            paramsTarjeta.setMargins(0, 0, 0, 24);
                            tarjeta.setLayoutParams(paramsTarjeta);

                            // Nombre
                            TextView tvNombre = new TextView(this);
                            tvNombre.setText("👤 " + nombre);
                            tvNombre.setTextColor(Color.WHITE);
                            tvNombre.setTextSize(20);
                            tvNombre.setTypeface(null, Typeface.BOLD);

                            // Detalles (AHORA INCLUYE DISCIPLINA Y MONTO)
                            TextView tvDetalles = new TextView(this);
                            String textoDetalles = "📅 Ingreso: " + fechaLimpia +
                                    "\n📱 " + telefono +
                                    "\n🥊 " + disciplina + " (💰 $" + monto + ")" +
                                    "\n⚡ Estado: " + estado;

                            tvDetalles.setText(textoDetalles);
                            tvDetalles.setTextColor(Color.parseColor("#CCCCCC"));
                            tvDetalles.setTextSize(15);
                            tvDetalles.setPadding(0, 12, 0, 0);

                            tarjeta.addView(tvNombre);
                            tarjeta.addView(tvDetalles);

                            contenedor.addView(tarjeta);
                        }

                    } catch (Exception e) {
                        tvEstado.setText("❌ Error al procesar los datos.");
                        tvEstado.setTextColor(Color.RED);
                        e.printStackTrace();
                    }
                },
                error -> {
                    tvEstado.setText("❌ Error de conexión. Revisa tu internet.");
                    tvEstado.setTextColor(Color.RED);
                });

        RequestQueue cola = Volley.newRequestQueue(this);
        cola.add(peticion);
    }
}