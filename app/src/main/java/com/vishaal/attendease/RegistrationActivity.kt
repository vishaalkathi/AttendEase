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
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegistrationActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = Firebase.auth

        val cardSelect: CardView = findViewById(R.id.card_selection)
        val cardTeach: CardView = findViewById(R.id.card_teacher)
        val cardStud: CardView = findViewById(R.id.card_student)

        //under card select
        val teachBtn: Button = findViewById(R.id.btn_teacher)
        val studBtn: Button = findViewById(R.id.btn_student)

        //under card Teach
        val teachNameBox: EditText = findViewById(R.id.et_teacher_name)
        val teachEmailBox: EditText = findViewById(R.id.et_teacher_email)
        val teachPasswdBox: EditText = findViewById(R.id.et_teacher_password)
        val teachRegBtn: Button = findViewById(R.id.btn_teacher_signup)

        //under card student
        val studNameBox: EditText = findViewById(R.id.et_student_name)
        val studEmailBox: EditText = findViewById(R.id.et_student_email)
        val studRegBox: EditText = findViewById(R.id.et_student_reg)
        val studPasswdBox: EditText = findViewById(R.id.et_student_password)
        val studRegBtn: Button = findViewById(R.id.btn_student_signup)

        val signInBtn: Button = findViewById(R.id.btn_old_user)

        signInBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        teachBtn.setOnClickListener {
            cardSelect.visibility = View.GONE
            cardStud.visibility = View.GONE
            cardTeach.visibility = View.VISIBLE
        }

        studBtn.setOnClickListener {
            cardSelect.visibility = View.GONE
            cardTeach.visibility = View.GONE
            cardStud.visibility = View.VISIBLE
        }

        teachRegBtn.setOnClickListener {
            var teachName = teachNameBox.text.toString()
            var teachEmail = teachEmailBox.text.toString()
            var teachPasswd = teachPasswdBox.text.toString()
            mAuth.createUserWithEmailAndPassword(teachEmail, teachPasswd).addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                {
                    val userDetails = hashMapOf(
                        "Name" to teachName,
                        "Email" to teachEmail,
                        "Passwd" to teachPasswd,
                        "Type" to "Teacher"
                    )
                    val database = Firebase.database
                    val myRef = database.getReference("Users")
                    val userId = mAuth.currentUser?.uid.toString()
                    myRef.child(userId).setValue(userDetails).addOnSuccessListener {
                        Toast.makeText(this, "Sign up was successful.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("Type", "Teacher")
                        startActivity(intent)
                    }.addOnFailureListener {
                        val user = Firebase.auth.currentUser!!
                        user.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Sign in was unsuccessful.Please Try Again", Toast.LENGTH_SHORT).show()
                                    teachNameBox.setText("")
                                    teachEmailBox.setText("")
                                    teachPasswdBox.setText("")
                                }
                            }
                    }
                }
            }
        }

        studRegBtn.setOnClickListener {
            var studName = studNameBox.text.toString()
            var studEmail = studEmailBox.text.toString()
            var studPasswd = studPasswdBox.text.toString()
            var studReg = studRegBox.text.toString()
            mAuth.createUserWithEmailAndPassword(studEmail, studPasswd).addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                {
                    val userDetails = hashMapOf(
                        "Name" to studName,
                        "Email" to studEmail,
                        "Passwd" to studPasswd,
                        "Reg" to studReg,
                        "Type" to "Teacher"
                    )
                    val database = Firebase.database
                    val myRef = database.getReference("Users")
                    val userId = mAuth.currentUser?.uid.toString()
                    myRef.child(userId).setValue(userDetails).addOnSuccessListener {
                        Toast.makeText(this, "Sign up was successful.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("Type", "Teacher")
                        startActivity(intent)
                    }.addOnFailureListener {
                        val user = Firebase.auth.currentUser!!
                        user.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Sign in was unsuccessful.Please Try Again", Toast.LENGTH_SHORT).show()
                                    studNameBox.setText("")
                                    studEmailBox.setText("")
                                    studPasswdBox.setText("")
                                    studRegBox.setText("")
                                }
                            }
                    }
                }
            }
        }
    }
}