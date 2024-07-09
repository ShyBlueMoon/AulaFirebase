package com.luanasilva.aulafirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luanasilva.aulafirebase.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
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

        binding.btnLogin.setOnClickListener {

            logarUsuario()

            //atualizarRemoverDados()

            //cadastroUsuario()
            //pesquisarDados()




        }

        binding.btnActivityNovaConta.setOnClickListener {
            startActivity(Intent(this, NovaContaActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        verificarUsuarioLogado()
    }

    private fun verificarUsuarioLogado() {
        //autenticacao.signOut()
        val usuario = autenticacao.currentUser
        if (usuario != null) {
            startActivity(Intent(this, PrincipalActivity::class.java))
        } else {
            Toast.makeText(this, "Não tem usuario logado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logarUsuario() {
        //Dados udigitarios pelo usuario
        val email = binding.editLoginEmail.text.toString()
        val senha = binding.editLoginSenha.text.toString()

        //Estivesse em uma tela de login
        autenticacao.signInWithEmailAndPassword(email, senha).addOnSuccessListener { authResult ->
            exibirMensagem("Sucesso ao logar usuário")
            startActivity(Intent(this, PrincipalActivity::class.java))

        }.addOnFailureListener { exception ->
            binding.textResultado.text = "Falha ao logar usuario ${exception.message}"
        }
    }



    //-------------------------------------------------------------------------


    private fun atualizarRemoverDados() {
        //aqui ele atualiza ana para ana cristina
        val dados = mapOf(
            "nome" to "ana",
            "idade" to "25"
        )

        val idUsuarioLogado = autenticacao.currentUser?.uid

        if (idUsuarioLogado != null) {
            val referenciaUsuario = bancoDados
                .collection("usuarios")
                .document(idUsuarioLogado)

            referenciaUsuario.update("nome", "Luana Silva")
                .addOnSuccessListener {
                    exibirMensagem("Usuário atualizado com sucesso")
                }.addOnFailureListener { exception ->
                    exibirMensagem("Erro ao atualizar usuario")
                }
        }

        /*

        referenciaAna.delete()
            .addOnSuccessListener {
            exibirMensagem("Usuário removido com sucesso")
        }.addOnFailureListener { exception ->
            exibirMensagem("Erro ao remover usuario")
        }
         */

    }

    private fun salvarDados() {

        val dados = mapOf(
            "nome" to "Lewis",
            "idade" to "33"
        )

        bancoDados.collection("usuarios")
            .document("1")
            .set(dados)
            .addOnSuccessListener {
                exibirMensagem("Usuário salvo com sucesso")
            }.addOnFailureListener { exception ->
                exibirMensagem("Erro ao salvar usuario com sucesso")
            }

    }

    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }


}