package com.kili.imagepicker.picker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kili.imagepicker.R
import com.kili.imagepicker.databinding.ActivityImagePickerBinding
import com.kili.imagepicker.func.CustomProgressbar
import com.kili.imagepicker.func.Func
import com.kili.imagepicker.model.FileModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ImagePicker : AppCompatActivity() {

    var currentPhotoPath: String = ""
    var fileModel: FileModel? = null
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )
    private val permissionRequestCode = 101

    lateinit var dataBinding: ActivityImagePickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_image_picker)
        if (hasPermissions(this, permissions) as Boolean) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                permissionRequestCode
            )
        } else {
            showBottomSheetDialogForImages()
        }

        dataBinding.btnOk.setOnClickListener {
            CustomProgressbar.showProgressBar(this, false)
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    dataBinding.CropImageView.croppedImage
                    val bos = ByteArrayOutputStream()
                    dataBinding.CropImageView.croppedImage.compress(
                        Bitmap.CompressFormat.JPEG,
                        50,
                        bos
                    )
                    val bitMapData: ByteArray = bos.toByteArray()
                    if (currentPhotoPath.isNotEmpty()) {
                        val filee = File(currentPhotoPath)
                        val fos = FileOutputStream(filee)
                        fos.write(bitMapData)
                        fos.flush()
                        fos.close()
                        delay(1000)
                        fileModel = FileModel(
                            filee.name,
                            filee.path,
                            filee.absolutePath,
                            filee.name,
                            Integer.parseInt((filee.length() / 1024).toString()),
                            filee
                        )
                        val intent = Intent()
                        val bundle = Bundle()
                        bundle.putParcelable("FilePath", fileModel)
                        intent.putExtra("FilePath", bundle)
                        setResult(RESULT_OK, intent)
                        finish()
                        CustomProgressbar.hideProgressBar()
                    } else {
                        println("error")
                    }
                } catch (e: Exception) {
                    CustomProgressbar.hideProgressBar()
                    println("Image size cannot cropped more than a limit")
                }
            }
        }
        dataBinding.btnCancel.setOnClickListener {
            showBottomSheetDialogForImages()
        }
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    private fun showBottomSheetDialogForImages() {

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setOnCancelListener {
            val intent = Intent()
            intent.putExtra("FilePath", "")
            setResult(RESULT_OK, intent)
            finish()
        }
        val camera = bottomSheetDialog.findViewById<ImageView>(R.id.camera)
        val cameratxt = bottomSheetDialog.findViewById<TextView>(R.id.first)
        val video = bottomSheetDialog.findViewById<ImageView>(R.id.video)
        val videotxt = bottomSheetDialog.findViewById<TextView>(R.id.second)

        camera?.isVisible = true
        camera?.setImageDrawable(getDrawable(R.drawable.ic_camera))
        cameratxt?.isVisible = true
        cameratxt?.text = "Camera"
        video?.isVisible = true
        video?.setImageDrawable(getDrawable(R.drawable.ic_gallery))
        videotxt?.isVisible = true
        videotxt?.text = "Gallery"

        camera?.setOnClickListener {
            bottomSheetDialog.dismiss()
            openCamera()
        }
        video?.setOnClickListener {
            bottomSheetDialog.dismiss()
            openGallery()
        }



        bottomSheetDialog.show()
    }

    private fun openCamera() {
        resultCameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { tpi ->
            tpi.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (e: Exception) {
                    null
                }
                photoFile?.also { file ->
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "$packageName.provider",
                        file
                    )
                    tpi.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                }
            }
        })
    }

    private fun openGallery() {
        resultMediaImageLauncher.launch(
            Intent().setAction(Intent.ACTION_PICK).setType("image/*").also { tpi ->
                tpi.resolveActivity(packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (e: Exception) {
                        null
                    }
                    photoFile?.also { file ->
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "$packageName.provider",
                            file
                        )
                        tpi.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    }
                }
            }
        )
    }


    private var resultCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    val filee = File(currentPhotoPath)
                    fileModel =
                        FileModel(filee.name, filee.path, filee.absolutePath, filee.name, 0, filee)
                    dataBinding.CropImageView.setImageURI(
                        FileProvider.getUriForFile(
                            this,
                            "$packageName.provider", filee
                        )
                    )
                }
                else -> {
                    val intent = Intent()
                    intent.putExtra("FilePath", "")
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }

    private var resultMediaImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    if (it.data != null) {
                        val data: Intent = it.data!!
                        val uridata: Uri? = data.data
                        uridata.let { uri ->
                            if (uri != null) {
                                val path = Func().getPath(this, uri)
                                val filee = File(path!!)
                                fileModel = FileModel(
                                    filee.name,
                                    filee.path,
                                    filee.absolutePath,
                                    filee.name,
                                    0,
                                    filee
                                )
                                dataBinding.CropImageView.setImageURI(
                                    FileProvider.getUriForFile(
                                        this,
                                        "$packageName.provider", filee
                                    )
                                )
                            }
                        }
                    }
                }
                else -> {
                    val intent = Intent()
                    intent.putExtra("FilePath", "")
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }

    private fun hasPermissions(activity: Activity, permissions: Array<String>): Any {

        for (item in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    item
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionRequestCode -> {
                if (grantResults.isEmpty()
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED
                ) {
                    //Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(
                        this@ImagePicker,
                        permissions,
                        permissionRequestCode
                    )

                } else {
                    showBottomSheetDialogForImages()
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_DCIM)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
}