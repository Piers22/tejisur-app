package com.tejisur.app.produccion.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.tejisur.app.R
import com.tejisur.app.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarListeners()
    }

    private fun configurarListeners() = with(binding) {
        btnIniciarSesion.setOnClickListener { intentarLogin() }

        etContrasena.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                intentarLogin()
                true
            } else false
        }

        etUsuario.doAfterTextChanged { etUsuario.error = null }
        etContrasena.doAfterTextChanged { etContrasena.error = null }
    }

    private fun intentarLogin() {
        val usuario = binding.etUsuario.text.toString().trim()
        val contrasena = binding.etContrasena.text.toString()

        if (!camposValidos(usuario, contrasena)) return

        if (autenticar(usuario, contrasena)) {
            Toast.makeText(requireContext(), getString(R.string.login_exito, usuario), Toast.LENGTH_SHORT).show()
            
            // Navegación conectada hacia el Registro de Consumo (HU01)
            (activity as? com.tejisur.app.MainActivity)?.mostrarSeccion(com.tejisur.app.MainActivity.Seccion.CONSUMO)
                ?: run {
                    parentFragmentManager.commit {
                        replace(android.R.id.content, RegistroConsumoFragment())
                    }
                }
        } else {
            binding.etContrasena.text?.clear()
            Toast.makeText(requireContext(), R.string.login_error_credenciales, Toast.LENGTH_SHORT).show()
        }
    }

    private fun camposValidos(usuario: String, contrasena: String): Boolean {
        with(binding) {
            etUsuario.error = if (usuario.isEmpty()) getString(R.string.login_error_usuario_requerido) else null
            etContrasena.error = if (contrasena.isEmpty()) getString(R.string.login_error_contrasena_requerida) else null

            when {
                usuario.isEmpty() -> etUsuario.requestFocus()
                contrasena.isEmpty() -> etContrasena.requestFocus()
            }
        }
        return usuario.isNotEmpty() && contrasena.isNotEmpty()
    }

    private fun autenticar(usuario: String, contrasena: String): Boolean =
        usuario == USUARIO_DEMO && contrasena == CONTRASENA_DEMO

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val USUARIO_DEMO = "operario01"
        const val CONTRASENA_DEMO = "123456"
    }
}