package com.example.wake_up_bird.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.wake_up_bird.GlobalApplication
import com.example.wake_up_bird.R
import com.example.wake_up_bird.presentation.ui.base.NavigationActivity
import com.example.wake_up_bird.presentation.ui.login.LoginActivity
import com.example.wake_up_bird.presentation.ui.recognition.RecognitionFragment
import com.example.wake_up_bird.presentation.ui.rest_time.RestTimeFragment
import com.example.wake_up_bird.presentation.ui.room.RoomFragment
import com.example.wake_up_bird.presentation.ui.statistic.StatisticFragment
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "FirebaseService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "new Token: $token")

        val pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("token", token).apply()
        editor.commit()
        Log.i("로그: ", "토큰 저장 성공")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "fcm title: ${remoteMessage.notification?.title}")
        Log.d(TAG, "fcm message: ${remoteMessage.notification?.body}")

        val app = applicationContext as GlobalApplication
        if (!app.isInForeground) {
            sendNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
        } else {
            val currentActivity = app.currentActivity
            if (currentActivity != null) {
                Log.d(TAG, "App is in foreground, current activity: ${currentActivity::class.java.simpleName}")
                Handler(Looper.getMainLooper()).post {
                    if (currentActivity is NavigationActivity) {
                        val activeFragmentTag = currentActivity.activeFragmentTag
                        currentActivity.recreate()
                        Handler(Looper.getMainLooper()).post {
                            if (activeFragmentTag != null) {
                                when (activeFragmentTag) {
                                    "statistic_fragment" -> currentActivity.setFragment("statistic_fragment", StatisticFragment())
                                    "room_fragment" -> currentActivity.setFragment("room_fragment", RoomFragment())
                                    "rest_time_fragment" -> currentActivity.setFragment("rest_time_fragment", RestTimeFragment())
                                    "recognition_fragment" -> currentActivity.setFragment("recognition_fragment", RecognitionFragment())
                                }
                            }
                        }
                        Log.d(TAG, "NavigationActivity refreshed")
                    }
                }
            } else {
                Log.d(TAG, "App is in foreground, but current activity is null")
            }
        }
    }

    private fun sendNotification(title: String?, message: String?) {
        val channelId = "fcm_default_channel"
        createNotificationChannel(channelId)

        val notificationId: Int = (System.currentTimeMillis() / 7).toInt()
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Default channel for app notifications"
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}