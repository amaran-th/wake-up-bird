package com.example.wake_up_bird.presentation.ui.init

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.wake_up_bird.R
import com.example.wake_up_bird.presentation.ui.base.NavigationActivity
import com.example.wake_up_bird.presentation.ui.create_room.CreateRoomActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class InitActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.init)

        val searchButton = findViewById<AppCompatButton>(R.id.Btn_init_1)
        val roomButton = findViewById<AppCompatButton>(R.id.Btn_init_2)
        val codeEditText = findViewById<EditText>(R.id.ET_init_1)

        val trueLayout = findViewById<FrameLayout>(R.id.Frag_code_true)
        val passEditText = findViewById<EditText>(R.id.ET_init_2)
        val backButton = findViewById<AppCompatButton>(R.id.Btn_init_3)
        val enterButton = findViewById<AppCompatButton>(R.id.Btn_init_4)
        val passWarning = findViewById<TextView>(R.id.TV_init_5)
        val roomName = findViewById<TextView>(R.id.TV_init_3)
        val roomInfo = findViewById<TextView>(R.id.TV_init_4)

        val falseLayout = findViewById<FrameLayout>(R.id.Frag_code_false)
        val checkButton = findViewById<AppCompatButton>(R.id.Btn_init_5)

        searchButton.setOnClickListener {
            val enteredCode = codeEditText.text.toString().trim()

            if(enteredCode.isNotEmpty()) {
                val db: FirebaseFirestore = FirebaseFirestore.getInstance()
                val colRef: CollectionReference = db.collection("room")

                colRef.document(enteredCode).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val name = document.getString("name")
                            val create_data = document.getString("create_data")
                            val member_num = document.getLong("member_num")

                            roomName.setText(name)
                            roomInfo.setText(create_data + " 개설 • " + "${member_num}명" )

                            trueLayout.visibility = View.VISIBLE
                        } else {
                            falseLayout.visibility = View.VISIBLE
                        }
                    }
            }
        }

        roomButton.setOnClickListener {
            val intent = Intent(this, CreateRoomActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            trueLayout.visibility = View.INVISIBLE
            passWarning.visibility = View.INVISIBLE
            passEditText.text.clear()
            passEditText.setBackgroundResource(R.drawable.line_init_password)
        }

        enterButton.setOnClickListener {
            val enteredPass = passEditText.text.toString().toIntOrNull()

            val db: FirebaseFirestore = FirebaseFirestore.getInstance()
            val colRef: CollectionReference = db.collection("room")

            colRef.document(codeEditText.text.toString()).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val password = document.getString("password")?.toIntOrNull()

                        if (enteredPass == password) {
                            val upref = getSharedPreferences("upref", Activity.MODE_PRIVATE)
                            val userId = upref.getString("id", null)

                            if (userId != null) {
                                val role = "guest"
                                val currentMemberNum = document.getLong("member_num") ?: 0
                                val updatedMemberNum = currentMemberNum + 1

                                db.collection("user").document(userId).update(
                                    "room_id", codeEditText.text.toString(),
                                    "role", role)
                                    .addOnSuccessListener {
                                        colRef.document(codeEditText.text.toString()).update("member_num", updatedMemberNum)
                                            .addOnSuccessListener {
                                                val intent = Intent(this, NavigationActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(ContentValues.TAG, "Error updating room member_num", e)
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(ContentValues.TAG, "Error updating user room_id", e)
                                    }
                            } else {
                                Log.e(ContentValues.TAG, "현재 사용자 ID를 가져오는 데 실패했습니다.")
                            }
                        } else {
                            passWarning.visibility = View.VISIBLE
                            passEditText.setBackgroundResource(R.drawable.line_init_warning)
                        }
                    }
                }
        }

        checkButton.setOnClickListener {
            falseLayout.visibility = View.INVISIBLE
        }
    }
}