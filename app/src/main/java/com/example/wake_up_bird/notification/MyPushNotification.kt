package com.example.wake_up_bird.notification

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

suspend fun getRoomMembersTokens(roomId: String, newMemberName: String): List<String> {
    val firestore = FirebaseFirestore.getInstance()
    val membersTokens = mutableListOf<String>()

    try {
        val querySnapshot = firestore.collection("user")
            .whereEqualTo("room_id", roomId)
            .get()
            .await()

        for (document in querySnapshot.documents) {
            val nickname = document.getString("nickname")
            val token = document.getString("device_token")
            if ((nickname != newMemberName) && (token != null)) {
                membersTokens.add(token)
            }
        }

        if (membersTokens.isNotEmpty()) {
            membersTokens.forEachIndexed { index, token ->
                Log.d("RoomMembersTokens", "Token $index: $token")
            }
        } else {
            Log.d("RoomMembersTokens", "No tokens found for the room members.")
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return membersTokens
}

fun sendEnterNotification(context: Context, roomId: String, newMemberName: String) {
    val membersTokens: List<String> = runBlocking {
        getRoomMembersTokens(roomId, newMemberName)
    }

    val title = "입장 알림"
    val body = "$newMemberName" + "님이 인증방에 입장하였습니다!"

    runBlocking {
        sendPushNotification(context, membersTokens, title, body)
    }
}

fun sendCertiNotification(context: Context, roomId: String, newMemberName: String) {
    val membersTokens: List<String> = runBlocking {
        getRoomMembersTokens(roomId, newMemberName)
    }

    val title = "인증 알림"
    val body = "$newMemberName" + "님이 기상 인증에 성공하였습니다!"

    runBlocking {
        sendPushNotification(context, membersTokens, title, body)
    }
}

fun sendReCertiNotification(context: Context, roomId: String, newMemberName: String) {
    val membersTokens: List<String> = runBlocking {
        getRoomMembersTokens(roomId, newMemberName)
    }

    val title = "인증 알림"
    val body = "$newMemberName" + "님이 기상 재인증에 성공하였습니다!"

    runBlocking {
        sendPushNotification(context, membersTokens, title, body)
    }
}