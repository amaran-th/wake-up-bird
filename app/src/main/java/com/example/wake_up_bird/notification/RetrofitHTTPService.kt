package com.example.wake_up_bird.notification

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.io.IOException

private const val TAG = "Retrofit"

interface FcmApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/projects/wake-up-bird-e0c84/messages:send")
    fun sendNotification(
        @Header("Authorization") authHeader: String,
        @Body body: FcmMessage
    ): Call<Void>
}

data class FcmMessage(
    val message: Message
)

data class Message(
    val token: String?,
    val notification: Notification
)

data class Notification(
    val title: String,
    val body: String
)

object RetrofitClient {
    private const val BASE_URL = "https://fcm.googleapis.com/"

    val instance: FcmApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(FcmApiService::class.java)
    }
}

@Throws(IOException::class)
suspend fun getAccessToken(context: Context): String {
    return withContext(Dispatchers.IO) {
        val inputStream = context.assets.open("service_account.json")
        val googleCredentials = GoogleCredentials
            .fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        googleCredentials.getAuthenticationType()
        googleCredentials.refreshIfExpired()
        Log.d("AccessToken", googleCredentials.accessToken.tokenValue)
        googleCredentials.accessToken.tokenValue
    }
}

fun sendPushNotification(context: Context, tokens: List<String>, title: String, body: String) {
    val apiService = RetrofitClient.instance
    val notification = Notification(title = title, body = body)

    runBlocking {
        val accessToken = getAccessToken(context)

        tokens.forEach { token ->
            val message = Message(token = token, notification = notification)
            val fcmMessage = FcmMessage(message = message)

            val call = apiService.sendNotification("Bearer $accessToken", fcmMessage)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Notification sent successfully")
                    } else {
                        Log.e(TAG, "Notification failed with response: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e(TAG, "Notification failed with error: ${t.message}")
                }
            })
        }
    }
}