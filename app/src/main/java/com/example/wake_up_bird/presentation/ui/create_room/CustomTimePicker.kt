import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import com.example.wake_up_bird.R
import java.util.Calendar

class CustomTimePickerDialog(
    context: Context,
    private val listener: OnTimeSetListener,
    private val hour: Int,
    private val minute: Int
) : Dialog(context) {

    private var amPm = Calendar.AM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_time_picker)
        window?.setBackgroundDrawableResource(R.drawable.rounded_corner_lv2)

        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        timePicker.hour = hour
        timePicker.minute = minute

        val confirmButton = findViewById<Button>(R.id.confirmButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        confirmButton.setOnClickListener {
            val selectedHour = if (amPm == Calendar.AM) timePicker.hour else (timePicker.hour % 12) + 12
            val selectedMinute = timePicker.minute
            listener.onTimeSet(selectedHour, selectedMinute)
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    interface OnTimeSetListener {
        fun onTimeSet(hourOfDay: Int, minute: Int)
    }
}