package com.mimo.poketeamapp.login

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.mimo.poketeamapp.forgotPassword.ForgotPasswordActivity
import com.mimo.poketeamapp.MainActivity
import com.mimo.poketeamapp.R
import com.mimo.poketeamapp.database.AppDatabase
import com.mimo.poketeamapp.registration.RegisterUserActivity
import com.mimo.poketeamapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val db = Room
            .databaseBuilder(applicationContext, AppDatabase::class.java, "pokemon-database")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
        // TODO: cambiar fallback a manejo de migraciones
        // TODO: borrar
        db.pokemonDao().removeAllFavorites()
        db.pokemonDao().addFavorite("Eevee","https://pokeapi.co/api/v2/pokemon/133",
            "133",  "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/133.png",
            65)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val forgotPassword = binding.forgotPassword
        val registerUser = binding.signUp
        val login = binding.login
        val loading = binding.loading
        val picture = binding.profilePictureLogin

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(db))
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
            
            setOnTouchListener { v, event ->
                val drawableLeft = 0
                val drawableTop = 1
                val drawableRight = 2
                val drawableBottom = 3

                if(event.action == MotionEvent.ACTION_UP) {
                    if(event.rawX >= (password.right - password.compoundDrawables[drawableRight].bounds.width())) {
                        if(password.inputType == InputType.TYPE_CLASS_TEXT) {
                            // TODO: no funciona del todo
                            password.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                        } else {
                            password.inputType = InputType.TYPE_CLASS_TEXT
                        }
                    }
                }
                false
            }
        }

        forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        registerUser.setOnClickListener {
            val intent = Intent(this, RegisterUserActivity::class.java)
            startActivity(intent)
        }

        picture.setOnClickListener {
            Log.d("imagen", "imageeeeeeen")
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}