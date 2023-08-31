package br.com.igorbag.githubsearch.ui

// ChatGPT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var nomeUsuario: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var listaRepositories: RecyclerView
    private lateinit var githubApi: GitHubService
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupListeners()
        setupRetrofit()
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        showUserName()
        getAllReposByUserName()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        //@TODO 1 - Recuperar os Id's da tela para a Activity com o findViewById
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        //@TODO 2 - colocar a acao de click do botao confirmar
        try {

            btnConfirmar.setOnClickListener {
                saveUserLocal()
            }

        } catch (e: Exception) {

            e.printStackTrace()

        } finally {

        }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
        //@TODO 3 - Persistir o usuario preenchido na editText com a SharedPref no listener do botao salvar
        val username = nomeUsuario.text.toString()
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.apply()
    }

    private fun showUserName() {
        //@TODO 4- depois de persistir o usuario exibir sempre as informacoes no EditText  se a
        // sharedpref possuir algum valor, exibir no proprio editText o valor salvo

        try {
            val savedUsername = sharedPreferences.getString("username", "luismendes070")
            nomeUsuario.setText(savedUsername)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

        }

    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {
        /*
           @TODO 5 -  realizar a Configuracao base do retrofit
           Documentacao oficial do retrofit - https://square.github.io/retrofit/
           URL_BASE da API do  GitHub= https://api.github.com/
           lembre-se de utilizar o GsonConverterFactory mostrado no curso
        */

        try {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            githubApi = retrofit.create(GitHubService::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

        }

    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {
        // TODO 6 - realizar a implementacao do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso
        try {

            val savedUsername = sharedPreferences.getString("username", "")
            if (!savedUsername.isNullOrEmpty()) {

                // FIX ChatGPT error Bard error
                githubApi.getAllRepositoriesByUser(savedUsername)
                    .enqueue(object : Callback<List<Repository>> {
                        override fun onResponse(
                            call: Call<List<Repository>>,
                            response: Response<List<Repository>>
                        ) {
                            if (response.isSuccessful) {
                                val repositories = response.body()
                                repositories?.let {
                                    setupAdapter(it)
                                }
                            }
                        }

                        override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                            // Handle failure
                        }
                    })
            }

        } catch (e: Exception) {
            e.message
            e.printStackTrace()
        } finally {

        }
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        /*
            @TODO 7 - Implementar a configuracao do Adapter , construir o adapter e instancia-lo
            passando a listagem dos repositorios
         */
        try {
            val adapter = RepositoryAdapter(list) { repository ->
                shareRepositoryLink(repository.url)
            }

            listaRepositories.adapter = adapter
            listaRepositories.layoutManager = LinearLayoutManager(this)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

        }
    }


    // Metodo responsavel por compartilhar o link do repositorio selecionado
    // @Todo 11 - Colocar esse metodo no click do share item do adapter
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio

    // @Todo 12 - Colocar esse metodo no click item do adapter
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}