package com.example.gymkratos;

import android.graphics.Color;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FinanzasActivity extends AppCompatActivity {

    // SEGURIDAD: Enlace oculto para GitHub
    private String urlAPI = "URL_PRIVADA_POR_SEGURIDAD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_finanzas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnVolver = findViewById(R.id.btnVolverFinanzas);
        btnVolver.setOnClickListener(v -> finish());

        calcularFinanzas();
    }

    private void calcularFinanzas() {
        TextView tvEstadoCarga = findViewById(R.id.tvEstadoCarga);
        TextView tvTotalMes = findViewById(R.id.tvTotalMes);
        LinearLayout contenedorDesglose = findViewById(R.id.contenedorDesglose);

        StringRequest peticionGet = new StringRequest(Request.Method.GET, urlAPI,
                response -> {
                    try {
                        tvEstadoCarga.setVisibility(View.GONE);
                        JSONArray jsonArray = new JSONArray(response);

                        int granTotal = 0;
                        HashMap<String, Integer> sumatorias = new HashMap<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject cliente = jsonArray.getJSONObject(i);
                            String disciplina = cliente.getString("disciplina");
                            String montoString = cliente.getString("monto");

                            int montoActual = 0;
                            try {
                                montoActual = Integer.parseInt(montoString);
                            } catch (Exception e) {}

                            granTotal += montoActual;
                            int totalAcumulado = sumatorias.getOrDefault(disciplina, 0);
                            sumatorias.put(disciplina, totalAcumulado + montoActual);
                        }

                        NumberFormat formatoPlata = NumberFormat.getNumberInstance(new Locale("es", "CL"));
                        tvTotalMes.setText("$ " + formatoPlata.format(granTotal));

                        contenedorDesglose.removeAllViews();
                        for (Map.Entry<String, Integer> entry : sumatorias.entrySet()) {
                            TextView tvDesglose = new TextView(this);
                            tvDesglose.setText("▶ " + entry.getKey() + ": \n    $ " + formatoPlata.format(entry.getValue()));
                            tvDesglose.setTextColor(Color.WHITE);
                            tvDesglose.setTextSize(16);
                            tvDesglose.setPadding(0, 0, 0, 24);

                            if (entry.getKey().contains("Gym")) {
                                tvDesglose.setTextColor(Color.parseColor("#FF5252"));
                            } else if (entry.getKey().contains("Boxeo") || entry.getKey().contains("Kickboxing") || entry.getKey().contains("Jiujitsu")) {
                                tvDesglose.setTextColor(Color.parseColor("#448AFF"));
                            }

                            contenedorDesglose.addView(tvDesglose);
                        }

                    } catch (Exception e) {
                        tvEstadoCarga.setText("❌ Error al procesar datos");
                        tvEstadoCarga.setTextColor(Color.RED);
                    }
                },
                error -> {
                    tvEstadoCarga.setText("❌ Sin conexión");
                    tvEstadoCarga.setTextColor(Color.RED);
                });

        RequestQueue cola = Volley.newRequestQueue(this);
        cola.add(peticionGet);
    }
}