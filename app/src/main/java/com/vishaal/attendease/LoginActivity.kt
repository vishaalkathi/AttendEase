package com.vishaal.attendease

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = Firebase.auth

        val emailTxtBox: EditText = findViewById(R.id.login_email)
        val passwdTxtBox: EditText = findViewById(R.id.login_password)
        val loginBtn: Button = findViewById(R.id.btn_login)

        val regBtn: Button = findViewById(R.id.btn_new_user)

        regBtn.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            var email = emailTxtBox.text.toString()
            var passwd = passwdTxtBox.text.toString()
            mAuth.signInWithEmailAndPassword(email, passwd)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this, "Sign in was successful.", Toast.LENGTH_SHORT).show()
                        val database = Firebase.database
                        val myRef = database.getReference("Users")
                        val userId = mAuth.currentUser?.uid.toString()
                        myRef.child(userId).get().addOnSuccessListener {snapshot ->
                            if (snapshot.exists())
                            {
                                val userDetails = snapshot.value as? HashMap<*, *>
                                val type = userDetails?.get("Type") as? String
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.putExtra("Type", type)
                                startActivity(intent)
                            }
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, "Sign in was unsuccessful.Please Try Again", Toast.LENGTH_SHORT).show()
                        emailTxtBox.setText("")
                        passwdTxtBox.setText("")
                    }
                }
        }
    }
}