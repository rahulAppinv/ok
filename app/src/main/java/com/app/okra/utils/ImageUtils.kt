package com.app.okra.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min


class ImageUtils {
    private var fileName: String?=null
    private var photoURI: Uri? = null
    private var mCurrentPhotoPath: String? = null
    private var mFragment: Fragment? = null

    private lateinit var mActivity: Activity
    private var mCallbacks: IChooseImageInterface? = null

    fun setCallbacks(mActivity: Activity, listener: IChooseImageInterface,  mFragment: Fragment? = null) {
        this.mActivity = mActivity
        this.mFragment = mFragment
        mCallbacks = listener
    }


    fun openCamera(triggerActivityResult : Boolean =true) :Intent {
       return Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera mActivity to handle the intent
            takePictureIntent.resolveActivity(mActivity.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(mActivity)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    ex.printStackTrace()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        mActivity,
                        mActivity.packageName + ".provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureIntent.putExtra("return-data", true)

                    if(triggerActivityResult) {
                        if (mFragment != null) {
                            mFragment!!.startActivityForResult(
                                takePictureIntent,
                                AppConstants.RequestOrResultCodes.REQUEST_CLICK_IMAGE_FROM_CAMERA
                            )
                        } else {
                            mActivity.startActivityForResult(
                                takePictureIntent,
                                AppConstants.RequestOrResultCodes.REQUEST_CLICK_IMAGE_FROM_CAMERA
                            )
                        }
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    fun createImageFile(mActivity: Activity): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(
            Date()
        )
        val storageDir: File = mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_proof_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
            fileName = name
        }
    }

    fun getCameraImageResult(data: Intent?){
        if (photoURI != null) {
            val image: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri.parse(mCurrentPhotoPath)
            } else {
                photoURI!!
            }
            mCallbacks?.setImagePath(image)
        }
    }

    fun setImageResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.RequestOrResultCodes.REQUEST_CLICK_IMAGE_FROM_CAMERA -> {
                    if (photoURI != null) {
                        val image: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri.parse(mCurrentPhotoPath)
                        } else {
                            photoURI!!
                        }
                        mCallbacks?.setImagePath(image)
                    }
                }

                AppConstants.RequestOrResultCodes.REQUEST_PICK_IMAGE_FROM_GALLERY -> {
                    if (data != null) {

                        photoURI = data.data
                        mCallbacks?.setImageForGallery(photoURI!!)
                    }
                }

            }

        }
    }

    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        mActivity.startActivityForResult(
            intent,
            AppConstants.RequestOrResultCodes.REQUEST_PICK_IMAGE_FROM_GALLERY
        )
    }

    companion object {

        fun loadCircularImage(
            url: String?,
            ivImage: ImageView,
            placeholder: Int
        ) {

            Glide.with(ivImage.context)
                .asBitmap()
                .load(url)
                .apply(RequestOptions.circleCropTransform())
                .into(object : BitmapImageViewTarget(ivImage) {
                    override fun setResource(resource: Bitmap?) {
                        val circularBitmapDrawable = resource?.run {
                            RoundedBitmapDrawableFactory.create(
                                ivImage.resources,
                                createBitmapWithBorder(0f)
                            ).apply {
                                isCircular = true
                            }
                        }
                        ivImage.setImageDrawable(circularBitmapDrawable)
                    }
                })


        }


        fun setCircularImage(mContext: Context?, imageUrl: String?, imageView: CircleImageView?, placeHolder: Drawable?) {
            try {
                Glide.with(mContext!!).load(imageUrl)
                    .centerCrop().placeholder(placeHolder)
                    .into(imageView!!)
            } catch (e: Exception) {
                return
            }
        }

        fun setImage(mContext: Context?, imageUrl: String?, imageView: AppCompatImageView?, placeHolder: Drawable?) {
            try {
                Glide.with(mContext!!).load(imageUrl)
                    .centerCrop().placeholder(placeHolder)
                    .into(imageView!!)
            } catch (e: Exception) {
                return
            }
        }
        fun Bitmap.createBitmapWithBorder(borderSize: Float, borderColor: Int = Color.WHITE): Bitmap {
            val borderOffset = (borderSize * 2).toInt()
            val halfWidth = width / 2
            val halfHeight = height / 2
            val circleRadius = min(halfWidth, halfHeight).toFloat()
            val newBitmap = Bitmap.createBitmap(
                width + borderOffset,
                height + borderOffset,
                Bitmap.Config.ARGB_8888
            )

            // Center coordinates of the image
            val centerX = halfWidth + borderSize
            val centerY = halfHeight + borderSize

            val paint = Paint()
            val canvas = Canvas(newBitmap).apply {
                // Set transparent initial area
                drawARGB(0, 0, 0, 0)
            }

            // Draw the transparent initial area
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            canvas.drawCircle(centerX, centerY, circleRadius, paint)

            // Draw the image
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(this, borderSize, borderSize, paint)

            // Draw the createBitmapWithBorder
            paint.xfermode = null
            paint.style = Paint.Style.STROKE
            paint.color = borderColor
            paint.strokeWidth = borderSize
            canvas.drawCircle(centerX, centerY, circleRadius, paint)
            return newBitmap
        }

        fun loadImage(
            url: String?,
            ivImage: ImageView,
        ) {
            if (url != null && url.isNotEmpty()) {
                Glide.with(ivImage.context)
                    .load(url)
                   // .placeholder(R.drawable.ic_logo)
                    .into(ivImage)
            }
        }
    }

    interface IChooseImageInterface {
        fun setImagePath(path: Uri)
        fun setImageForGallery(path: Uri)
    }
}