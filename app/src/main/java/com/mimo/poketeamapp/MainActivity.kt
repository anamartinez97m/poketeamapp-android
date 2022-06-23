package com.mimo.poketeamapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.mimo.poketeamapp.databinding.ActivityMainBinding
import com.mimo.poketeamapp.model.Pokemon
import com.mimo.poketeamapp.network.GsonRequest
import com.mimo.poketeamapp.network.RequestManager
import com.mimo.poketeamapp.settings.SettingsActivity

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private var count = 1126
    private lateinit var pokemonView: PokemonView
    private lateinit var textViewError404: TextView

    private lateinit var username: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.myToolbar
        toolbar.title = getString(R.string.app_name)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = binding.navigationView
        navigationView.setNavigationItemSelectedListener(this)

        val swipeRefresh: SwipeRefreshLayout = binding.swipeRefresh
        val textViewSwipeRefresh: TextView = binding.textviewSwipeRefresh

        pokemonView = binding.pokemonView
        textViewError404 = binding.textviewError404
        textViewError404.visibility = View.GONE

        // TODO: Change for placeholders
        val randomCount = rand(count)
        textViewSwipeRefresh.text = "Número random entre [1 y $count]: $randomCount"
        doRequest(randomCount)
        swipeRefresh.setOnRefreshListener {
            val randomCount2 = rand(count)
            textViewSwipeRefresh.text = "Número random entre [1 y $count]: $randomCount2"
            doRequest(randomCount2)
            swipeRefresh.isRefreshing = false
        }

        val intent: Intent = intent
        username = intent.getStringExtra("username").toString()
        password = intent.getStringExtra("password").toString()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bar, menu)
        return true
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_settings -> {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("password", password)
            startActivity(intent)
            true
        }
        R.id.menu_sign_out -> {
            LogoutConfirmationDialogFragment().show(supportFragmentManager, LogoutConfirmationDialogFragment.TAG)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_nav_team -> {
            val intent = Intent(this, PokeTeamActivity::class.java)
            startActivity(intent)
            true
        }
        R.id.menu_nav_poke_list -> {
            val intent = Intent(this, PokeListActivity::class.java)
            startActivity(intent)
            true
        }
        R.id.menu_nav_tutorial -> {
            val intent = Intent(this, TutorialActivity::class.java)
            startActivity(intent)
            true
        }
        R.id.menu_nav_about -> {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
            true
        }
        else -> {
            true
        }
    }

    private fun doRequest(id: Int) {
        val url = "https://pokeapi.co/api/v2/pokemon/$id"
        val gsonRequest = GsonRequest(url,
            Pokemon::class.java, null,
            { response ->
                pokemonView.visibility = View.VISIBLE
                textViewError404.visibility = View.GONE
                Log.d("response name", response.name)
                Log.d("response base experience", response.base_experience.toString())
                Log.d("response types", response.types.contentToString())
            },
            {
                pokemonView.visibility = View.GONE
                textViewError404.visibility = View.VISIBLE
                textViewError404.text = "Pokemon no encontrado. \nPor favor, recargue de nuevo la página."
                Log.d("requestError", "Pokemon no encontrado. Por favor, recargue de nuevo la página.")
            }
        )
        RequestManager.getInstance(this).addToRequestQueue(gsonRequest)
    }

    private fun rand(end: Int): Int {
        val start = 1
        require(start <= end) { "Illegal Argument" }
        return (Math.random() * (end - start + 1)).toInt() + start
    }
}