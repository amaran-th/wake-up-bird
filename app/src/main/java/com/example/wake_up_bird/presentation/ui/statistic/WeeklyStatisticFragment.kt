package com.example.wake_up_bird.presentation.ui.statistic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wake_up_bird.R
import com.example.wake_up_bird.databinding.StatisticBinding
import com.example.wake_up_bird.databinding.WeeklyStatisticBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeeklyStatisticFragment: Fragment() {

    private lateinit var binding: WeeklyStatisticBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=WeeklyStatisticBinding.inflate(inflater, container, false)
        val now = Date()
        val monday = getMonday(now)

        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val startDate = dateFormat.format(monday.time)

        monday.add(Calendar.DAY_OF_MONTH, 6)
        val endDate = dateFormat.format(monday.time)

        binding.weekRange.text = "$startDate~$endDate"

        return binding.root
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