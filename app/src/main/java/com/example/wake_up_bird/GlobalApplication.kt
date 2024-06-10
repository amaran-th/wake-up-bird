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

    var currentActivity: Activity? = null
        private set

    override fun onCreate() {
        super.onCreate()
        // Kakao Sdk 초기화
        KakaoSdk.init(this, Constants.LOGIN_APP_KEY)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
                Log.d("GlobalApplication", "Activity started: ${activity::class.java.simpleName}")
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
                Log.d("GlobalApplication", "Activity resumed: ${activity::class.java.simpleName}")
                if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    isInForeground = true
                    Log.d("GlobalApplication", "App is in foreground")
                }
            }

            override fun onActivityPaused(activity: Activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations
                Log.d("GlobalApplication", "Activity paused: ${activity::class.java.simpleName}")
            }

            override fun onActivityStopped(activity: Activity) {
                Log.d("GlobalApplication", "Activity stopped: ${activity::class.java.simpleName}")
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    isInForeground = false
                    Log.d("GlobalApplication", "App is in background")
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                Log.d("GlobalApplication", "Activity destroyed: ${activity::class.java.simpleName}")
                if (currentActivity === activity) {
                    currentActivity = null
                    Log.d("GlobalApplication", "Current activity set to null")
                }
            }
        })
    }
}