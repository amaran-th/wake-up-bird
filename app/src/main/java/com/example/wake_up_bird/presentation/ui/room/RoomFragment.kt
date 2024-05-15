package com.example.wake_up_bird.presentation.ui.room

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.example.wake_up_bird.data.Certification
import com.example.wake_up_bird.databinding.RoomBinding
import com.example.wake_up_bird.presentation.ui.capture.CaptureActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

class RoomFragment: Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var upref: SharedPreferences
    private lateinit var storage: FirebaseStorage
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:Bundle?) : View?{
        val binding = RoomBinding.inflate(inflater, container, false )
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        var datas:List<Certification> = listOf()
        upref=getActivity()?.getSharedPreferences("upref",Activity.MODE_PRIVATE)?:return binding.root
        CoroutineScope(Dispatchers.Main).launch {
            val roomCertifications = db.collection("certification")
                .whereEqualTo("room_id", upref.getString("room_id", ""))
                .whereEqualTo("certified_date", SimpleDateFormat("yyyy-MM-dd").format(Date()))
                .orderBy("certified_time")
                .get()
                .await()
            datas = roomCertifications.documents.map { document ->
                val user=db.collection("user")
                    .document(document.getString("user_id") ?: "")
                    .get()
                    .await()

                val imageUrl=storage.reference.child("images").child(document.getString("image_name")?:"").downloadUrl.await()
                Certification(
                    user.getString("nickname") ?: "",
                    user.getString("image_url") ?: "",
                    imageUrl,
                    document.getString("certified_time") ?: ""
                )
            }
            Log.d(TAG,datas.toString())
            binding.certifications.layoutManager=LinearLayoutManager(activity)
            binding.certifications.adapter=CertificationAdapter(datas)
        }


        binding.certifyButton.setOnClickListener{
            val intent = Intent(activity, CaptureActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }
}