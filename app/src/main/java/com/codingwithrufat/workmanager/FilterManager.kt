package com.codingwithrufat.workmanager

import android.content.Context
import android.graphics.*
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class FilterManager(private val context: Context, private val workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        var successful = false
        var resultImageFile: File ? = null

            val file = workerParameters.inputData.getString(WorkerConditions.IMAGE_URI)
                ?.toUri()?.toFile()

            file?.let { it ->
                val bmp = BitmapFactory.decodeFile(it.absolutePath)
                val resultBmp = bmp.copy(bmp.config, true)
                val paint = Paint()
                paint.colorFilter = LightingColorFilter(0xFF66FF, 1)
                val canvas = Canvas(resultBmp)
                canvas.drawBitmap(resultBmp, 0f, 0f, paint)

                withContext(IO) {
                    delay(3000L)
                    resultImageFile = File(context.cacheDir, "new-image.jpg")
                    val outputStream = FileOutputStream(resultImageFile)
                    successful = resultBmp.compress(
                        Bitmap.CompressFormat.JPEG,
                        90,
                        outputStream
                    )

                }
            }

        return if (successful){
            Result.success(
                workDataOf(
                    WorkerConditions.FILTER_URI to resultImageFile?.toUri().toString()
                )
            )
        }else Result.failure(
            workDataOf(
                WorkerConditions.FAILED to "Something went wrong"
            )
        )

    }
}