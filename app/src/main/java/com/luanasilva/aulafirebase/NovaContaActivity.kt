package com.luanasilva.aulafirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luanasilva.aulafirebase.databinding.ActivityNovaContaBinding

class NovaContaActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityNovaContaBinding.inflate(layoutInflater)
    }
    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }
    private val bancoDados by lazy {
        FirebaseFirestore.getInstance()
    }

    companion object {
        const val COLECAO_USUARIOS = "usuarios"
        const val LOG_FIREBASE = "log_firebase"
    }


    /*
   SENHAS E EMAILS DE TESTE
       email = "luana.silva.11@hotmail.com"
       senha = "123ls@lb"
    */



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.btnCriarContaFirebase.setOnClickListener {
            cadastroUsuario()
        }
    }


    private fun cadastroUsuario() {
        //Dados digitarios pelo usuario
        val nome = binding.editNovoNome.text.toString()
        val email = binding.editNovoEmail.text.toString()
        val idade = binding.editNovoIdade.text.toString()
        val senha = binding.editNovoSenha.text.toString()

        try {

            if (nome.isEmpty()) {
                binding.editNovoNomeLayout.error = "Nome não pode estar vazio"
                throw Exception("Preencha seu nome!")
            } else {
                binding.editNovoNomeLayout.error = null
            }

            if (email.isEmpty()) {
                binding.editNovoEmailLayout.error = "Email não pode estar vazio"
                throw Exception("Preencha seu e-mail!")
            } else {
                binding.editNovoEmailLayout.error = null
            }

            if (idade.isEmpty()) {
                binding.editNovaIdadeLayout.error = "Idade não pode estar vazia"
                throw Exception("Preencha sua idade!")
            } else {
                binding.editNovaIdadeLayout.error = null
            }

            if (senha.isEmpty()) {
                binding.editNovaSenhaLayout.error = "Senha não pode estar vazia"
                throw Exception("Preencha sua senha!")
            } else {
                binding.editNovaSenhaLayout.error = null
            }


            // Código para continuar se todos os campos estiverem preenchidos
        } catch (error: Exception) {
            // Lida com o erro, por exemplo, exibir uma mensagem de log
            println("Erro: ${error.message}")
        }

        //Tela de cadastro do seu app

        autenticacao.createUserWithEmailAndPassword(
            email,
            senha
        ).addOnSuccessListener { authResult ->

            val email = authResult.user?.email
            val idUsuario = authResult.user?.uid

            //Salvar mais dados do usuario
            salvarDadosUsuario(nome, idade)
            exibirMensagem("Usuario $nome cadastrado com sucesso")

            startActivity(Intent(this, PrincipalActivity::class.java))

        }.addOnFailureListener { exception ->
            val mensagemErro = exception.message
            exibirMensagem("Erro:  - $mensagemErro")
            exception.printStackTrace()
            Log.i("AulaFirebase","Erro ${exception.message}")
        }

    }

    private fun salvarDadosUsuario(nome:String, idade:String) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null) {
            val dados = mapOf(
                "nome" to nome,
                "idade" to idade
            )

            bancoDados.collection(COLECAO_USUARIOS)
                .document(idUsuarioLogado)
                .set(dados)
                .addOnSuccessListener { exibirMensagem("Usuario salvo com sucesso") }
                .addOnFailureListener {exception->
                    exibirMensagem("Falha em salvar usuario")
                    exception.printStackTrace()
                    Log.i("log_firebase","Erro ${exception.message}")
                }
        }


    }

    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}