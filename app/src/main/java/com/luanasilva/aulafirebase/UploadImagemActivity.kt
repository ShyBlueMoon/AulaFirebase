package com.luanasilva.aulafirebase

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.luanasilva.aulafirebase.databinding.ActivityUploadImagemBinding
import com.squareup.picasso.Picasso
import java.util.UUID


class UploadImagemActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityUploadImagemBinding.inflate(layoutInflater)
    }

    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }

    private val armazenamento by lazy {
        FirebaseStorage.getInstance()
    }

    private val nomeImagemAleatorio = UUID.randomUUID().toString()


    private var uriImagemSelecionada:Uri? = null
    private var bitmapImagemSelecionada:Bitmap? = null

    private var abrirGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri->
        if (uri != null) {
            binding.imageSelecionada.setImageURI(uri)
            uriImagemSelecionada = uri

            Toast.makeText(this, "Imagem selecionada",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Nenhuma imagem selecionada",Toast.LENGTH_SHORT).show()
        }
    }

    private var abrirCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultadoActivity->
        if(resultadoActivity.resultCode == RESULT_OK){
            bitmapImagemSelecionada = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                resultadoActivity.data?.extras?.getParcelable("data", Bitmap::class.java)
            } else {
                resultadoActivity.data?.extras?.getParcelable("data")
            }
            binding.imageSelecionada.setImageBitmap(bitmapImagemSelecionada)
        } else {
            Toast.makeText(this, "Erro ao carregar a camera", Toast.LENGTH_LONG).show()

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

        binding.btnGaleria.setOnClickListener {

            abrirGaleria.launch("image/*")//MIME Type
        }

        binding.btnUpload.setOnClickListener {
            uploadGaleria()
        }

        binding.btnRecuperar.setOnClickListener {
            recuperarImagemFirebase()
        }

        binding.btnCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            abrirCamera.launch(intent)
        }
    }

    private fun recuperarImagemFirebase() {
        val idUsuarioLogado = autenticacao.currentUser?.uid

        if(idUsuarioLogado != null) {
            armazenamento.getReference("fotos")
                .child(idUsuarioLogado)
                .child("foto1")
                .downloadUrl
                .addOnSuccessListener { urlFirebase->
                    Picasso.get()
                        .load(urlFirebase)
                        .into(binding.imageRecuperada)

                }.addOnFailureListener{ erro->
                    Toast.makeText(this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
                    erro.printStackTrace()
                    Log.i("${NovaContaActivity.LOG_FIREBASE}", "${erro.message}")

                }
        }
    }



    private fun uploadGaleria() {

        val idUsuarioLogado = autenticacao.currentUser?.uid
        val nomeImagem = UUID.randomUUID().toString()

        if (uriImagemSelecionada != null && idUsuarioLogado != null) {
            //getReference cria a pasta ,se não existir uma de mesmo nome
            armazenamento.getReference("fotos")
                .child(idUsuarioLogado)
                .child("foto1")//nomeImagem
                //link da imagem no celular
                .putFile(uriImagemSelecionada!!)
                .addOnSuccessListener { task->
                    Toast.makeText(this, "Sucesso ao fazer upload da imagem", Toast.LENGTH_LONG).show()
                    task.metadata?.reference?.downloadUrl?.addOnSuccessListener { urlFirebase->
                        Toast.makeText(this, urlFirebase.toString(), Toast.LENGTH_SHORT).show()
                    }
                    
                }.addOnFailureListener{ erro->
                    Toast.makeText(this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
                    erro.printStackTrace()
                    Log.i("${NovaContaActivity.LOG_FIREBASE}", "${erro.message}")
                }
        }

    }
}