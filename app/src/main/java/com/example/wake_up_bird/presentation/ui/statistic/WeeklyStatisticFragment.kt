package com.example.wake_up_bird.presentation.ui.statistic

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.wake_up_bird.R
import com.example.wake_up_bird.databinding.WeeklyStatisticBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.opencensus.tags.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeeklyStatisticFragment: Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: WeeklyStatisticBinding
    private lateinit var upref: SharedPreferences
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    private var days: MutableList<Calendar> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=WeeklyStatisticBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        upref=getActivity()?.getSharedPreferences("upref", Activity.MODE_PRIVATE)?:return binding.root
        val now = Date()
        val day = getMonday(now)
        for(i in 0..6){
            days.add(day.clone() as Calendar)
            day.add(Calendar.DAY_OF_MONTH, 1)
        }

        updateWeekRange()
        binding.lastWeek.setOnClickListener{
            for(i in 0..6){
                days[i].add(Calendar.DAY_OF_MONTH, -7)
            }
            updateWeekRange()
            CoroutineScope(Dispatchers.Main+ Job()).launch {
                updateTable()
            }
        }
        binding.nextWeek.setOnClickListener{
            for(i in 0..6){
                days[i].add(Calendar.DAY_OF_MONTH, 7)
            }
            updateWeekRange()
            CoroutineScope(Dispatchers.Main+ Job()).launch {
                updateTable()
            }
        }
        CoroutineScope(Dispatchers.Main+ Job()).launch {
            updateTable()
        }

        return binding.root
    }

    private suspend fun updateTable() {
        val users = db.collection("user")
            .whereEqualTo("room_id", upref.getString("room_id", ""))
            .orderBy("role", Query.Direction.DESCENDING)
            .get().await()
        val myRoom = db.collection("room")
            .document(upref.getString("room_id", "") ?: "")
            .get().await()
        val certifications = db.collection("certification")
            .whereEqualTo("room_id", upref.getString("room_id", ""))
            .whereGreaterThanOrEqualTo(
                "certified_date",
                SimpleDateFormat("yyyy-MM-dd").format(days[0].time)
            )
            .whereLessThanOrEqualTo(
                "certified_date",
                SimpleDateFormat("yyyy-MM-dd").format(days[days.size - 1].time)
            )
            .get().await()

        val tableLayout = binding.statisticTable
        tableLayout.removeAllViews()
        var firstTableRow = TableRow(activity)
        firstTableRow.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        firstTableRow.addView(TextView(activity))
        for (user in users.documents) {
            var firstTextView = TextView(activity)
            firstTextView.text = user.getString("nickname")
            firstTableRow.addView(firstTextView)
        }
        tableLayout.addView(firstTableRow)

        for (nowDay in days) {
            var tableRow = TableRow(activity)
            tableRow.layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val certificationMap = certifications.documents.groupBy {
                it.getString("certified_date")
            }

            val firstTextView = TextView(activity)
            firstTextView.text = SimpleDateFormat("MM/dd").format(nowDay.time)
            tableRow.addView(firstTextView)
            for (user in users) {
                Log.d(TAG, certificationMap.keys.toString())
                val certification =
                    certificationMap[SimpleDateFormat("yyyy-MM-dd").format(nowDay.time)]
                        ?.find { c ->
                            c.getString("user_id") == user.id
                        }
                var textView = TextView(activity)
                if (nowDay.time.compareTo(Date())==-1&&certification == null) {
                    textView.text = "결석"
                    textView.setTextColor(resources.getColor(R.color.red_color))
                } else {
                    if (certification != null) {
                        if (myRoom.getString("middle_time")!! < certification.getString("certified_time")!!) {
                            textView.text = "지각"
                            textView.setTextColor(resources.getColor(R.color.blue_color))
                        } else {
                            textView.text = "출석"
                            textView.setTextColor(resources.getColor(R.color.primary_color))
                        }
                    }
                }
                tableRow.addView(textView)
            }
            tableLayout.addView(tableRow)
        }
    }

    private fun updateWeekRange() {
        val startDate = dateFormat.format(days[0].time)
        val endDate = dateFormat.format(days[days.size-1].time)

        binding.weekRange.text = "$startDate~$endDate"
    }

    fun getMonday(now:Date): Calendar {
        val eventDate = SimpleDateFormat("yyyy-MM-dd").format(now)
        val dateArray = eventDate.split("-").toTypedArray()

        val cal = Calendar.getInstance()
        cal[dateArray[0].toInt(), dateArray[1].toInt() - 1] = dateArray[2].toInt()

        // 일주일의 첫날을 일요일로 지정한다
        cal.firstDayOfWeek = Calendar.MONDAY

        // 시작일과 특정날짜의 차이를 구한다
        val dayOfWeek = cal[Calendar.DAY_OF_WEEK] - cal.firstDayOfWeek

        // 해당 주차의 첫째날을 지정한다
        cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek)

        val sf = SimpleDateFormat("yyyy.MM.dd")

        // 해당 주차의 첫째 날짜
        return cal
    }
}