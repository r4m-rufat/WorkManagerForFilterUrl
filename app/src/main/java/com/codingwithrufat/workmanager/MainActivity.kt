package com.codingwithrufat.workmanager

import android.content.ContentValues.TAG
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.work.*
import com.codingwithrufat.workmanager.databinding.ActivityMainBinding
import java.time.Duration
import java.util.logging.Filter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val downloadManager = OneTimeWorkRequestBuilder<DownloadManager>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(
                        NetworkType.CONNECTED
                    )
                    .build()
            )
            .build()

        val filterManager = OneTimeWorkRequestBuilder<FilterManager>()
            .setInitialDelay(Duration.ofSeconds(2L))
            .build()
        val workManager = WorkManager.getInstance(applicationContext)

        clickedButton(workManager, downloadManager, filterManager)

        observableWorkData(workManager, downloadManager, filterManager)

    }

    private fun observableWorkData(workManager: WorkManager, downloadManager: OneTimeWorkRequest, filterManager: OneTimeWorkRequest) {
        workManager.getWorkInfosForUniqueWorkLiveData("download")
            .observe(this) { info ->
                val downloadInfo = info.find { it.id == downloadManager.id }
                val filterInfo = info.find { it.id == filterManager.id }

                downloadInfo?.let {
                    it.outputData.getString(WorkerConditions.IMAGE_URI)?.toUri()?.let {
                        Log.d(TAG, "onCreate: enter to imageuri block")
                        binding.imageView.setImageDrawable(
                            Drawable.createFromStream(
                                contentResolver.openInputStream(
                                    it
                                ),
                                null
                            )
                        )
                    }
                }

                filterInfo?.let {
                    it.outputData.getString(WorkerConditions.FILTER_URI)?.toUri()?.let {
                        Log.d(TAG, "onCreate: enter to filteruri block")
                        binding.imageView.setImageDrawable(
                            Drawable.createFromStream(
                                contentResolver.openInputStream(
                                    it
                                ),
                                null
                            )
                        )
                    }
                }

            }

    }

    private fun clickedButton(workManager: WorkManager, downloadManager: OneTimeWorkRequest, filterManager: OneTimeWorkRequest) {
        binding.button.setOnClickListener {
            val string = binding.editUrl.text.trim().toString()
            workManager
                .beginUniqueWork(
                    "download",
                    ExistingWorkPolicy.KEEP,
                    downloadManager
                )
                .then(filterManager)
                .enqueue()
        }
    }

}