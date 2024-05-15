package com.example.wake_up_bird.data

import android.net.Uri

data class Certification(
    var userName: String,
    var profileImageUrl:String,
    var imageUrl: Uri,
    var certifiedTime: String,
) {
}