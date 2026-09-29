package com.example.gymkratos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private String profeActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Recibir quién inició sesión desde el Login
        profeActual = getIntent().getStringExtra("PROFE_ACTUAL");
        if (profeActual == null) profeActual = "Invitado";

        Toast.makeText(this, "Sesión activa: " + profeActual, Toast.LENGTH_SHORT).show();

        // 2. Enlazar con los ID EXACTOS de tu activity_main.xml
        Button btnRegistro = findViewById(R.id.idRegistrar);
        Button btnVerClientes = findViewById(R.id.idVer);
        Button btnRenovar = findViewById(R.id.idRenovar);
        Button btnAvisos = findViewById(R.id.idAvisos);
        Button btnEliminar = findViewById(R.id.idEliminar);
        Button btnFinanzas = findViewById(R.id.idFinanzas);
        Button btnCerrarSesion = findViewById(R.id.idCerrarSesion); // NUEVO BOTÓN

        // 3. Restricción de seguridad: Si no es Paulo, ocultamos el botón de Finanzas
        boolean esAdmin = profeActual.equalsIgnoreCase("Paulo");
        if (!esAdmin) {
            if (btnFinanzas != null) {
                btnFinanzas.setVisibility(View.GONE);
            }
        }

        // 4. Configurar clics de navegación
        if (btnRegistro != null) {
            btnRegistro.setOnClickListener(v -> abrirPantalla(RegistroActivity.class));
        }
        if (btnVerClientes != null) {
            btnVerClientes.setOnClickListener(v -> abrirPantalla(VerClientesActivity.class));
        }
        if (btnRenovar != null) {
            btnRenovar.setOnClickListener(v -> abrirPantalla(RenovarActivity.class));
        }
        if (btnAvisos != null) {
            btnAvisos.setOnClickListener(v -> abrirPantalla(AvisosActivity.class));
        }
        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> abrirPantalla(EliminarActivity.class));
        }
        if (btnFinanzas != null) {
            btnFinanzas.setOnClickListener(v -> abrirPantalla(FinanzasActivity.class));
        }

        // Clic para CERRAR SESIÓN
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> {
                // Te devuelve a la pantalla de Login (cambia LoginActivity.class si tu pantalla principal se llama diferente)
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);

                // Estas banderas borran el historial para que si le dan al botón físico de "Atrás" del celular no vuelvan al menú principal
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Cierra esta activity
            });
        }
    }

    private void abrirPantalla(Class<?> claseDestino) {
        Intent intent = new Intent(MainActivity.this, claseDestino);
        intent.putExtra("PROFE_ACTUAL", profeActual);
        startActivity(intent);
    }
}