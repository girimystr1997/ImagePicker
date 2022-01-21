package com.kili.imagepicker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File


@Parcelize
data class FileModel(
    val filename: String?,
    val filepath: String?,
    val fileAbsolutePath: String?,
    val displayName: String?,
    val size: Int,
    val file: File
) : Parcelable