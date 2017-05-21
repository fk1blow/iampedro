@file:Suppress("DEPRECATION")

package com.dobar.dragos.thaifuss.camera.flow

import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker.checkPermission
import android.util.Log
import org.funktionale.option.Option
import org.funktionale.option.orElse

fun start_flow(activity: Activity) {
    hasPermissions(activity)
            .orElse {
                requestPermissions(activity, CAMERA)
                requestPermissions(activity, READ_EXTERNAL_STORAGE)
                requestPermissions(activity, WRITE_EXTERNAL_STORAGE)
                Option.Some(activity)
            }
            .map { hasPermissions(it) }
            .flatMap { getCameraInstance() }
            .map { camera: Camera ->
                Log.d("xrx", "well...")
            }
            .orElse {
                Log.d("xrx", "cacat")
                Option.None
            }
}

private fun hasCameraHardware(context: Context): Option<Boolean> {
    if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA))
        return Option.Some(true)
    else
        return Option.None
}

private fun hasPermissions(activity: Activity): Option<Activity> {
    if (checkPermissionType(activity, CAMERA)
        && checkPermissionType(activity, READ_EXTERNAL_STORAGE)
        && checkPermissionType(activity, WRITE_EXTERNAL_STORAGE))
    {
        return Option.Some(activity)
    } else {
        return Option.None
    }
}


private fun requestPermissions(activity: Activity, permission: String) {
    ActivityCompat.requestPermissions(activity, arrayOf<String>(permission), 201)
}

private fun checkPermissionType(activity: Activity, permissionType: String)
    = ContextCompat.checkSelfPermission(activity, permissionType) == PackageManager.PERMISSION_GRANTED



private fun getCameraInstance(): Option<Camera> {
    var c: Option<Camera>
    try
    {
        c = Option.Some(Camera.open()) // attempt to get a Camera instance
    }
    catch (e:Exception) {
        c = Option.None
        // Camera is not available (in use or does not exist)
    }
    return c // returns null if camera is unavailable
}
