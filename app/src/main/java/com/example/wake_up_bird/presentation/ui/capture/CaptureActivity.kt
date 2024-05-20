package com.example.wake_up_bird.presentation.ui.capture

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import android.Manifest
import android.app.Activity
import android.content.SharedPreferences
import android.net.Uri
import androidx.camera.core.AspectRatio.RATIO_4_3
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.wake_up_bird.databinding.CaptureBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class CaptureActivity: AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val REQUIRED_PERMISSIONS = Manifest.permission.CAMERA
    }
    lateinit var upref: SharedPreferences
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore
    private lateinit var photoFile: File
    private var imageCapture: ImageCapture? = null
    private lateinit var mBinding: CaptureBinding
    private var camera: Camera? = null
    private var cameraController: CameraControl? = null
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        upref=getSharedPreferences("upref", Activity.MODE_PRIVATE)
        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()
        mBinding = CaptureBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // 카메라 권한 확인
        if (allPermissionsGranted()) {
            startCamera() // 카메라 실행
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(REQUIRED_PERMISSIONS), REQUEST_CODE_PERMISSIONS)
        }

        photoFile = File(applicationContext.cacheDir, "newImage.jpg")

        mBinding.btnCapture.setOnClickListener {
            // 임시 파일 삭제
            photoFile.delete()
            // 사진 캡처 하기
            takePhoto()
        }
        mBinding.retryButton.setOnClickListener{
            mBinding.ivCapture.visibility = View.INVISIBLE
            mBinding.viewFinder.visibility = View.VISIBLE
            mBinding.afterCapture.visibility=View.GONE
            mBinding.btnCapture.visibility=View.VISIBLE
        }
        mBinding.certifyButton.setOnClickListener{
            val now = Date()
            mBinding.certifyButton.isEnabled=false
            var timeStamp= SimpleDateFormat("yyyy-MM-dd").format(Date())
            var imageFileName = timeStamp+"_"+upref.getString("id","none")+".png"
            var storageRef = storage.reference.child("images").child(imageFileName)
            storageRef.putFile(Uri.fromFile(photoFile))
                .addOnSuccessListener {
                    certify(now, imageFileName)
                }.addOnFailureListener{
                    Toast.makeText(this,"인증에 실패하였습니다.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().setTargetAspectRatio(RATIO_4_3).build()
            preview.setSurfaceProvider(mBinding.viewFinder.surfaceProvider)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageCapture=ImageCapture.Builder().build()
            try{
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
                cameraController = camera!!.cameraControl
                cameraController!!.setZoomRatio(1F) // 1x Zoom
            }catch(exc: Exception){
                println("에러 $exc")
            }
            preview.setSurfaceProvider(mBinding.viewFinder.surfaceProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val mImageCapture = imageCapture ?: return

        // ImageCapture.OutputFileOptions는 새로 캡처한 이미지를 저장하기 위한 옵션
        // 저장 위치 및 메타데이터를 구성하는데 사용
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // takePicture를 통해 사진을 촬영.
        mImageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Glide를 통해서 ImageView에 이미지 캡처한 이미지 설정
                    Glide.with(this@CaptureActivity)
                        .load(outputFileResults.savedUri)
                        .apply(
                            // 이전 이미지를 재활용하지 않도록 처리
                            RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                        )
                        .into(mBinding.ivCapture)

                    // 이미지 그린 후 UI 변경
                    mBinding.ivCapture.visibility = View.VISIBLE
                    mBinding.viewFinder.visibility = View.INVISIBLE
                    mBinding.afterCapture.visibility=View.VISIBLE
                    mBinding.btnCapture.visibility=View.GONE
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(applicationContext, "사진 촬영에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    private fun certify(now:Date, imageName:String){
        val userId:String? = upref.getString("id","none")
        val nowDate=SimpleDateFormat("yyyy-MM-dd").format(now)
        val nowTime=SimpleDateFormat("HH:mm:ss").format(now)
        db.collection("certification").whereEqualTo("user_id",userId).whereEqualTo("certified_date",nowDate)
            .get()
            .addOnSuccessListener {
                result->
                if(result.isEmpty){
                    val certification = mapOf(
                        "image_name" to imageName,
                        "certified_date" to nowDate,
                        "certified_time" to nowTime,
                        "user_id" to upref.getString("id","none"),
                        "room_id" to upref.getString("room_id","none")
                    )
                    val colRef: CollectionReference = db.collection("certification")
                    val docRef: Task<DocumentReference> = colRef.add(certification)
                    docRef.addOnSuccessListener {
                        Toast.makeText(this,"인증에 성공하였습니다.",Toast.LENGTH_SHORT).show()
                        intent.putExtra("certified_time",nowTime)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }else{
                    db.collection("certification")
                        .document(result.documents.get(0).id)
                        .update("certified_time", nowTime)
                        .addOnSuccessListener {
                            Toast.makeText(this,"재인증에 성공하였습니다.",Toast.LENGTH_SHORT).show()
                            intent.putExtra("certified_time",nowTime)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                }

            }

    }

    /**
     * 권한 요청 결과를 판단(requestPermissions에 의해 호출)
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults:
        IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "접근 권한이 허용되지 않아 카메라를 실행할 수 없습니다. 설정에서 접근 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 카메라 권한 체크
     */
    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}