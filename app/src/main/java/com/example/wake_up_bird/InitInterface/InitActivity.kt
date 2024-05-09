package com.example.wake_up_bird.InitInterface

import android.os.Bundle
import android.widget.EditText
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.example.wake_up_bird.R

class InitActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.init)

        val searchButton = findViewById<AppCompatImageButton>(R.id.IBtn_init_1)
        val roomButton = findViewById<AppCompatImageButton>(R.id.IBtn_init_2)
        val codeEditText = findViewById<EditText>(R.id.ET_init_1)

        codeEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // Keypad가 활성화되면 InitKeypadActivity로 전환
                val intent = Intent(this, InitKeypadActivity::class.java)
                startActivity(intent)
            }
        }

        searchButton.setOnClickListener {
            val enteredCode = codeEditText.text.toString().trim()

            // 검색 로직을 수행하고 결과를 출력하는 부분
        }

        roomButton.setOnClickListener {
            // 인증방 만드는 화면으로 전환하는 부분
        }
    }
}