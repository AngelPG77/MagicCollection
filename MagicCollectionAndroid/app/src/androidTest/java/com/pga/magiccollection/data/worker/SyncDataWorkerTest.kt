package com.pga.magiccollection.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.pga.magiccollection.data.repository.SessionState
import com.pga.magiccollection.domain.usecase.auth.GetSessionStateUseCase
import com.pga.magiccollection.domain.usecase.collection.SyncCollectionsUseCase
import com.pga.magiccollection.domain.usecase.wantlist.SyncWantListsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SyncDataWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun buildWorker(
        sessionState: SessionState = SessionState(isLoggedIn = true, userId = 1L, username = "user"),
        syncCollectionsAction: suspend () -> Unit = {},
        runAttemptCount: Int = 0
    ): SyncDataWorker {
        val getSessionStateUseCase = mockk<GetSessionStateUseCase> {
            every { invoke() } returns sessionState
        }
        val syncCollectionsUseCase = mockk<SyncCollectionsUseCase> {
            coEvery { invoke(any()) } coAnswers { syncCollectionsAction() }
        }
        val syncWantListsUseCase = mockk<SyncWantListsUseCase> {
            coEvery { invoke(any()) } returns 0
        }
        return TestListenableWorkerBuilder<SyncDataWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ) = SyncDataWorker(
                    appContext,
                    workerParameters,
                    syncCollectionsUseCase,
                    syncWantListsUseCase,
                    getSessionStateUseCase
                )
            })
            .setRunAttemptCount(runAttemptCount)
            .build() as SyncDataWorker
    }

    private fun httpException(code: Int): HttpException {
        val response = mockk<Response<*>>(relaxed = true) {
            every { code() } returns code
            every { message() } returns "HTTP $code"
        }
        return HttpException(response)
    }

    @Test
    fun doWork_returnsSuccess_noOp_whenUserNotLoggedIn() {
        val worker = buildWorker(SessionState(isLoggedIn = false, userId = -1L, username = ""))
        val result = runBlocking { worker.doWork() }
        assertEquals("Success", result.javaClass.simpleName)
    }

    @Test
    fun doWork_returnsRetry_whenHttpException5xx_andUnderMaxRetries() {
        val worker = buildWorker(
            syncCollectionsAction = { throw httpException(503) },
            runAttemptCount = 0
        )
        val result = runBlocking { worker.doWork() }
        assertEquals("Retry", result.javaClass.simpleName)
    }

    @Test
    fun doWork_returnsFailure_whenHttpException4xx() {
        val worker = buildWorker(
            syncCollectionsAction = { throw httpException(400) }
        )
        val result = runBlocking { worker.doWork() }
        assertEquals("Failure", result.javaClass.simpleName)
    }

    @Test
    fun doWork_returnsRetry_whenIOException_andUnderMaxRetries() {
        val worker = buildWorker(
            syncCollectionsAction = { throw IOException("network gone") },
            runAttemptCount = 2
        )
        val result = runBlocking { worker.doWork() }
        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
