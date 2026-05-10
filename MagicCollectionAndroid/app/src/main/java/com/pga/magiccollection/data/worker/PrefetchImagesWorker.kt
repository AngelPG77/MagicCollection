package com.pga.magiccollection.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.pga.magiccollection.data.local.dao.CollectionCardDao
import com.pga.magiccollection.data.local.dao.WantListCardDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class PrefetchImagesWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val collectionCardDao: CollectionCardDao,
    private val wantListCardDao: WantListCardDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val userId = inputData.getLong(KEY_USER_ID, -1L)
        if (userId == -1L) return@withContext Result.failure()

        try {
            val ownedUrls = collectionCardDao.getAllImageUrls(userId)
            val wantListUrls = wantListCardDao.getAllWantListImageUrls(userId)

            val allUrls = (ownedUrls + wantListUrls).distinct()
            
            Timber.d("Prefetching ${allUrls.size} images for user $userId")

            var successCount = 0
            allUrls.forEach { url ->
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                
                try {
                    val result = context.imageLoader.execute(request)
                    if (result is SuccessResult) {
                        successCount++
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to prefetch image: $url")
                }
            }
            
            Timber.d("Successfully prefetched $successCount images")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error prefetching images")
            Result.failure()
        }
    }

    companion object {
        const val KEY_USER_ID = "user_id"
        const val WORK_NAME = "prefetch_images_work"
    }
}
