package com.tejisur.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.tejisur.app.produccion.ui.RegistroConsumoFragment
import com.tejisur.app.produccion.ui.RegistroMermaFragment
import com.tejisur.app.produccion.ui.LoginFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargamos el Fragment directamente en el contenedor raíz para pruebas rápidas
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                // AQUÍ CARGAS LA HU01 (Consumo)
                //replace(android.R.id.content, RegistroConsumoFragment())

                //replace(android.R.id.content, RegistroMermaFragment())

                replace(android.R.id.content, LoginFragment())

            }
        }
    }
}