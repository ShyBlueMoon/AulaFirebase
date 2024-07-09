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
import com.luanasilva.aulafirebase.databinding.ActivityPrincipalBinding

class PrincipalActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPrincipalBinding.inflate(layoutInflater)
    }
    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }
    private val bancoDados by lazy {
        FirebaseFirestore.getInstance()
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

        //Exibir nome usuario
        exibirNomeUsuario()

        binding.btnDeslogar.setOnClickListener {
            deslogarUsuario()
        }

        binding.btnExibirListarTodosDados.setOnClickListener{
            listarTodosDados()
        }
    }

    private fun exibirNomeUsuario() {
        val idUsuarioLogado = autenticacao.currentUser?.uid

        if(idUsuarioLogado != null) {
            val referenciaUsuario = bancoDados
                .collection("usuarios")
                .document(idUsuarioLogado)



            referenciaUsuario.addSnapshotListener { documentSnapshot, erro ->
                val dados = documentSnapshot?.data

                if (dados != null) {
                    val nome = dados["nome"].toString()
                    binding.textUsuarioNome.text = nome
                } else {

                    val mensagemErro = erro?.message
                    exibirMensagem("Erro:  - $mensagemErro")
                    erro?.printStackTrace()
                    Log.i("AulaFirebase","Erro ${erro?.message}")
                }

            }

        }


    }

    private fun listarTodosDados() {
        val idUsuarioLogado = autenticacao.currentUser?.uid

        if(idUsuarioLogado != null) {

            val referenciaUsuario = bancoDados
                .collection("usuarios")
            //Para um único usuário
            //.document(idUsuarioLogado)

            //Recuperar dados em tempo real(pois usa Listener)
            //Lista de documentos
            referenciaUsuario.addSnapshotListener { querySnapshot, erro ->

                val listaDocuments = querySnapshot?.documents

                var listaResultado = ""
                listaDocuments?.forEach { documentSnapshot ->

                    val dados = documentSnapshot?.data
                    if(dados != null) {
                        val nome = dados["nome"]
                        val idade = dados["idade"]

                        listaResultado += "NOME: $nome IDADE: $idade\n"

                    }

                }
                binding.textListarTodosDados.text = listaResultado




                //Um único usuário
                /*val dados =documentSnapshot?.data
                if(dados != null) {
                    val nome = dados["nome"]
                    val idade = dados["idade"]
                    val textoResultado = "nome: $nome idade: $idade"

                    binding.textResultado.text = textoResultado
                }*/
            }





            /*referenciaUsuario
                .get()
                .addOnSuccessListener {documentSnapshot ->
                    val dados =documentSnapshot.data
                    if(dados != null) {
                        val nome = dados["nome"]
                        val idade = dados["idade"]
                        val textoResultado = "nome: $nome idade: $idade"

                        binding.textResultado.text = textoResultado
                    }

                }
                .addOnFailureListener {  }*/
        }


    }

    private fun deslogarUsuario() {
        autenticacao.signOut()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}