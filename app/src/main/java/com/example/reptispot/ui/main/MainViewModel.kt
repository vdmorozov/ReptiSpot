package com.example.reptispot.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.reptispot.R
import com.example.reptispot.util.ImageHelper
import com.example.reptispot.util.ImageHelperLegacy
import com.example.reptispot.util.ReptiSpot
import com.example.reptispot.util.SpotResult
import com.microsoft.projectoxford.face.FaceServiceRestClient
import com.microsoft.projectoxford.face.contract.Face
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val _resultImage = MutableLiveData<Bitmap>()
    val resultImage: LiveData<Bitmap>
        get() = _resultImage


    fun onImageSelected(imageUri: Uri) {
        val bitmap = ImageHelperLegacy.loadSizeLimitedBitmapFromUri(
            imageUri, getApplication<Application>().contentResolver
        )

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        val inputStream = ByteArrayInputStream(output.toByteArray())

        val endpoint = getApplication<Application>().resources.getString(R.string.endpoint)
        val key = getApplication<Application>().resources.getString(R.string.subscription_key)
        val client = FaceServiceRestClient(endpoint, key)

        viewModelScope.launch(Dispatchers.IO) {
            val results = ReptiSpot(
                client,
                0.5F,
                "known-persons",
                inputStream
            ).process()
            _resultImage.postValue(
                ImageHelper().drawFaceRectsWithCaptions(
                    bitmap,
                    generateCaptions(results)
                )
            )
            Log.d("AZAZAZ", results.firstOrNull()?.matchRate.toString())
        }
    }

    //todo: move this logic to the ReptiSpot class
    private fun generateCaptions(results: Iterable<SpotResult>): Iterable<Pair<Face, String?>> {
        return results.map {
            val randomDeviation = Random.nextInt(-5, 5)
            val matchDeviated = (it.matchRate * 100).toInt() + randomDeviation
            Pair(it.face, "$matchDeviated%" )
        }
    }

}