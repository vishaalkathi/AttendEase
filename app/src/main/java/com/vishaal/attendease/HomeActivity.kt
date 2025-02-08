package com.vishaal.attendease

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var scannedData: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = Firebase.auth
        database = FirebaseDatabase.getInstance().getReference("Users")
        val userID = mAuth.currentUser?.uid
        var name = ""
        database.child(userID.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
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

        val type = intent.getStringExtra("Type")
        val teachLyt: RelativeLayout = findViewById(R.id.teacherLayout)
        val studLyt: RelativeLayout = findViewById(R.id.studentLayout)

        if (type.equals("Teacher"))
        {
            teachLyt.visibility = View.VISIBLE
            studLyt.visibility = View.GONE
        }
        else
        {
            teachLyt.visibility = View.GONE
            studLyt.visibility = View.VISIBLE
        }

        //teacher layout
        val nameText: TextView = findViewById(R.id.teacherWelcome)
        val genQRBtn: Button = findViewById(R.id.generateQRButton)
        val genQRCardView: CardView = findViewById(R.id.qrCardView)
        val subTextBox: EditText = findViewById(R.id.subjectInput)
        val secTextBox: EditText = findViewById(R.id.sectionInput)
        val cardGenBtn: Button = findViewById(R.id.cardGenerateButton)
        val qrCodeIV: ImageView = findViewById(R.id.qrCodeIV)
        val doneBtn: Button = findViewById(R.id.attendanceDoneBtn)
        nameText.setText("WELCOME "+ name)

        genQRBtn.setOnClickListener {
            genQRCardView.visibility = View.VISIBLE
        }

        cardGenBtn.setOnClickListener {
            val subTxt = subTextBox.text.toString()
            val secTxt = secTextBox.text.toString()
            val text =  userID + "_" +  subTxt + "_" + secTxt

            val barcodeEncoder = BarcodeEncoder()
            try {
                // Generate QR code as Bitmap (400x400 pixels)
                val bitmap: Bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400)
                qrCodeIV.setImageBitmap(bitmap) // Set the generated QR code to ImageView
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }

        doneBtn.setOnClickListener {
            val subTxt = subTextBox.text.toString()
            val secTxt = secTextBox.text.toString()
            val classKey =  userID + "_" +  subTxt + "_" + secTxt

            val databaseRef = FirebaseDatabase.getInstance().getReference("Classes")
            databaseRef.child(classKey).child("students").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(this@HomeActivity, "No attendance data found!", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Create Excel workbook & sheet
                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet("Attendance")

                    // Create Header Row
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("Name")
                    headerRow.createCell(1).setCellValue("Registration Number")

                    var rowIndex = 1
                    for (studentSnapshot in snapshot.children) {
                        val studentData = studentSnapshot.value as? Map<String, String>
                        val name = studentData?.get("name") ?: "Unknown"
                        val reg = studentData?.get("reg") ?: "Unknown"

                        // Create row for each student
                        val row = sheet.createRow(rowIndex++)
                        row.createCell(0).setCellValue(name)
                        row.createCell(1).setCellValue(reg)
                    }

                    // Save Excel file
                    saveExcelFile(this@HomeActivity, workbook, classKey)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HomeActivity, "Failed to retrieve data!", Toast.LENGTH_SHORT).show()
                }
            })
        }

        //student layout
        val studNameText: TextView = findViewById(R.id.studentWelcome)
        val scanQRBtn: Button = findViewById(R.id.scanQRButton)
        studNameText.setText("WELCOME " + name)

        scanQRBtn.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setPrompt("Scan QR Code")
            integrator.setBeepEnabled(true)
            integrator.setOrientationLocked(true)
            integrator.initiateScan()
        }

        //settings page
        val settingsBtn: ImageView = findViewById(R.id.homeIcon)
        settingsBtn.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("Type", type)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                scannedData = result.contents // Store scanned text
                database = FirebaseDatabase.getInstance().getReference("Users")
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
                saveStudentData(scannedData, name, reg)
                Toast.makeText(this, "Scanned: $scannedData", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Scan canceled!", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }


    }

    private fun saveStudentData(classId: String, name: String, reg: String) {
        val database = FirebaseDatabase.getInstance().getReference("Classes")
        val studentId = database.push().key ?: return // Generate unique student key

        val studentData = mapOf(
            "name" to name,
            "reg" to reg
        )

        database.child(classId).child("students").child(studentId).setValue(studentData)
            .addOnSuccessListener {
                Toast.makeText(this, "Attendance Recorded!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to record attendance", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveExcelFile(context: Context, workbook: XSSFWorkbook, fileName: String) {
        try {
            val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Attendance")
            if (!directory.exists()) {
                directory.mkdirs() // Create directory if it doesn't exist
            }

            val file = File(directory, "$fileName.xlsx")
            val fileOutputStream = FileOutputStream(file)
            workbook.write(fileOutputStream)
            fileOutputStream.close()

            Toast.makeText(context, "Attendance saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving file!", Toast.LENGTH_SHORT).show()
        }
    }
}