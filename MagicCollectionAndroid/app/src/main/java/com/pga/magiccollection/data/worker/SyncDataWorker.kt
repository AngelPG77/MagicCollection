package com.pga.magiccollection.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pga.magiccollection.domain.usecase.auth.GetSessionStateUseCase
import com.pga.magiccollection.domain.usecase.collection.SyncCollectionsUseCase
import com.pga.magiccollection.domain.usecase.wantlist.SyncWantListsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/**
 * Background worker that flushes locally-pending changes (collections + wantlists)
 * to the backend and pulls server state. Runs with NetworkType.CONNECTED so WorkManager
 * automatically defers it until network is available, and retries with exponential
 * backoff on transient failures.
 *
 * Trigger points:
 *  - On reconnect (NetworkConnectivityObserver in MainViewModel).
 *  - As a backup retry path after foreground sync fails.
 *  - Periodically (optional, hooked from MagicCollectionApp at startup).
 */
@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncCollectionsUseCase: SyncCollectionsUseCase,
    private val syncWantListsUseCase: SyncWantListsUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val session = getSessionStateUseCase()
        if (!session.isLoggedIn || session.userId <= 0L) {
            // Nothing to sync — not an error, just a no-op.
            return Result.success()
        }

        return try {
            syncCollectionsUseCase(session.userId)
            syncWantListsUseCase(session.userId)
            Result.success()
        } catch (e: HttpException) {
            Timber.w(e, "SyncDataWorker HTTP ${e.code()} — ${if (e.code() in 500..599) "retry" else "fail"}")
            if (e.code() in 500..599 && runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure(workDataOf(KEY_ERROR to "HTTP ${e.code()}"))
            }
        } catch (e: IOException) {
            Timber.w(e, "SyncDataWorker network error — retry")
            if (runAttemptCount < MAX_RETRIES) Result.retry()
            else Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Network error")))
        } catch (e: Exception) {
            Timber.e(e, "SyncDataWorker unexpected error")
            Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Unknown error")))
        }
    }

    companion object {
        const val WORK_NAME = "sync_data_work"
        const val KEY_ERROR = "error"
        private const val MAX_RETRIES = 5
    }
}
