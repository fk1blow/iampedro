package com.dobar.dragos.thaifuss

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.requestPermissions
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    var mCamera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create an instance of Camera
//        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)

        // Create our Preview view and set it as the content of our activity.
//        val cameraPreview = CameraPreview(this, mCamera!!)
//        val preview = findViewById(R.id.camera_preview) as FrameLayout
//        preview.addView(cameraPreview)


        requestPermissions(this@MainActivity, arrayOf<String>(android.Manifest.permission.CAMERA), 201)
        requestPermissions(this@MainActivity, arrayOf<String>(android.Manifest.permission.READ_EXTERNAL_STORAGE), 201)
        requestPermissions(this@MainActivity, arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 201)



        // add click listener on the main button
        val capture_picture: Button = findViewById(R.id.button_capture) as Button
        capture_picture.setOnClickListener {
            val toast = Toast.makeText(this@MainActivity, "sup, kamera", 1)
            toast.show()

//            mCamera!!.takePicture(null, null, { data, camera ->
//                var file = getOutputMediaFile(MEDIA_TYPE_IMAGE)
//                val fos = FileOutputStream(file)
//                fos.write(data)
//                fos.close()
//                // because do not want
////                mCamera!!.startPreview()
//            })
        }
    }

    private fun hasCameraHardware(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)

    private fun openCamera() {
        if (hasCameraHardware(applicationContext)) startActivity(Intent("android.media.action.IMAGE_CAPTURE"))
    }

    fun getCameraInstance():Camera? {
        var c: Camera?
        try
        {
            c = Camera.open() // attempt to get a Camera instance
        }
        catch (e:Exception) {
            c = null
            // Camera is not available (in use or does not exist)
        }
        return c // returns null if camera is unavailable
    }

    val MEDIA_TYPE_IMAGE = 1
    val MEDIA_TYPE_VIDEO = 2

    /** Create a file Uri for saving an image or video */
    private fun getOutputMediaFileUri(type:Int): Uri {
        return Uri.fromFile(getOutputMediaFile(type))
    }
    /** Create a File for saving an image or video */
    private fun getOutputMediaFile(type:Int):File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp")

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                Log.d("MyCameraApp", "failed to create directory")
                return null
            }
        }

        val storagePath = mediaStorageDir.path
        Log.d("x", "media storage path: $storagePath")

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var mediaFile:File = File(mediaStorageDir.getPath() + File.separator +
            "IMG_" + timeStamp + ".jpg")

        return mediaFile
    }
}
