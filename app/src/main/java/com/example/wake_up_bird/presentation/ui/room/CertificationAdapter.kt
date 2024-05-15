package com.example.wake_up_bird.presentation.ui.room

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wake_up_bird.Constants.IMAGE_PATH_PREFIX
import com.example.wake_up_bird.Constants.IMAGE_PATH_SUFFIX
import com.example.wake_up_bird.R
import com.example.wake_up_bird.data.Certification
import com.example.wake_up_bird.databinding.CertificationItemBinding
import com.google.firebase.storage.FirebaseStorage

class CertificationAdapterViewHolder(val binding: CertificationItemBinding): RecyclerView.ViewHolder(binding.root)

//항목 구성자
class CertificationAdapter(val datas:List<Certification>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = datas.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = CertificationAdapterViewHolder(
        CertificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        //항목 뷰를 가지는 뷰 홀더를 준비하기 위해 자동 호출
        val binding = (holder as CertificationAdapterViewHolder).binding

        Glide.with(binding.profile.context)
            .load(datas[position].profileImageUrl)
            .error(R.drawable.ic_logo)
            .fallback(R.drawable.ic_logo)
            .circleCrop()
            .into(binding.profile)
        binding.userName.text = datas[position].userName
        binding.certifiedTime.text = datas[position].certifiedTime.slice(0..4)

        Glide.with(binding.certifyImage.context)
            .load(datas[position].imageUrl)
            .error(R.drawable.ic_logo)
            .fallback(R.drawable.ic_logo)
            .into(binding.certifyImage)
    }
}