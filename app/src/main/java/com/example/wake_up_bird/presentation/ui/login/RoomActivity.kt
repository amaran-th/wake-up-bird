package com.example.wake_up_bird.presentation.ui.login

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wake_up_bird.R
import com.example.wake_up_bird.databinding.RoomBinding
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

class RoomActivity:AppCompatActivity() {
    private val binding by lazy { RoomBinding.inflate(layoutInflater) }
    lateinit var upref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        upref = getSharedPreferences("upref", Activity.MODE_PRIVATE)

        var textView = findViewById<TextView>(R.id.roomId)
        textView.text = "${upref.getString("id","")}&${upref.getString("room_id","")}"
    }
}