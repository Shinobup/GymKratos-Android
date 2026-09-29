package com.example.gymkratos;

import android.graphics.Color;
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

import com.android.volley.DefaultRetryPolicy;
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

    private String urlAPI = "https://script.google.com/macros/s/AKfycbxDV4ogyyFwqsUNrvhoy7O0gTJgpZTFx6_Rn3N4WIMEMgWLKkI10AimFQdPgBfQCI7o/exec";
    private Button btnReconectar;

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
        btnReconectar = findViewById(R.id.btnReconectarFinanzas);

        btnVolver.setOnClickListener(v -> finish());
        btnReconectar.setOnClickListener(v -> calcularFinanzas());

        calcularFinanzas();
    }

    private void calcularFinanzas() {
        TextView tvEstadoCarga = findViewById(R.id.tvEstadoCarga);
        TextView tvTotalMes = findViewById(R.id.tvTotalMes);
        LinearLayout contenedorDesglose = findViewById(R.id.contenedorDesglose);

        tvEstadoCarga.setVisibility(View.VISIBLE);
        tvEstadoCarga.setText("⏳ Calculando ingresos...");
        tvEstadoCarga.setTextColor(Color.parseColor("#90CAF9"));
        btnReconectar.setVisibility(View.GONE);
        contenedorDesglose.removeAllViews();
        tvTotalMes.setText("$ 0");

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
                        btnReconectar.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    tvEstadoCarga.setText("❌ Sin conexión");
                    tvEstadoCarga.setTextColor(Color.RED);
                    btnReconectar.setVisibility(View.VISIBLE);
                });

        peticionGet.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        RequestQueue cola = Volley.newRequestQueue(this);
        cola.add(peticionGet);
    }
}


// PIENSA EN LA TRAZABILIDAD!!

// PIENSA EN LA TRAZABILIDAD!!

// PERFILES DE INGRESO, ASOCIADO AL RUT, NOMBRE, BLABLABAL OJO ACAAA!!

// OJO CON CLIENTES, LA DUPLICACION!

// PONER OTRO BOTON DONDE ESTE EL CHECK, PARA PROFE ADMI O PROFE PERSONALIZADO

// INGRESO LIBRE, PARA LA PARTE CORRESPONDIENTE AL GYM

// COMO EL PLAN ESPECIAL!!!, PODRIA SER OTRO PORCENTAJE

// OJO CON EL PLAN ESPECIAL, QUE DIGA QUE HACE, HACERLO CON CHECK, CAMBIAR EL DESPLEGABLE POR CHECK!

// PENSAR EN UNA BASE DE DATOS REAL!, PARA QUE SEA SUSTENTABLE Y TRAZABLES

// PENSAR QUE SE PUEDE DESCARGAR EL EXCEL, GUARDAR COMO UNA BD RUDIMENTARIA XD



// PORCENTAJE DE DESCUENTO!!!!!!!!!!!!!!! 5% 10%

// PORCENTAJE DE DESCUENTO!!!!!!!!!!!!!!! 5% 10%

// PORCENTAJE DE DESCUENTO!!!!!!!!!!!!!!! 5% 10%

// PORCENTAJE DE DESCUENTO!!!!!!!!!!!!!!! 5% 10%

