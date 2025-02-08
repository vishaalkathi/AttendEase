package com.vishaal.attendease

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class SettingsActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = Firebase.auth
        database = FirebaseDatabase.getInstance().getReference("Users")
        val type = intent.getStringExtra("Type")
        val nameTxtBox: EditText = findViewById(R.id.nameInput)
        val regTxtBox: EditText = findViewById(R.id.registrationNumberInput)
        val saveBtn: Button = findViewById(R.id.saveButton)
        val passwdResetBtn: Button = findViewById(R.id.resetPasswordButton)
        val signOutBtn: Button = findViewById(R.id.signOutButton)
        val homeBtn: ImageView = findViewById(R.id.homeIcon)

        if (type.equals("Teacher"))
        {
            regTxtBox.visibility = View.GONE
            val userID = mAuth.currentUser?.uid
            var name = ""
            database.child(userID.toString()).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        name = snapshot.child("Name").getValue(String::class.java).toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to read data", error.toException())
                }
            })
            nameTxtBox.setText(name)
        }
        else
        {
            val userID = mAuth.currentUser?.uid
            var name = ""
            var reg = ""
            database.child(userID.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        name = snapshot.child("Name").getValue(String::class.java).toString()
                        reg = snapshot.child("Reg").getValue(String::class.java).toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to read data", error.toException())
                }
            })
            nameTxtBox.setText(name)
            regTxtBox.setText(reg)
        }

        nameTxtBox.setOnClickListener{
            saveBtn.visibility = View.VISIBLE
        }

        regTxtBox.setOnClickListener {
            saveBtn.visibility = View.VISIBLE
        }

        saveBtn.setOnClickListener {
            if (type.equals("Teacher"))
            {
                val teachName = nameTxtBox.text.toString()
                val databaseRef = FirebaseDatabase.getInstance().getReference("Users")
                val userID = mAuth.currentUser?.uid

                val updates = mapOf("Name" to teachName)

                databaseRef.child(userID.toString()).updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update profile!", Toast.LENGTH_SHORT).show()
                    }
            }
            else
            {
                val studName = nameTxtBox.text.toString()
                val studReg = regTxtBox.text.toString()
                val databaseRef = FirebaseDatabase.getInstance().getReference("Users")
                val userID = mAuth.currentUser?.uid

                val updates = mapOf("Name" to studName, "Reg" to studReg)

                databaseRef.child(userID.toString()).updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update profile!", Toast.LENGTH_SHORT).show()
                    }

            }
        }

        passwdResetBtn.setOnClickListener {
            val userID = mAuth.currentUser?.uid
            var email = ""
            database.child(userID.toString()).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        email = snapshot.child("Email").getValue(String::class.java).toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to read data", error.toException())
                }
            })

            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset mail sent to " + email, Toast.LENGTH_SHORT).show()
                        signOutBtn.performClick()
                    }
                }
        }

        signOutBtn.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        homeBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("Type", type)
            startActivity(intent)
        }
    }
}