package com.example.gymkratos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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

        // 1. Enlazamos los botones
        Button botonRegistrar = findViewById(R.id.idRegistrar);
        Button botonVer = findViewById(R.id.idVer);
        Button botonRenovar = findViewById(R.id.idRenovar);
        Button botonEliminar = findViewById(R.id.idEliminar);
        Button botonAvisos = findViewById(R.id.idAvisos);

        // ¡NUEVO ENLACE AL BOTÓN DE FINANZAS!
        Button botonFinanzas = findViewById(R.id.idFinanzas);

        // 2. Acciones de las pantallas (viajes reales)
        botonRegistrar.setOnClickListener(v -> {
            Intent intencion = new Intent(MainActivity.this, RegistroActivity.class);
            startActivity(intencion);
        });

        botonVer.setOnClickListener(v -> {
            Intent intencion = new Intent(MainActivity.this, VerClientesActivity.class);
            startActivity(intencion);
        });

        botonRenovar.setOnClickListener(v -> {
            Intent intencion = new Intent(MainActivity.this, RenovarActivity.class);
            startActivity(intencion);
        });

        botonEliminar.setOnClickListener(v -> {
            Intent intencion = new Intent(MainActivity.this, EliminarActivity.class);
            startActivity(intencion);
        });

        botonAvisos.setOnClickListener(v -> {
            Intent intencion = new Intent(MainActivity.this, AvisosActivity.class);
            startActivity(intencion);
        });

        // 3. Viaje a la pantalla maestra de la plata
        botonFinanzas.setOnClickListener(v -> {
            Intent intencion = new Intent(MainActivity.this, FinanzasActivity.class);
            startActivity(intencion);
        });
    }
}