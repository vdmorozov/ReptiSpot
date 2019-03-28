package com.example.reptispot.main

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.reptispot.R
import com.example.reptispot.util.ImageHelper
import com.example.reptispot.util.ReptiSpot
import com.microsoft.projectoxford.face.FaceServiceRestClient
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

class MainViewModel(app: Application) : AndroidViewModel(app) {


    fun onImageSelected(imageUri: Uri) {
        val bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
            imageUri, getApplication<Application>().contentResolver
        )

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        val inputStream = ByteArrayInputStream(output.toByteArray())

        val endpoint = getApplication<Application>().resources.getString(R.string.endpoint)
        val key = getApplication<Application>().resources.getString(R.string.subscription_key)
        val client = FaceServiceRestClient(endpoint, key)
//                val deferred = GlobalScope.async(Dispatchers.Default) {
//                    val results = ReptiSpot(
//                        client,
//                        0.5F,
//                        "known-persons",
//                        inputStream
//                    ).process()
//                    Log.d("AZAZAZ", results[0].matchRate.toString())
//                }

        thread(start = true) {
            val results = ReptiSpot(
                client,
                0.5F,
                "known-persons",
                inputStream
            ).process()
            Log.d("AZAZAZ", results.firstOrNull()?.matchRate.toString())
        }
    }

}