package com.example.wake_up_bird.presentation.ui.init

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.example.wake_up_bird.R

class InitActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.init_keypad)

        val searchButton = findViewById<AppCompatImageButton>(R.id.IBtn_init_keypad_1)
        val roomButton = findViewById<AppCompatImageButton>(R.id.IBtn_init_keypad_2)
        val codeEditText = findViewById<EditText>(R.id.ET_init_keypad_1)

        val trueLayout = findViewById<FrameLayout>(R.id.Frag_code_true)
        val passEditText = findViewById<EditText>(R.id.ET_init_keypad_2)
        val backButton = findViewById<AppCompatImageButton>(R.id.IBtn_init_keypad_3)
        val enterButton = findViewById<AppCompatImageButton>(R.id.IBtn_init_keypad_4)
        val passWarning = findViewById<TextView>(R.id.TV_init_keypad_5)

        val falseLayout = findViewById<FrameLayout>(R.id.Frag_code_false)
        val checkButton = findViewById<AppCompatImageButton>(R.id.IBtn_init_keypad_5)

        codeEditText.requestFocus()
        val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        manager.showSoftInput(codeEditText, InputMethodManager.SHOW_IMPLICIT)

        searchButton.setOnClickListener {
            val enteredCode = codeEditText.text.toString().trim()

            if(enteredCode == "true") {
                trueLayout.visibility = View.VISIBLE
            }
            else {
                falseLayout.visibility = View.VISIBLE
            }
        }

        roomButton.setOnClickListener {
            // 인증방 만드는 화면으로 전환하는 부분
        }

        backButton.setOnClickListener {
            trueLayout.visibility = View.INVISIBLE
            passWarning.visibility = View.INVISIBLE
            passEditText.text.clear()
            passEditText.setBackgroundResource(R.drawable.ic_init_search_line)
        }

        enterButton.setOnClickListener {
            val enteredPass = passEditText.text.toString().trim()

            if(enteredPass == "1234") {
                // 해당 채팅방으로 입장하는 부분
            }
            else  {
                passWarning.visibility = View.VISIBLE
                passEditText.setBackgroundResource(R.drawable.ic_init_warning_line)
            }
        }

        checkButton.setOnClickListener {
            falseLayout.visibility = View.INVISIBLE
        }
    }
}