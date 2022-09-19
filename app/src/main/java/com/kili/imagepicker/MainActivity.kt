package com.kili.imagepicker

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.kili.imagepicker.databinding.ActivityMainBinding
import com.kili.imagepicker.model.FileModel
import com.kili.imagepicker.picker.ImagePickerBuilder

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ImagePickerBuilder().showImages(true)
            .start(this, resultLauncher)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.getBundleExtra("FilePath").let { it1 ->
                    it1?.getParcelable<FileModel>("FilePath").let { fileModel ->
                        Glide.with(this).load(fileModel!!.filepath)
                            .placeholder(R.mipmap.ic_launcher).into(binding.imageview)
                    }
                }
            }
        }
}