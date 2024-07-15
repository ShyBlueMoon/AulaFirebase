package com.luanasilva.aulafirebase

import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.luanasilva.aulafirebase.databinding.ActivityUploadImagemBinding
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
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

    private val permissoes = listOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false


    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.i("permissao_app", "requestCode: $requestCode'")


        permissions.forEachIndexed {indice, valor ->
            Log.i("permissao_app", "permission: $indice) $valor'")
        }
        grantResults.forEachIndexed {indice, valor ->
            Log.i("permissao_app", "concedida: $indice) $valor'")
        }

    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        solicitarPermissoes()

        /*Permissao.requisitarPermissoes(this, permissoes,100)*/

        binding.btnGaleria.setOnClickListener {
            if (temPermissaoGaleria) {
                abrirGaleria.launch("image/*")//MIME Type
            } else {
                Toast.makeText(this,"Você não tem permissão de galeria", Toast.LENGTH_LONG).show()
            }


        }

        binding.btnUpload.setOnClickListener {
            //uploadGaleria()
            uploadCamera()
        }

        binding.btnRecuperar.setOnClickListener {
            recuperarImagemFirebase()
        }

        binding.btnCamera.setOnClickListener {
            if (temPermissaoCamera) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                abrirCamera.launch(intent)
            } else {
                Toast.makeText(this,"Você não tem permissão de camera", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun solicitarPermissoes() {
        //Verificar permissões que o usuário já tem
        val permissoesNegadas = mutableListOf<String>()

        temPermissaoCamera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if( !temPermissaoCamera)
            permissoesNegadas.add(android.Manifest.permission.CAMERA)
        if( !temPermissaoGaleria)
            permissoesNegadas.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)


        //Solicitar permissões
        if(permissoesNegadas.isNotEmpty()) {
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissoes:Map<String, Boolean> ->
                //camera - true
                Log.i("novas_permissoes", "permissoes: $permissoes")
                temPermissaoCamera= permissoes[android.Manifest.permission.CAMERA]
                    ?:temPermissaoCamera

                temPermissaoGaleria= permissoes[android.Manifest.permission.READ_EXTERNAL_STORAGE]
                    ?:temPermissaoGaleria

            }
            gerenciadorPermissoes.launch(permissoesNegadas.toTypedArray())
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


    private fun uploadCamera() {

        val idUsuarioLogado = autenticacao.currentUser?.uid

        val outputStream = ByteArrayOutputStream()
        bitmapImagemSelecionada?.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            outputStream
        )

        if (bitmapImagemSelecionada != null && idUsuarioLogado != null) {
            //getReference cria a pasta ,se não existir uma de mesmo nome
            armazenamento
                .getReference("fotos")
                .child(idUsuarioLogado)
                .child("foto.jpg")//nomeImagem
                //link da imagem no celular
                .putBytes(outputStream.toByteArray())
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
        } else {
            Toast.makeText(this, "URI or id usuario is null", Toast.LENGTH_SHORT).show()
        }

    }
}