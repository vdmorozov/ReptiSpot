package com.example.reptispot.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.example.reptispot.R


class MainActivity : AppCompatActivity() {
    // Flag to indicate which task is to be performed.
    private val REQUEST_SELECT_IMAGE = 0

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this)
            .get(MainViewModel::class.java)

        //get photo from album
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_IMAGE -> if (resultCode == Activity.RESULT_OK) {
                // If image is selected successfully, set the image URI and bitmap.
                data?.data?.let { viewModel.onImageSelected(it) }
            }
            else -> {
            }
        }
    }
}
