package com.tejisur.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.tejisur.app.produccion.ui.RegistroConsumoFragment
import com.tejisur.app.produccion.ui.RegistroMermaFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargamos el Fragment directamente en el contenedor raíz para pruebas rápidas
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                // AQUÍ CARGAS LA HU01 (Consumo)
                //replace(android.R.id.content, RegistroConsumoFragment())

                // CUANDO QUIERAS PROBAR LA HU02 (Merma), COMENTA LA LÍNEA DE ARRIBA
                // Y DESCOMENTA LA LÍNEA DE ABAJO:
                 replace(android.R.id.content, RegistroMermaFragment())
            }
        }
    }
}