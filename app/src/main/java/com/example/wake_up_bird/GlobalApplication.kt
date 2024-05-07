package com.example.wake_up_bird

import android.app.Application
import com.example.wake_up_bird.presentation.ui.login.Constants
import com.kakao.sdk.common.KakaoSdk
class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Kakao Sdk 초기화
        KakaoSdk.init(this, Constants.APP_KEY)
    }
}