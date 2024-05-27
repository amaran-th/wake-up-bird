package com.example.wake_up_bird.presentation.ui.room

import android.content.ContentValues.TAG
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wake_up_bird.R
import com.example.wake_up_bird.data.User
import com.example.wake_up_bird.databinding.UserItemBinding

class UserAdapterViewHolder(val binding: UserItemBinding): RecyclerView.ViewHolder(binding.root)

//항목 구성자
class UserAdapter(val datas:List<User>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = datas.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = UserAdapterViewHolder(
        UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        //항목 뷰를 가지는 뷰 홀더를 준비하기 위해 자동 호출
        val binding = (holder as UserAdapterViewHolder).binding

        Glide.with(binding.profile.context)
            .load(datas[position].profileImageUrl)
            .error(R.mipmap.ic_fallback)
            .fallback(R.mipmap.ic_fallback)
            .circleCrop()
            .into(binding.profile)
        binding.userName.text = datas[position].nickName
        if(datas[position].role.equals("manager")){
            Log.d(TAG,datas[position].role)
            binding.iconManager.visibility=View.VISIBLE
        }
        if(datas[position].isMe){
            binding.iconMe.visibility=View.VISIBLE
            Log.d(TAG, datas[position].isMe.toString())
        }
    }
}