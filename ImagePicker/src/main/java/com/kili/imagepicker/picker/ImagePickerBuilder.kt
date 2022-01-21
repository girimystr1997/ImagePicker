package com.kili.imagepicker.picker

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.kili.imagepicker.utils.Variables.setShowImages

class ImagePickerBuilder {

    fun showImages(status: Boolean): ImagePickerBuilder {
        setShowImages(status)
        return this
    }
    fun start(context: Context, resultLauncher: ActivityResultLauncher<Intent>) {
        resultLauncher.launch(
            Intent(context, ImagePicker::class.java)
        )
    }

    companion object {
        val instance: ImagePickerBuilder
            get() = ImagePickerBuilder()
    }
}