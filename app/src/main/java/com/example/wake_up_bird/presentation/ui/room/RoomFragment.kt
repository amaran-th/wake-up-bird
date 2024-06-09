package com.example.wake_up_bird.presentation.ui.room

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wake_up_bird.R
import com.example.wake_up_bird.data.Certification
import com.example.wake_up_bird.data.User
import com.example.wake_up_bird.databinding.RoomBinding
import com.example.wake_up_bird.presentation.ui.capture.CaptureActivity
import com.example.wake_up_bird.presentation.ui.init.InitActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date

class RoomFragment: Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var upref: SharedPreferences
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: RoomBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:Bundle?) : View?{
        binding = RoomBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        upref=getActivity()?.getSharedPreferences("upref",Activity.MODE_PRIVATE)?:return binding.root
        CoroutineScope(Dispatchers.Main+ Job()).launch {
            loadUserCount()
            loadCertificationStatus()
            loadCertifications()
            loadDrawer()

        }
        binding.navigationView.getOutRoom.setOnClickListener{
            binding.drawer.closeDrawers()
            binding.warningDialog.visibility=View.VISIBLE
        }

        binding.getOutButton.setOnClickListener {
            db.collection("user")
                .document(upref.getString("id","")?:"")
                .update("role",null,"room_id",null)
                .addOnSuccessListener {
                    upref.edit().putString("room_id",null)
                    val intent = Intent(activity, InitActivity::class.java) //fragment라서 activity intent와는 다른 방식
                    startActivity(intent)
                }
        }

        binding.cancelButton.setOnClickListener {
            binding.warningDialog.visibility=View.GONE
        }

        binding.nowDate.text = SimpleDateFormat("yyyy년 M월 dd일").format(Date())+"자 인증 현황"

        binding.certifyButton.setOnClickListener{
            val intent = Intent(activity, CaptureActivity::class.java)
            startActivityForResult(intent, 1)
        }
        return binding.root
    }

    private suspend fun loadDrawer() {
        val users = db.collection("user")
            .whereEqualTo("room_id", upref.getString("room_id",""))
            .orderBy("role", Query.Direction.DESCENDING)
            .get().await()
        val datas = users.documents.map { user ->
            User(
                user.id,
                user.getString("nickname")!!,
                user.getString("image_url")!!,
                user.getString("role")!!,
                user.id.equals(upref.getString("id", ""))
            )
        }
        binding.navigationView.users.layoutManager = LinearLayoutManager(activity)
        binding.navigationView.users.adapter = UserAdapter(datas)

        val room = db.collection("room")
            .document(upref.getString("room_id", "") ?: "")
            .get().await()
        binding.navigationView.attendanceTime.text =
            "출석 인정 시간: " + room.getString("start_time")!! + " ~ " + room.getString("middle_time")!!
        binding.navigationView.latenessTime.text =
            "지각 인정 시간: " + room.getString("middle_time")!! + " ~ " + room.getString("end_time")!!
        binding.navigationView.inviteCodeCopy.setOnClickListener {
            copyToClipboard(room.getString("invite_code")!!)
        }
        binding.navigationView.passwordCopy.setOnClickListener {
            copyToClipboard(room.getLong("password")!!.toString())
        }
    }

    private suspend fun loadCertificationStatus() {
        val myCertification = db.collection("certification")
            .whereEqualTo("room_id", upref.getString("room_id", ""))
            .whereEqualTo("certified_date", SimpleDateFormat("yyyy-MM-dd").format(Date()))
            .whereEqualTo("user_id", upref.getString("id", ""))
            .get().await()
        val myRoom = db.collection("room")
            .document(upref.getString("room_id", "") ?: "")
            .get().await()
        val startTime = myRoom.getString("start_time")
        val middleTime = myRoom.getString("middle_time")
        val endTime = myRoom.getString("end_time")
        Log.d(TAG, upref.getString("room_id", "")!!)
        if (myCertification.isEmpty) {
            //미인증 상태
            val nowTime = SimpleDateFormat("HH:mm").format(Date())
            if (nowTime < startTime!!) {
                //대기
                updateWaitUI()
            } else if (endTime!! < nowTime) {
                //결석
                updateAbsenceUI()
            } else {
                updateCertifyUI()
            }
        } else {
            //인증 상태
            val certifiedTime =
                myCertification.documents.get(0).getString("certified_time")?.slice(0..4)

            if (startTime!! < certifiedTime!! && certifiedTime < middleTime!!) {
                //출석
                updateAttendenceUI(certifiedTime)
            } else if (middleTime!! < certifiedTime && certifiedTime < endTime!!) {
                //지각
                updateLatenessUI(certifiedTime)
            } else {
                db.collection("certification").document(myCertification.documents.get(0).id)
                    .delete()
            }
        }
    }

    private suspend fun loadUserCount() {
        val usersCount = db.collection("user")
            .whereEqualTo("room_id", upref.getString("room_id", ""))
            .get().await().size()
        val certifiedCount = db.collection("certification")
            .whereEqualTo("room_id", upref.getString("room_id", ""))
            .whereEqualTo("certified_date", SimpleDateFormat("yyyy-MM-dd").format(Date()))
            .get().await().size()
        val middleTime = db.collection("room")
            .document(upref.getString("room_id", "")?:"").get().await().getString("middle_time")!!
        val attendedCount = db.collection("certification")
            .whereEqualTo("room_id", upref.getString("room_id", ""))
            .whereEqualTo("certified_date", SimpleDateFormat("yyyy-MM-dd").format(Date()))
            .whereLessThan("certified_time",middleTime)
            .get().await().size()
        binding.allMemberCount.text = usersCount.toString() + "명 중 "
        binding.certifiedMemberCount.text=certifiedCount.toString()
        binding.certifyDayStatistic.text="출석: " +attendedCount.toString() + " • "+"지각: " +(certifiedCount-attendedCount).toString() + " • " + "결석: " + (usersCount-certifiedCount).toString()
    }

    private suspend fun loadCertifications() {
        val roomCertifications = db.collection("certification")
            .whereEqualTo("room_id", upref.getString("room_id", ""))
            .whereEqualTo("certified_date", SimpleDateFormat("yyyy-MM-dd").format(Date()))
            .orderBy("certified_time")
            .get()
            .await()
        var datas = roomCertifications.documents.map { document ->
            val user = db.collection("user")
                .document(document.getString("user_id") ?: "")
                .get()
                .await()

            val imageUrl = storage.reference.child("images")
                .child(document.getString("image_name") ?: "").downloadUrl.await()
            Certification(
                user.getString("nickname") ?: "",
                user.getString("image_url") ?: "",
                imageUrl,
                document.getString("certified_time") ?: ""
            )
        }
        binding.certifications.layoutManager = LinearLayoutManager(activity)
        binding.certifications.adapter = CertificationAdapter(datas)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, resultCode.toString())
        if(resultCode==Activity.RESULT_OK) {
            val certifiedTime = data?.getStringExtra("certified_time")!!
            Log.d(TAG, certifiedTime)
            CoroutineScope(Dispatchers.Main).launch {
                loadUserCount()
                loadCertificationStatus()
                loadCertifications()
            }
        }



    }
    private fun copyToClipboard(content:String){
        val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("copy pass",content) //label, 복사할 값
        clipboardManager.setPrimaryClip(clip)
    }

    private fun updateCertifyUI(){
        binding.certifyButton.setText("인증하기")
        binding.certifyButton.isEnabled = true
        binding.certifyButton.setTextColor(resources.getColor(R.color.black_color))
        binding.certifyButton.setBackgroundResource(R.drawable.room_certify)
    }
    private fun updateAttendenceUI(certifiedTime: String) {
        binding.certifyButton.setText(certifiedTime + " 인증 완료")
        binding.certifyButton.isEnabled = true
        binding.certifyButton.setTextColor(getResources().getColor(R.color.primary_color))
        binding.certifyButton.setBackgroundResource(R.drawable.room_attendance)
    }
    private fun updateLatenessUI(certifiedTime: String) {
        binding.certifyButton.setText(certifiedTime + " 지각")
        binding.certifyButton.isEnabled = true
        binding.certifyButton.setTextColor(resources.getColor(R.color.blue_color))
        binding.certifyButton.setBackgroundResource(R.drawable.room_lateness)
    }

    private fun updateAbsenceUI() {
        binding.certifyButton.setText("결석")
        binding.certifyButton.isEnabled = false
        binding.certifyButton.setTextColor(getResources().getColor(R.color.red_color))
        binding.certifyButton.setBackgroundResource(R.drawable.room_absence)
    }
    private fun updateWaitUI() {
        binding.certifyButton.setText("아직 인증 시간이 아닙니다.")
        binding.certifyButton.isEnabled = false
        binding.certifyButton.setTextColor(getResources().getColor(R.color.gray_color))
        binding.certifyButton.setBackgroundResource(R.drawable.room_wait)
    }
}