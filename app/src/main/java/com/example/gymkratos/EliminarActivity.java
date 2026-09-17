package com.example.gymkratos;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EliminarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_eliminar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Configuramos la lista temporal de clientes
        Spinner spinnerClientes = findViewById(R.id.spinnerClientesEliminar);
        String[] listaClientesPrueba = {"Selecciona un cliente...", "Aliro Cuevas", "Julián", "María Gómez"};

        ArrayAdapter<String> adapterClientes = new ArrayAdapter<>(
                this, R.layout.molde_spinner, listaClientesPrueba);
        spinnerClientes.setAdapter(adapterClientes);

        // 2. Conectamos los botones
        Button botonEliminar = findViewById(R.id.btnEliminarAccion);
        Button botonVolver = findViewById(R.id.btnVolverDesdeEliminar);

        // 3. Acción del botón eliminar
        botonEliminar.setOnClickListener(v -> {
            String clienteSeleccionado = spinnerClientes.getSelectedItem().toString();

            if (clienteSeleccionado.contains("Selecciona")) {
                Toast.makeText(EliminarActivity.this, "Por favor selecciona un cliente a eliminar", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EliminarActivity.this, "🚨 " + clienteSeleccionado + " ha sido eliminado del sistema", Toast.LENGTH_LONG).show();
            }
        });

        // 4. Acción del botón volver
        botonVolver.setOnClickListener(v -> {
            finish();
        });
    }
}