package com.example.wake_up_bird.presentation.ui.login

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wake_up_bird.Constants
import com.example.wake_up_bird.databinding.LoginBinding
import com.example.wake_up_bird.presentation.ui.init.InitActivity
import com.example.wake_up_bird.presentation.ui.base.NavigationActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
class LoginActivity: AppCompatActivity() , View.OnClickListener {

    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var upref: SharedPreferences
    private val binding by lazy { LoginBinding.inflate(layoutInflater) }

    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(TAG, "로그인 실패 $error")
        } else if (token != null) {
            Log.d(TAG, "로그인 성공 ${token.accessToken}")
            nextMainActivity()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnLogin.id -> {
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                    UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                        if (error != null) {
                            Log.e(TAG, "로그인 실패 $error")
                            if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                                return@loginWithKakaoTalk
                            } else {
                                UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback)
                            }
                        } else if (token != null) {
                            Log.d(TAG, "로그인 성공 ${token.accessToken}")
                            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            nextMainActivity()
                        }
                    }
                } else {
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        upref = getSharedPreferences("upref", Activity.MODE_PRIVATE)

        Log.d(TAG, "keyhash : ${Utility.getKeyHash(this)}")

        KakaoSdk.init(this, Constants.LOGIN_APP_KEY)
        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error == null) {
                    nextMainActivity()
                }
            }
        }

        setContentView(binding.root)

        binding.btnLogin.setOnClickListener(this)
    }

    private fun nextMainActivity() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패 $error")
            } else if (user != null) {
                Log.d(TAG, "사용자 정보 요청 성공 : $user")

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
                        Log.d(TAG,"Sign Up Success : " + "${documentReference.id} \n")
                        val ueditor=upref.edit()
                        ueditor.putString("id",documentReference.id)
                        ueditor.apply()
                        startActivity(Intent(this, InitActivity::class.java)) // TODO: 초기설정 화면으로 이동
                        finish()
                    }
                    docRef.addOnFailureListener {
                        Log.d(TAG,"Sign Up Failure \n")
                    }
                }else{
                    // 로그인
                    val document = result.documents.get(0)
                    val ueditor=upref.edit()
                    ueditor.putString("id",document.id)
                    ueditor.apply()
                    var roomId:String? = document.getString("room_id")
                    if(roomId!=null){
                        // 인증방이 있을 때
                        val ueditor=upref.edit()
                        ueditor.putString("room_id",roomId)
                        ueditor.apply()
                        startActivity(Intent(this, NavigationActivity::class.java)) // TODO: 인증방으로 이동
                        finish()
                    }else{
                        startActivity(Intent(this, InitActivity::class.java)) // TODO: 초기설정 화면으로 이동
                        finish()
                    }

                }
            }
            .addOnFailureListener {
                Log.d(TAG,"Sign In Failure \n")
            }
    }
}