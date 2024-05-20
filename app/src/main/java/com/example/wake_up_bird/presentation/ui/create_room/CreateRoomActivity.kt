package com.example.wake_up_bird.presentation.ui.create_room

import CustomTimePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat.startActivity
import com.example.wake_up_bird.R
import com.example.wake_up_bird.presentation.ui.base.NavigationActivity
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
        val timeStartEdit = findViewById<AppCompatTextView>(R.id.ET_create_room_3_start)
        val timeMiddleEdit = findViewById<AppCompatTextView>(R.id.ET_create_room_3_middle)
        val timeEndEdit = findViewById<AppCompatTextView>(R.id.ET_create_room_3_end)
        val lateFeeEdit = findViewById<AppCompatEditText>(R.id.ET_create_room_4)
        val absentFeeEdit = findViewById<AppCompatEditText>(R.id.ET_create_room_5)

        val nameWarning = findViewById<AppCompatTextView>(R.id.TV_create_room_warning_1)
        val passwordWarning = findViewById<AppCompatTextView>(R.id.TV_create_room_warning_2)
        val timeWarning = findViewById<AppCompatTextView>(R.id.TV_create_room_warning_3)
        val lateFeeWarning = findViewById<AppCompatTextView>(R.id.TV_create_room_warning_4)
        val absentFeeWarning = findViewById<AppCompatTextView>(R.id.TV_create_room_warning_5)

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
            if (lateFee.isEmpty()) {
                lateFeeWarning.visibility = View.VISIBLE
                lateFeeEdit.setBackgroundResource(R.drawable.line_create_room_warning)
            } else {
                lateFeeEdit.setBackgroundResource(R.drawable.line_create_room)
                lateFeeWarning.visibility = View.INVISIBLE
            }
            if (absentFee.isEmpty()) {
                absentFeeWarning.visibility = View.VISIBLE
                absentFeeEdit.setBackgroundResource(R.drawable.line_create_room_warning)
            } else {
                absentFeeEdit.setBackgroundResource(R.drawable.line_create_room)
                absentFeeWarning.visibility = View.INVISIBLE
            }

            if (isTimeAscending(timeStart, timeMiddle, timeEnd) == false) {
                timeWarning.visibility = View.VISIBLE
                timeStartEdit.setBackgroundResource(R.drawable.line_create_room_warning)
                timeMiddleEdit.setBackgroundResource(R.drawable.line_create_room_warning)
                timeEndEdit.setBackgroundResource(R.drawable.line_create_room_warning)
            } else {
                timeStartEdit.setBackgroundResource(R.drawable.line_create_room)
                timeMiddleEdit.setBackgroundResource(R.drawable.line_create_room)
                timeEndEdit.setBackgroundResource(R.drawable.line_create_room)
                timeWarning.visibility = View.INVISIBLE
            }

            if (name.isNotEmpty() && password.isNotEmpty() && lateFee.isNotEmpty() && absentFee.isNotEmpty()
                && (isTimeAscending(timeStart, timeMiddle, timeEnd) == true)) {
                writeFirebase(name, password, timeStart, timeMiddle, timeEnd, lateFee, absentFee)
            }
        }
    }

    private fun isTimeAscending(start: String, middle: String, end: String): Boolean {
        val regex = Regex("""(\d{2}):(\d{2})""")

        val startMatch = regex.find(start)
        val middleMatch = regex.find(middle)
        val endMatch = regex.find(end)

        if (startMatch != null && middleMatch != null && endMatch != null) {
            val (hourStr1, minuteStr1) = startMatch.destructured
            val startValue = hourStr1.toInt() * 60 + minuteStr1.toInt()

            val (hourStr2, minuteStr2) = middleMatch.destructured
            val middleValue = hourStr2.toInt() * 60 + minuteStr2.toInt()

            val (hourStr3, minuteStr3) = endMatch.destructured
            val endValue = hourStr3.toInt() * 60 + minuteStr3.toInt()

            return startValue < middleValue && middleValue < endValue
        } else {
            return false
        }
    }

    private fun showTimePickerDialog(text: TextView) {
        val calendar = Calendar.getInstance()
        val hour = 0
        val minute = 0

        val customTimePickerDialog = CustomTimePickerDialog(this,
            object : CustomTimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(hourOfDay: Int, minute: Int) {
                    val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    text.setText(selectedTime)
                }
            }, hour, minute)

        customTimePickerDialog.show()
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
                // 방 생성자를 방장으로 설정하고, 방에 입장시킴 (수정필요)
                val intent = Intent(this, NavigationActivity::class.java)
                startActivity(intent)
            }
    }

    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA)
        return currentDate.format(formatter)
    }
}