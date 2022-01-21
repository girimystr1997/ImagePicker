package com.kili.imagepicker.utils

object Variables {
    private var showImages: Boolean = false

    fun showImages(): Boolean {
        return showImages
    }

    fun setShowImages(showImages: Boolean) {
        Variables.showImages = showImages
    }

}