package com.codingwithrufat.workmanager

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadManager(context: Context, workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters){

    override suspend fun doWork(): Result {

        val file = File(applicationContext.cacheDir, "image.jpg")

        withContext(IO) {
            delay(3000L)
            try {
                val inputStream = URL(URL).openStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                inputStream.close()

            }catch (exception: Exception){
                return@withContext Result.failure(workDataOf(WorkerConditions.FAILED to "Something went wrong"))
            }
            Log.d(TAG, "doWork: Yes")
        }

        return Result.success(workDataOf(WorkerConditions.IMAGE_URI to file.toUri().toString()))

    }

}