package br.com.igorbag.githubsearch.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository

class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var repositoryAdapter: RepositoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        getAllReposByUserName()
    }

    // Método responsável por realizar o setup da view e recuperar os IDs do layout
    fun setupView() {
        nomeUsuario = findViewById(R.id.editTextNomeUsuario)
        btnConfirmar = findViewById(R.id.buttonConfirmar)
        listaRepositories = findViewById(R.id.recyclerViewRepositories)
        setupListeners()
    }

    // Método responsável por configurar os listeners de clique da tela
    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            saveUserLocal()
        }
    }

    // Salvar o usuário preenchido no EditText usando SharedPreferences
    private fun saveUserLocal() {
        val usuario = nomeUsuario.text.toString()
        val sharedPref = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("usuario", usuario)
        editor.apply()
    }

    private fun showUserName() {
        val sharedPref = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val usuario = sharedPref.getString("usuario", null)
        if (usuario != null) {
            nomeUsuario.setText(usuario)
        }
    }

    // Método responsável por fazer a configuração base do Retrofit
    fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    // Método responsável por buscar todos os repositórios do usuário fornecido
    fun getAllReposByUserName() {
        btnConfirmar.setOnClickListener {
            val usuario = nomeUsuario.text.toString()
            val call = githubApi.getAllRepositoriesByUser(usuario)
            call.enqueue(object : Callback<List<Repository>> {
                override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                    if (response.isSuccessful) {
                        val repositories = response.body() ?: emptyList()
                        setupAdapter(repositories)
                    } else {
                        // Tratar erro da chamada à API
                    }
                }

                override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    // Tratar falha na chamada à API
                }
            })
        }
    }

    // Método responsável por configurar o adapter
    fun setupAdapter(list: List<Repository>) {
        repositoryAdapter = RepositoryAdapter(list)
        repositoryAdapter.carItemLister = {
            // Implemente a ação desejada ao clicar no item da lista
        }
        repositoryAdapter.btnShareLister = {
            // Implemente a ação desejada ao clicar no botão Share
        }

        listaRepositories.layoutManager = LinearLayoutManager(this)
        listaRepositories.adapter = repositoryAdapter
    }

    // Compartilhar o link do repositório selecionado
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Abrir o browser com o link informado do repositório
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }
}