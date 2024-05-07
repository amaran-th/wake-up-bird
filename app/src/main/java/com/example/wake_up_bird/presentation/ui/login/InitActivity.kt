package com.example.wake_up_bird.presentation.ui.login

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wake_up_bird.databinding.InitBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
class InitActivity : AppCompatActivity() {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var upref:SharedPreferences
    private val binding by lazy { InitBinding.inflate(layoutInflater) }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setContentView(binding.root)
        upref = getSharedPreferences("upref", Activity.MODE_PRIVATE)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(Constants.TAG, "사용자 정보 요청 실패 $error")
            } else if (user != null) {
                Log.d(Constants.TAG, "사용자 정보 요청 성공 : $user")

                searchUser(user.id.toString(), user.kakaoAccount?.profile?.profileImageUrl, user.kakaoAccount?.profile?.nickname)
            }
        }
    }

    fun searchUser(id:String, imageUrl:String?, nickname:String?){
        db.collection("user")
            .whereEqualTo("sns_id", id)
            .get()
            .addOnSuccessListener{
                    result ->
                        if(result.isEmpty){
                            // 회원가입
                            val user = mapOf(
                                "image_url" to imageUrl,
                                "nickname" to nickname,
                                "room_id" to null,
                                "sns_id" to id
                            )
                            val colRef: CollectionReference = db.collection("user")
                            val docRef: Task<DocumentReference> = colRef.add(user)
                            docRef.addOnSuccessListener { documentReference ->
                                Log.d(Constants.TAG,"Sign Up Success : " + "${documentReference.id} \n")
                                val ueditor=upref.edit()
                                ueditor.putString("id",documentReference.id)
                                ueditor.apply()
                            }
                            docRef.addOnFailureListener {
                                Log.d(Constants.TAG,"Sign Up Failure \n")
                            }
                        }else{
                            val document = result.documents.get(0)
                            val ueditor=upref.edit()
                            ueditor.putString("id",document.id)
                            ueditor.apply()
                            var roomId:String? = document.getString("room_id")
                            if(roomId!=null){
                                val ueditor=upref.edit()
                                ueditor.putString("room_id",roomId)
                                ueditor.apply()
                                startActivity(Intent(this, RoomActivity::class.java))
                                finish()
                            }
                        }
            }
            .addOnFailureListener {
                Log.d(Constants.TAG,"Sign In Failure \n")
            }
    }
}