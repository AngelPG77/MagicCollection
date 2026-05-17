package com.pga.magiccollection.ui.screen

import com.pga.magiccollection.data.local.dao.CollectionWithCount
import com.pga.magiccollection.data.local.entities.CollectionCardEntity
import com.pga.magiccollection.data.repository.SessionState
import com.pga.magiccollection.domain.usecase.auth.GetSessionStateUseCase
import com.pga.magiccollection.domain.usecase.auth.LogoutUseCase
import com.pga.magiccollection.domain.usecase.collection.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeCollectionsUseCase: ObserveCollectionsUseCase = mockk()
    private val createCollectionUseCase: CreateCollectionUseCase = mockk(relaxed = true)
    private val updateCollectionUseCase: UpdateCollectionUseCase = mockk(relaxed = true)
    private val deleteCollectionUseCase: DeleteCollectionUseCase = mockk(relaxed = true)
    private val syncCollectionsUseCase: SyncCollectionsUseCase = mockk(relaxed = true)
    private val checkCollectionNameExistsUseCase: CheckCollectionNameExistsUseCase = mockk()
    private val getCollectionByIdUseCase: GetCollectionByIdUseCase = mockk(relaxed = true)
    private val observeCollectionCardsUseCase: ObserveCollectionCardsUseCase = mockk()
    private val observeAllOwnedCardsUseCase: ObserveAllOwnedCardsUseCase = mockk()
    private val removeCardFromCollectionUseCase: RemoveCardFromCollectionUseCase = mockk(relaxed = true)
    private val updateCardInCollectionUseCase: UpdateCardInCollectionUseCase = mockk(relaxed = true)
    private val logoutUseCase: LogoutUseCase = mockk(relaxed = true)
    private val observeGlobalCardCountUseCase: ObserveGlobalCardCountUseCase = mockk()
    private val getSessionStateUseCase: GetSessionStateUseCase = mockk()

    private fun buildViewModel() = CollectionViewModel(
        observeCollectionsUseCase = observeCollectionsUseCase,
        createCollectionUseCase = createCollectionUseCase,
        updateCollectionUseCase = updateCollectionUseCase,
        deleteCollectionUseCase = deleteCollectionUseCase,
        syncCollectionsUseCase = syncCollectionsUseCase,
        checkCollectionNameExistsUseCase = checkCollectionNameExistsUseCase,
        getCollectionByIdUseCase = getCollectionByIdUseCase,
        observeCollectionCardsUseCase = observeCollectionCardsUseCase,
        observeAllOwnedCardsUseCase = observeAllOwnedCardsUseCase,
        removeCardFromCollectionUseCase = removeCardFromCollectionUseCase,
        updateCardInCollectionUseCase = updateCardInCollectionUseCase,
        getSessionStateUseCase = getSessionStateUseCase,
        logoutUseCase = logoutUseCase,
        observeGlobalCardCountUseCase = observeGlobalCardCountUseCase
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeCollectionsUseCase(any()) } returns flowOf(emptyList<CollectionWithCount>())
        every { observeCollectionCardsUseCase(any()) } returns flowOf(emptyList<CollectionCardEntity>())
        every { observeAllOwnedCardsUseCase(any()) } returns flowOf(emptyList<CollectionCardEntity>())
        every { observeGlobalCardCountUseCase(any()) } returns flowOf(0)
        every { getSessionStateUseCase() } returns SessionState(isLoggedIn = true, userId = 1L, username = "testuser")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init does not start observing when not logged in`() = runTest {
        every { getSessionStateUseCase() } returns SessionState(isLoggedIn = false, userId = -1L, username = "")

        buildViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { observeCollectionsUseCase(any()) }
    }

    @Test
    fun `createCollection updates state to success when name is unique`() = runTest {
        coEvery { checkCollectionNameExistsUseCase(any(), any()) } returns false

        val viewModel = buildViewModel()
        viewModel.onAction(CollectionUiAction.NameInputChanged("Vintage Deck"))
        viewModel.onAction(CollectionUiAction.ToggleCreateDialog(true))
        viewModel.onAction(CollectionUiAction.NameInputChanged("Vintage Deck"))
        viewModel.onAction(CollectionUiAction.CreateCollection)
        advanceUntilIdle()

        assertEquals("collection_created_success", viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.showCreateDialog)
    }

    @Test
    fun `createCollection sets nameError when name already exists`() = runTest {
        coEvery { checkCollectionNameExistsUseCase(any(), any()) } returns true

        val viewModel = buildViewModel()
        viewModel.onAction(CollectionUiAction.NameInputChanged("Existing Deck"))
        viewModel.onAction(CollectionUiAction.CreateCollection)
        advanceUntilIdle()

        assertEquals("collection_name_exists_error", viewModel.uiState.value.nameError)
        coVerify(exactly = 0) { createCollectionUseCase(any(), any()) }
    }

    @Test
    fun `createCollection sets nameError when name is blank`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onAction(CollectionUiAction.NameInputChanged("   "))
        viewModel.onAction(CollectionUiAction.CreateCollection)

        assertEquals("collection_name_empty_error", viewModel.uiState.value.nameError)
        coVerify(exactly = 0) { checkCollectionNameExistsUseCase(any(), any()) }
    }
}
