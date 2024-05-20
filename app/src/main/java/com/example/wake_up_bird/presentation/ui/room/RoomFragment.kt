package com.example.wake_up_bird.presentation.ui.room

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wake_up_bird.databinding.RoomBinding
import com.example.wake_up_bird.presentation.ui.capture.CaptureActivity

class RoomFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:Bundle?) : View?{
        val binding = RoomBinding.inflate(inflater, container, false )
        binding.certifyButton.setOnClickListener{
            val intent = Intent(getActivity(), CaptureActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }
}