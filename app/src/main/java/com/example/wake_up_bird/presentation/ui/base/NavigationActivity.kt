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

        setFragment(activeFragmentTag ?: TAG_ROOM)

        binding.navigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.statistic -> setFragment(TAG_STATISTIC)
                R.id.room -> setFragment(TAG_ROOM)
                R.id.rest_time -> setFragment(TAG_REST_TIME)
                R.id.recognition -> setFragment(TAG_RECOGNITION)
            }
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("activeFragmentTag", activeFragmentTag)
    }

    fun setFragment(tag: String) {
        val manager: FragmentManager = supportFragmentManager
        val fragTransaction = manager.beginTransaction()

        manager.fragments.forEach { fragment ->
            fragTransaction.hide(fragment)
        }

        val newFragment = when (tag) {
            TAG_STATISTIC -> StatisticFragment()
            TAG_ROOM -> RoomFragment()
            TAG_REST_TIME -> RestTimeFragment()
            TAG_RECOGNITION -> RecognitionFragment()
            else -> throw IllegalArgumentException("Invalid fragment tag")
        }

        fragTransaction.add(R.id.mainFrameLayout, newFragment, tag)
        fragTransaction.commitAllowingStateLoss()
        activeFragmentTag = tag
    }
}