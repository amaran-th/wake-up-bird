package com.example.wake_up_bird.presentation.ui.create_room

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.example.wake_up_bird.R
import com.example.wake_up_bird.presentation.ui.init.InitActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.UUID
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CreateRoomActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.create_room)

        val nameEdit = findViewById<AppCompatEditText>(R.id.ET_create_room_1)
        val passwordEdit = findViewById<AppCompatEditText>(R.id.ET_create_room_2)
        val timeStartEdit = findViewById<AppCompatEditText>(R.id.ET_create_room_3_start)
        val timeMiddleEdit = findViewById<AppCompatEditText>(R.id.ET_create_room_3_middle)
        val timeEndEdit = findViewById<AppCompatEditText>(R.id.ET_create_room_3_end)
        val lateFeeEdit = findViewById<AppCompatEditText>(R.id.ET_create_room_4)
        val absentFeeEdit = findViewById<AppCompatEditText>(R.id.ET_create_room_5)

        val nameWarning = findViewById<AppCompatTextView>(R.id.TV_create_room_warning_1)
        val passwordWarning = findViewById<AppCompatTextView>(R.id.TV_create_room_warning_2)

        val backButton = findViewById<AppCompatButton>(R.id.Btn_create_room_1)
        val makeButton = findViewById<AppCompatButton>(R.id.Btn_create_room_2)

        timeStartEdit.setOnClickListener {
            showTimePickerDialog(timeStartEdit)
        }

        timeMiddleEdit.setOnClickListener {
            showTimePickerDialog(timeMiddleEdit)
        }

        timeEndEdit.setOnClickListener {
            showTimePickerDialog(timeEndEdit)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, InitActivity::class.java)
            startActivity(intent)
        }

        makeButton.setOnClickListener {
            val name = nameEdit.text.toString()
            val password = passwordEdit.text.toString()
            val timeStart = timeStartEdit.text.toString()
            val timeMiddle = timeMiddleEdit.text.toString()
            val timeEnd = timeEndEdit.text.toString()
            val lateFee = lateFeeEdit.text.toString()
            val absentFee = absentFeeEdit.text.toString()

            if (name.isEmpty()) {
                nameWarning.visibility = View.VISIBLE
                nameEdit.setBackgroundResource(R.drawable.line_create_room_warning)
            } else {
                nameEdit.setBackgroundResource(R.drawable.line_create_room)
                nameWarning.visibility = View.INVISIBLE
            }
            if (password.isEmpty()) {
                passwordWarning.visibility = View.VISIBLE
                passwordEdit.setBackgroundResource(R.drawable.line_create_room_warning)
            } else {
                passwordEdit.setBackgroundResource(R.drawable.line_create_room)
                passwordWarning.visibility = View.INVISIBLE
            }

            if (name.isNotEmpty() && password.isNotEmpty()) {
                // default fee
                if (lateFee.isEmpty()) "100"
                if (absentFee.isEmpty()) "2000"

                writeFirebase(name, password, timeStart, timeMiddle, timeEnd, lateFee, absentFee)
            }
        }
    }

    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->
                val selectedTime = String.format(" %02d:%02d", hourOfDay, minute)
                editText.setText(selectedTime)
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }

    private fun writeFirebase(name: String, password: String, timeStart: String, timeMiddle: String,
                              timeEnd: String, lateFee: String, absentFee: String) {
        val randomCode: String = UUID.randomUUID().toString().replace("-", "").take(12)
        val currentDate = getCurrentDate()
        val room = hashMapOf(
            "name" to name,
            "password" to password,
            "start_time" to timeStart,
            "middle_time" to timeMiddle,
            "end_time" to timeEnd,
            "late_fee" to lateFee,
            "absentee_fee" to absentFee,
            "invite_code" to randomCode,
            "create_data" to currentDate,
            "member_num" to 1
        )

        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val colRef: CollectionReference = db.collection("room")

        colRef.document(randomCode).set(room)
            .addOnSuccessListener {
                // 방 생성 성공시 초기 화면으로 돌아감 (수정 필요)
                val intent = Intent(this, InitActivity::class.java)
                startActivity(intent)
            }
    }

    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA)
        return currentDate.format(formatter)
    }
}