package com.example.wake_up_bird.presentation.ui.statistic

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class StatisticPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity){
    private val fragments: ArrayList<Fragment> = ArrayList()

    // 페이지 개수
    override fun getItemCount(): Int {
        return fragments.size
    }

    /*
     * 파라미터 : position값, 반환 값 : Fragment 인스턴스
     */
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)
        notifyItemInserted(fragments.size - 1)
    }

    fun removeFragment() {
        fragments.removeLast()
        notifyItemRemoved(fragments.size)
    }
}