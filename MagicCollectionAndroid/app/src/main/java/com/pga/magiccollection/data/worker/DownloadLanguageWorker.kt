package com.pga.magiccollection.data.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pga.magiccollection.data.local.dao.CardLanguageDao
import com.pga.magiccollection.data.local.dao.LanguageIndexStateDao
import com.pga.magiccollection.data.local.entities.CardLanguageEntity
import com.pga.magiccollection.data.local.security.PreferenceManager
import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import com.pga.magiccollection.data.repository.LanguageSnapshotChecksumException
import com.pga.magiccollection.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

@HiltWorker
class DownloadLanguageWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: CardSearchIndexRepository,
    private val cardLanguageDao: CardLanguageDao,
    private val languageIndexStateDao: LanguageIndexStateDao,
    private val preferenceManager: PreferenceManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val langCode = inputData.getString(KEY_LANG_CODE) ?: return Result.failure()
        setForeground(createForegroundInfo(langCode, 0f))

        return try {
            Timber.d("Starting download for language: $langCode")
            setProgress(workDataOf(KEY_PROGRESS to 0f))

            val count = repository.downloadLanguage(langCode) { progress ->
                setProgress(workDataOf(KEY_PROGRESS to progress))
                setForeground(createForegroundInfo(langCode, progress))
            }

            Timber.d("Downloaded $count names for $langCode")
            cardLanguageDao.insertLanguage(CardLanguageEntity(langCode))
            preferenceManager.addDownloadedLanguage(langCode)

            Result.success()
        } catch (e: LanguageSnapshotChecksumException) {
            Timber.e(e, "Checksum mismatch downloading language $langCode")
            if (runAttemptCount >= MAX_CHECKSUM_RETRIES) {
                Result.failure(workDataOf(KEY_ERROR to "Checksum mismatch"))
            } else {
                Result.retry()
            }
        } catch (e: HttpException) {
            Timber.e(e, "HTTP error downloading language $langCode")
            if (e.code() in 500..599) {
                Result.retry()
            } else {
                Result.failure(workDataOf(KEY_ERROR to "HTTP ${e.code()}"))
            }
        } catch (e: IOException) {
            Timber.e(e, "Network error downloading language $langCode")
            Result.retry()
        } catch (e: Exception) {
            Timber.e(e, "Error downloading language $langCode")
            Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Unknown error")))
        } finally {
            val currentState = languageIndexStateDao.getState(langCode)
            Timber.d("Language state after worker: $currentState")
        }
    }

    private fun createForegroundInfo(langCode: String, progress: Float): ForegroundInfo {
        createNotificationChannel()
        val percent = (progress.coerceIn(0f, 1f) * 100).toInt()
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Downloading language")
            .setContentText("$langCode: $percent%")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, percent, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
        if (existing != null) return
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Language download",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Language index download progress"
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val KEY_LANG_CODE = "lang_code"
        const val KEY_PROGRESS = "progress"
        const val KEY_ERROR = "error"
        const val WORK_NAME_PREFIX = "download_lang_"
        private const val NOTIFICATION_CHANNEL_ID = "language_downloads"
        private const val NOTIFICATION_ID = 1002
        private const val MAX_CHECKSUM_RETRIES = 1
    }
}
