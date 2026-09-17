package com.example.gymkratos;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AvisosActivity extends AppCompatActivity {

    private Spinner spinnerClientes;
    private ArrayList<String> listaNombres;
    private ArrayAdapter<String> adaptador;

    private HashMap<String, String> mapaTelefonos;
    private HashMap<String, String> mapaVencimientos;

    // SEGURIDAD: Enlace oculto para GitHub
    private String urlAPI = "URL_PRIVADA_POR_SEGURIDAD";

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

        spinnerClientes = findViewById(R.id.spinnerClientesAvisos);
        EditText inputMensaje = findViewById(R.id.inputMensajeWsp);
        Button btnEnviar = findViewById(R.id.btnEnviarWsp);
        Button btnVolver = findViewById(R.id.btnVolverAvisos);

        listaNombres = new ArrayList<>();
        mapaTelefonos = new HashMap<>();
        mapaVencimientos = new HashMap<>();

        listaNombres.add("Cargando clientes...");
        adaptador = new ArrayAdapter<>(this, R.layout.molde_spinner, listaNombres);
        spinnerClientes.setAdapter(adaptador);

        cargarClientesParaWsp();

        spinnerClientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String nombreElegido = listaNombres.get(position);
                if (!nombreElegido.contains("Selecciona") && !nombreElegido.contains("Cargando")) {

                    String fechaVencimiento = mapaVencimientos.getOrDefault(nombreElegido, "fecha desconocida");

                    String mensajeAuto = "¡Hola " + nombreElegido + "! Te escribimos de Gym Kratos. Tu membresía venció el "
                            + fechaVencimiento + ". ¡Te esperamos para renovar y seguir entrenando con todo!";
                    inputMensaje.setText(mensajeAuto);
                } else {
                    inputMensaje.setText("");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnEnviar.setOnClickListener(v -> {
            String clienteElegido = spinnerClientes.getSelectedItem().toString();
            String mensaje = inputMensaje.getText().toString();

            if (clienteElegido.contains("Selecciona") || clienteElegido.contains("Cargando")) {
                Toast.makeText(this, "Selecciona un cliente primero", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mensaje.isEmpty()) {
                Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show();
                return;
            }

            String telefonoStr = mapaTelefonos.get(clienteElegido);

            if (telefonoStr != null) {
                telefonoStr = telefonoStr.replace("+", "");
                try {
                    String link = "https://api.whatsapp.com/send?phone=" + telefonoStr + "&text=" + URLEncoder.encode(mensaje, "UTF-8");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(link));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Error al abrir WhatsApp", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarClientesParaWsp() {
        LinearLayout contenedorVencidos = findViewById(R.id.contenedorVencidosAvisos);

        StringRequest peticionGet = new StringRequest(Request.Method.GET, urlAPI,
                response -> {
                    try {
                        listaNombres.clear();
                        mapaTelefonos.clear();
                        mapaVencimientos.clear();
                        contenedorVencidos.removeAllViews();
                        listaNombres.add("Selecciona un cliente...");

                        JSONArray jsonArray = new JSONArray(response);
                        SimpleDateFormat sdfGoogle = new SimpleDateFormat("yyyy-MM-dd");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject cliente = jsonArray.getJSONObject(i);
                            String nombre = cliente.getString("nombre");
                            String telefono = cliente.getString("telefono");
                            String fechaCruda = cliente.getString("fecha");

                            listaNombres.add(nombre);
                            mapaTelefonos.put(nombre, telefono);

                            if (fechaCruda.contains("T")) {
                                String soloFecha = fechaCruda.split("T")[0];
                                Date fechaPago = sdfGoogle.parse(soloFecha);

                                Calendar calendario = Calendar.getInstance();
                                calendario.setTime(fechaPago);
                                calendario.add(Calendar.DAY_OF_YEAR, 30);
                                String fechaExactaVencimiento = sdfGoogle.format(calendario.getTime());

                                mapaVencimientos.put(nombre, fechaExactaVencimiento);

                                Date hoy = new Date();
                                long diferenciaMilisegundos = hoy.getTime() - fechaPago.getTime();
                                long diasPasados = diferenciaMilisegundos / (1000 * 60 * 60 * 24);

                                if (diasPasados >= 30) {
                                    crearTarjetaVencido(contenedorVencidos, nombre, diasPasados);
                                }
                            }
                        }

                        adaptador.notifyDataSetChanged();

                        if (contenedorVencidos.getChildCount() == 0) {
                            TextView tvCero = new TextView(this);
                            tvCero.setText("✅ Nadie está atrasado con sus pagos.");
                            tvCero.setTextColor(Color.GREEN);
                            contenedorVencidos.addView(tvCero);
                        }

                    } catch (Exception e) {
                        Toast.makeText(AvisosActivity.this, "Error procesando clientes", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(AvisosActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show());

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
            int posicion = adaptador.getPosition(nombre);
            if(posicion >= 0) {
                spinnerClientes.setSelection(posicion);
                Toast.makeText(this, "Cliente seleccionado para cobrar ☝️", Toast.LENGTH_SHORT).show();
            }
        });

        contenedor.addView(tarjeta);
    }
}