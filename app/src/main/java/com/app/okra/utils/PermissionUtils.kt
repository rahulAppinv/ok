package com.app.okra.utils

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.app.okra.R


class PermissionUtils(val listener: IGetPermissionListener?){


    interface IGetPermissionListener{
        fun onPermissionsGiven(data: Int)
        fun onPermissionsDeny(data: Int)
    }

    fun  checkAndGetRecordPermissions( context:Context):Boolean {
        return hasPermissions(context,  Manifest.permission.RECORD_AUDIO);
    }

    fun  checkAndGetCallPermissions( context:Context):Boolean {
        return hasPermissions(context,  Manifest.permission.CALL_PHONE);
    }

    fun  checkAndGetLocationPermissions( context:Context):Boolean {
        return hasPermissions(context,  Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    fun  checkAndGetStorageAndCameraPermissions( context:Context):Boolean {
        return hasPermissions(context, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    fun  checkAndGetStoragePermissions( context:Context):Boolean {
        return hasPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        context?.let {
            if (permissions.size > 1) {
                val permissionList = mutableListOf<String>()
                var shouldShowPermissionAlert = false
                var IsPermissionGranted = true
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(it, permission!!) != PackageManager.PERMISSION_GRANTED) {
                        permissionList.add(permission)
                        IsPermissionGranted = false
                        if (ActivityCompat.shouldShowRequestPermissionRationale(it as Activity, permission)) {
                            shouldShowPermissionAlert = true
                        }
                    }
                }

                // if shouldShowPermissionAlert is true, that means user has denied any permission with DON'T SHOW checkbox checked,
                // In that case we will show PERMISSION ALERT pop up,
                // no matter either User denied (Completely Denied for one permission or two.
                if (permissionList.size > 0) {
                    if (shouldShowPermissionAlert) {
                        showPermissionInfoDialog("Permission_Alert", context, permissionList)
                    } else {
                        ActivityCompat.requestPermissions(context as Activity, permissionList.toTypedArray(), 200)

                        //  showPermissionInfoDialog("Permission_Info", context, permissionList)
                    }
                }
                return IsPermissionGranted
            }
            else {
                val permissionList = mutableListOf<String>()
                permissionList.add(permissions[0]!!)

                if (ActivityCompat.checkSelfPermission(it, permissions[0]!!) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(it as Activity, permissions[0]!!)) {
                        showPermissionInfoDialog("Permission_Alert", context, permissionList)
                    } else {
                        ActivityCompat.requestPermissions(context as Activity, permissionList.toTypedArray(), 200)

                    //   showPermissionInfoDialog("Permission_Info", context, permissionList)
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun showPermissionInfoDialog(type: String, context: Context, permissions: List<String>) {
        val dialog = Dialog(context)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)

            // Setting dialog height and width, View
            val inflater = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            val dialogView: View = inflater.inflate(R.layout.dialog_android_permission_info, null)
            setContentView(dialogView)
            window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            var textToSet: String? = ""
            textToSet = if (permissions.size > 1) {
                val permissionTypes = StringBuilder()
                for (permission in permissions) {
                    val permissionName = getFeatureNameFromPermission(permission)
                    if (permissionTypes.isNotEmpty()) {
                        permissionTypes.append(" and ")
                        permissionTypes.append(permissionName)
                    } else {
                        permissionTypes.append(permissionName)
                    }
                }
                context.getString(R.string.app_name) + " require " + permissionTypes.toString() + " permission to perform this task." +
                        "\nGo to 'Setting', select 'Permission' and enable/allow it from there. "
            } else {
                getPermissionInfoText(context, type, permissions[0])
            }
            val tvInfo: TextView = findViewById(R.id.tvInfo)
            //  val tvTitle: TextView = findViewById(R.id.tvTitle)
            val btnCancel: Button = findViewById(R.id.btnCancel)
            val btnAllow: Button = findViewById(R.id.btnAllow)


            //   tvTitle.setText(R.string.permission_alert)
            tvInfo.text = textToSet

            btnCancel.setOnClickListener {
                listener?.onPermissionsDeny(AppConstants.PermissionCodes.PERMISSION_LOCATION)
                dismiss()
            }
            btnAllow.setOnClickListener {
                if (type.equals("Permission_Info", ignoreCase = true)) {
                    ActivityCompat.requestPermissions(context as Activity, permissions.toTypedArray(), 200)
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    (context as Activity).startActivityForResult(intent, 101)
                }
                dismiss()
            }
            show()
        }
    }


    private fun getPermissionInfoText(context: Context, type: String, permission: String): String? {
        var textToSend = ""
        val appName= context.getString(R.string.app_name)
        textToSend = if (type.equals("Permission_Info", ignoreCase = true)) {
            when (permission) {
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> "STORAGE PERMISSION: $appName require this permission for handling images/files from gallery or storage, wherever required."
                Manifest.permission.CAMERA -> "CAMERA PERMISSION: $appName require this permission for handling device camera, wherever required."
                Manifest.permission.ACCESS_FINE_LOCATION -> "LOCATION PERMISSION: $appName require this permission to access your bluetooth."
                Manifest.permission.READ_PHONE_STATE -> "PHONE STATE PERMISSION: $appName require Telephony/Contact permission to enable your device to receive notifications for this app."
                else -> "PERMISSION:$appName require this permission."
            }
        } else {
            when (permission) {
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> "$appName require STORAGE permission to perform this task.\nGo to 'Setting', select 'Permission' and enable/allow it from there. "
                Manifest.permission.CAMERA -> "$appName require CAMERA permission to perform this task.\nGo to 'Setting', select 'Permission' and enable/allow it from there. "
                Manifest.permission.ACCESS_FINE_LOCATION -> "$appName require LOCATION permission to perform this task.\nGo to 'Setting', select 'Permission' and enable/allow it from there. "
                Manifest.permission.READ_PHONE_STATE -> "$appName require TELEPHONY/CONTACT permission to receive notifications.\nGo to 'Setting', select 'Permission' and enable/allow it from there. "
                else -> "PERMISSION:" + appName + "We need this permission."
            }
        }
        return textToSend
    }


    private fun getFeatureNameFromPermission(permission: String): String? {
        var nameToSend = ""
        when (permission) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> nameToSend = "Storage"
            Manifest.permission.CAMERA -> nameToSend = "Camera"
            Manifest.permission.ACCESS_FINE_LOCATION -> nameToSend = "Location"
            Manifest.permission.READ_PHONE_STATE -> nameToSend = "Telephony/Contacts"
            Manifest.permission.RECORD_AUDIO -> nameToSend = "Record Audio"
        }
        return nameToSend
    }

}
