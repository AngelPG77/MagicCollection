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
import com.pga.magiccollection.R
import com.pga.magiccollection.data.local.dao.LanguageIndexStateDao
import com.pga.magiccollection.data.local.entities.LanguageIndexStateEntity
import com.pga.magiccollection.data.local.security.PreferenceManager
import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

@HiltWorker
class SyncCatalogWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cardSearchIndexRepository: CardSearchIndexRepository,
    private val languageIndexStateDao: LanguageIndexStateDao,
    private val preferenceManager: PreferenceManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo(0f))

        return try {
            val serverVersion = cardSearchIndexRepository.getIndexVersion()
            val downloadedLangs = preferenceManager.downloadedLanguages.first().ifEmpty { setOf("en") }

            val targetLanguages = inputData.getString(KEY_LANGUAGES)
                ?.split(",")
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotBlank() }
                ?.distinct()
                ?.takeIf { it.isNotEmpty() }
                ?: (listOf("en") + downloadedLangs.map { it.trim().lowercase() }
                    .filter { it.isNotBlank() && it != "en" }
                    .distinct())

            val includesEn = targetLanguages.any { it == "en" }
            val extraLanguages = targetLanguages.filter { it != "en" }
            val totalSteps = (if (includesEn) 1 else 0) + extraLanguages.size

            if (totalSteps == 0) {
                return Result.success(workDataOf(KEY_STEPS to 0))
            }

            var currentStep = 0

            if (includesEn) {
                cardSearchIndexRepository.bootstrapIndex("en") { progress ->
                    val globalProgress = (currentStep.toFloat() + progress) / totalSteps.toFloat()
                    setProgress(workDataOf(KEY_PROGRESS to globalProgress))
                    setForeground(createForegroundInfo(globalProgress))
                }

                languageIndexStateDao.upsert(
                    LanguageIndexStateEntity(
                        languageCode = "en",
                        installedVersion = serverVersion.version,
                        checksum = serverVersion.checksum,
                        status = "READY",
                        lastSyncAt = System.currentTimeMillis(),
                        rowCount = serverVersion.totalRows
                    )
                )

                currentStep++
            }

            extraLanguages.forEach { lang ->
                cardSearchIndexRepository.downloadLanguage(lang) { progress ->
                    val globalProgress = (currentStep.toFloat() + progress) / totalSteps.toFloat()
                    setProgress(workDataOf(KEY_PROGRESS to globalProgress))
                    setForeground(createForegroundInfo(globalProgress))
                }
                currentStep++
            }

            preferenceManager.setLastIndexUpdate(serverVersion.lastUpdated ?: System.currentTimeMillis().toString())

            Result.success(workDataOf(KEY_STEPS to totalSteps))
        } catch (e: Exception) {
            Timber.e(e, "Error syncing catalog")
            Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Unknown error")))
        }
    }

    private fun createForegroundInfo(progress: Float): ForegroundInfo {
        createNotificationChannel()
        val percent = (progress.coerceIn(0f, 1f) * 100).toInt()
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.notif_syncing_catalog))
            .setContentText(applicationContext.getString(R.string.notif_progress_simple, percent))
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
            applicationContext.getString(R.string.notif_channel_catalog_sync),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = applicationContext.getString(R.string.notif_syncing_catalog)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val KEY_PROGRESS = "progress"
        const val KEY_ERROR = "error"
        const val KEY_STEPS = "steps"
        const val KEY_LANGUAGES = "languages"
        const val WORK_NAME = "sync_catalog_work"
        private const val NOTIFICATION_CHANNEL_ID = "catalog_sync"
        private const val NOTIFICATION_ID = 1003
    }
}
