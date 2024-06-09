package com.example.wake_up_bird

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import android.app.Activity
import android.os.Bundle
import android.util.Log

class GlobalApplication : Application() {
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    var isInForeground = false
        private set

    override fun onCreate() {
        super.onCreate()
        // Kakao Sdk 초기화
        KakaoSdk.init(this, Constants.LOGIN_APP_KEY)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    isInForeground = true
                    Log.d("GlobalApplication", "App is in foreground")
                }
            }

            override fun onActivityPaused(activity: Activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations
            }

            override fun onActivityStopped(activity: Activity) {
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    isInForeground = false
                    Log.d("GlobalApplication", "App is in background")
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}