package com.example.reptispot.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.microsoft.projectoxford.face.contract.Face
import kotlin.math.max

class ImageHelper {

    // Ratio to scale a detected face rectangle, the face rectangle scaled up looks more natural.
    private val FACE_RECT_SCALE_RATIO = 1.3

    private val CAPTION_PADDING = 25

    fun drawFaceRects(original: Bitmap, faces: Iterable<Face>): Bitmap {
        return drawFaceRectsWithCaptions(original, faces.map { Pair(it, null) })
    }

    fun drawFaceRectsWithCaptions(original: Bitmap, facesWithCaptions: Iterable<Pair<Face, String?>>): Bitmap {
        val bitmap = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)

        val rectPaint = Paint()
        rectPaint.isAntiAlias = true
        rectPaint.color = Color.LTGRAY
        val strokeWidth = max(original.width, original.height) / 100f
        rectPaint.strokeWidth = if (strokeWidth > 0) strokeWidth else 1f

        val textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.textSize = strokeWidth * 3
        textPaint.isFakeBoldText = true
        textPaint.color = Color.BLACK

        facesWithCaptions.forEach {
            val faceRect = ImageHelperLegacy.calculateFaceRectangle(
                bitmap, it.first.faceRectangle, FACE_RECT_SCALE_RATIO
            )

            rectPaint.style = Paint.Style.STROKE
            canvas.drawRect(
                faceRect.left.toFloat(),
                faceRect.top.toFloat(),
                (faceRect.left + faceRect.width).toFloat(),
                (faceRect.top + faceRect.height).toFloat(),
                rectPaint
            )

            rectPaint.style = Paint.Style.FILL_AND_STROKE
            canvas.drawRect(
                faceRect.left.toFloat(),
                (faceRect.top + faceRect.height).toFloat(),
                (faceRect.left + faceRect.width).toFloat(),
                faceRect.top + faceRect.height + strokeWidth * 1.5f + CAPTION_PADDING,
                rectPaint
            )

            it.second?.let { caption ->
                canvas.drawText(
                    caption,
                    (faceRect.left + CAPTION_PADDING / 2).toFloat(),
                    (faceRect.top + faceRect.height + CAPTION_PADDING).toFloat(),
                    textPaint
                )
            }
        }
        return bitmap
    }
}