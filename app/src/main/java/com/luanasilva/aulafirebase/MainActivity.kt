package com.luanasilva.aulafirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.luanasilva.aulafirebase.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        verificarUsuarioLogado()
    }

    private fun verificarUsuarioLogado() {
        //autenticacao.signOut()
        val usuario = autenticacao.currentUser
        if(usuario != null) {
            startActivity(Intent(this, PrincipalActivity::class.java))
        } else{
            Toast.makeText(this, "Não tem usuario logado",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnExecutar.setOnClickListener {
            //cadastroUsuario()
            logarUsuario()
        }
    }

    private fun logarUsuario() {
        //Dados udigitarios pelo usuario
        val email = "luana.silva.11@hotmail.com"
        val senha = "123ls@lb"

        //Estivesse em uma tela de login
        autenticacao.signInWithEmailAndPassword(email,senha).addOnSuccessListener {authResult ->
            binding.textResultado.text = "Sucesso ao logar usuario"
            startActivity(Intent(this, PrincipalActivity::class.java))

        }.addOnFailureListener { exception ->
            binding.textResultado.text = "Falha ap logar usuario ${exception.message}"
        }
    }

    private fun cadastroUsuario() {
        //Dados digitarios pelo usuario
        val email = "luana.silva.11@hotmail.com"
        val senha = "123ls@lb"

        //Tela de cadastro do seu app

        autenticacao.createUserWithEmailAndPassword(
            email,
            senha
        ).addOnSuccessListener { authResult ->

            val email = authResult.user?.email
            val idUsuario = authResult.user?.uid


            binding.textResultado.text = "sucesso: $idUsuario - $email"

        }.addOnFailureListener { exception ->
            val mensagemErro = exception.message
            binding.textResultado.text = "Erro:  - $mensagemErro"
        }
    }




}