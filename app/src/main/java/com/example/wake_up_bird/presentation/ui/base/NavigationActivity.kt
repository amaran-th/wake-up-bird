package com.example.wake_up_bird.presentation.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.wake_up_bird.R
import com.example.wake_up_bird.databinding.NavigationBinding
import com.example.wake_up_bird.presentation.ui.recognition.RecognitionFragment
import com.example.wake_up_bird.presentation.ui.rest_time.RestTimeFragment
import com.example.wake_up_bird.presentation.ui.room.RoomFragment
import com.example.wake_up_bird.presentation.ui.statistic.StatisticFragment

private const val TAG_STATISTIC = "statistic_fragment"
private const val TAG_ROOM = "room_fragment"
private const val TAG_REST_TIME = "rest_time_fragment"
private const val TAG_RECOGNITION = "recognition_fragment"

class NavigationActivity : AppCompatActivity() {
    private lateinit var binding : NavigationBinding
    var activeFragmentTag: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activeFragmentTag = savedInstanceState?.getString("activeFragmentTag") ?: TAG_ROOM

        setFragment(activeFragmentTag ?: TAG_ROOM, RoomFragment())

        binding.navigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.statistic -> setFragment(TAG_STATISTIC, StatisticFragment())
                R.id.room -> setFragment(TAG_ROOM, RoomFragment())
                R.id.rest_time -> setFragment(TAG_REST_TIME, RestTimeFragment())
                R.id.recognition -> setFragment(TAG_RECOGNITION, RecognitionFragment())
            }
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("activeFragmentTag", activeFragmentTag)
    }

    fun setFragment(tag: String, fragment: Fragment) {
        val manager: FragmentManager = supportFragmentManager
        val fragTransaction = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null) {
            fragTransaction.add(R.id.mainFrameLayout, fragment, tag)
        }

        val statistic = manager.findFragmentByTag(TAG_STATISTIC)
        val room = manager.findFragmentByTag(TAG_ROOM)
        val restTime = manager.findFragmentByTag(TAG_REST_TIME)
        val recognition = manager.findFragmentByTag(TAG_RECOGNITION) // 추가

        if (statistic != null) {
            fragTransaction.hide(statistic)
        }

        if (room != null) {
            fragTransaction.hide(room)
        }

        if (restTime != null) {
            fragTransaction.hide(restTime)
        }

        if (recognition != null) {
            fragTransaction.hide(recognition)
        }

        when (tag) {
            TAG_STATISTIC -> if (statistic != null) fragTransaction.show(statistic)
            TAG_ROOM -> if (room != null) fragTransaction.show(room)
            TAG_REST_TIME -> if (restTime != null) fragTransaction.show(restTime)
            TAG_RECOGNITION -> if (recognition != null) fragTransaction.show(recognition)
        }

        fragTransaction.commitAllowingStateLoss()
        activeFragmentTag = tag
    }
}