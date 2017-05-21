package com.dobar.dragos.thaifuss

import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import com.dobar.dragos.thaifuss.camera.preview.CameraPreview
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.reactivex.Observable
import io.reactivex.functions.Function3
import io.reactivex.functions.BiFunction
import org.funktionale.option.Option
import org.funktionale.option.getOrElse
import org.funktionale.option.orElse

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    val cameraPermission: BehaviorSubject<Boolean> = BehaviorSubject.create<Boolean>()
    val fileWritePermission: BehaviorSubject<Boolean> = BehaviorSubject.create<Boolean>()
    val fileReadPermission: BehaviorSubject<Boolean> = BehaviorSubject.create<Boolean>()

    var mCamera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // test if it has camera and if not...
        val hasCamera = if (hasCameraHardware() != null) Option.Some(this@MainActivity) else Option.None

        // has camera and permissions(happy path)
        hasCamera
            .flatMap { hasPermissions(it) }
            .map {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
                openCamera(mCamera!!)
                mCamera
            }
            .map { camera: Camera? ->
                val capture_picture: Button = findViewById(R.id.button_capture) as Button
                capture_picture.setOnClickListener {
                    Toast.makeText(this@MainActivity, "xxxxxxxxx", 5).show()
                    //            camera!!.takePicture(null, null, { data, camera ->
//                var file = getOutputMediaFile(MEDIA_TYPE_IMAGE)
//                val fos = FileOutputStream(file)
//                fos.write(data)
//                fos.close()
//                // because do not want
////                mCamera!!.startPreview()
//            })
                }
            }

        // has camera but no permissions
        hasCamera
            .flatMap { hasNoPermissions(it) }
            .map {
                requestPermissions(it, CAMERA)
                requestPermissions(it, READ_EXTERNAL_STORAGE)
                requestPermissions(it, WRITE_EXTERNAL_STORAGE)
            }

        // no camera
        hasCamera
            .getOrElse {
                Toast.makeText(this@MainActivity, "cannot use iampedro without a camera...", 1).show()
            }

        // zip every permission subject
        val zipped: Observable<Boolean> = Observable.zip(
            cameraPermission, fileWritePermission, fileReadPermission, Function3 { a, b, c -> a && b && c })

        // observe when all of the permissions have been granted not not
        zipped.subscribe(
            {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
                openCamera(mCamera!!)
            },
            {
                Toast.makeText(this@MainActivity, "iampedro needs those permissions", 1).show()
            }
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (permissions.get(0)) {
            WRITE_EXTERNAL_STORAGE -> {
                if (grantResults[0] == 0) {
                    fileWritePermission.onNext(true)
                } else {
                    fileWritePermission.onError(Exception("no permissions to write files"))
                }
            }
            READ_EXTERNAL_STORAGE -> {
                if (grantResults[0] == 0) {
                    fileReadPermission.onNext(true)
                } else {
                    fileReadPermission.onError(Exception("no permissions to read files"))
                }
            }
            CAMERA -> {
                if (grantResults[0] == 0) {
                    cameraPermission.onNext(true)
                } else {
                    cameraPermission.onError(Exception("no permissions to use camera"))
                }
            }
        }
    }

    override fun onDestroy() {
        mCamera?.stopPreview()
    }

    override fun onPause() {
        super.onPause()
        mCamera?.stopPreview()
    }

    private fun requestPermissions(activity: Activity, permission: String) {
        ActivityCompat.requestPermissions(activity, arrayOf<String>(permission), 201)
    }

    private fun openCamera(camera: Camera) {
        (findViewById(R.id.camera_preview) as FrameLayout)?.addView(CameraPreview(this, camera))
    }

    private fun hasCameraHardware(): String? {
        var backCameraId: String? = null
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        for (cameraId in manager.cameraIdList)
        {
            val cameraCharacteristics = manager.getCameraCharacteristics(cameraId)
            val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing === CameraMetadata.LENS_FACING_BACK)
            {
                backCameraId = cameraId
                break
            }
        }
        return backCameraId
    }

    private fun hasPermissions(activity: Activity) :Option<Activity>
        = if (getHasPermissions(activity)) { Option.Some(activity) } else { Option.None }

    private fun hasNoPermissions(activity: Activity): Option<Activity>
        = if (!getHasPermissions(activity)) { Option.Some(activity) } else { Option.None }

    private fun getHasPermissions(activity: Activity): Boolean {
        return checkPermissionType(activity, CAMERA)
            && checkPermissionType(activity, READ_EXTERNAL_STORAGE)
            && checkPermissionType(activity, WRITE_EXTERNAL_STORAGE)
    }

    private fun checkPermissionType(activity: Activity, permissionType: String)
        = ContextCompat.checkSelfPermission(activity, permissionType) == PackageManager.PERMISSION_GRANTED

    private fun getCameraInstance():Camera? {
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
                Log.d("Necessity", "failed to create directory")
                return null
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var mediaFile:File = File(mediaStorageDir.getPath() + File.separator +
            "IMG_" + timeStamp + ".jpg")

        return mediaFile
    }
}
