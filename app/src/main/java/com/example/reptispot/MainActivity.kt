package com.example.reptispot

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.microsoft.projectoxford.face.FaceServiceRestClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    // Flag to indicate which task is to be performed.
    private val REQUEST_SELECT_IMAGE = 0

    private lateinit var mBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                val mImageUri = data?.data
                mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                    mImageUri, contentResolver
                )

                val output = ByteArrayOutputStream()
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                val inputStream = ByteArrayInputStream(output.toByteArray())

                val client = FaceServiceRestClient(getString(R.string.endpoint), getString(R.string.subscription_key))
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
                    Log.d("AZAZAZ", results[0].matchRate.toString())
                }
            }
            else -> {
            }
        }
    }
}
