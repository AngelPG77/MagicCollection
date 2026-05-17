package com.pga.magiccollection.ui.screen

import androidx.paging.PagingData
import com.pga.magiccollection.data.local.entities.MtgSetEntity
import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import com.pga.magiccollection.domain.model.search.IndexedCard
import com.pga.magiccollection.domain.usecase.card.BootstrapCardIndexUseCase
import com.pga.magiccollection.domain.usecase.card.HasCardIndexDataUseCase
import com.pga.magiccollection.domain.usecase.card.IngestRemoteCardsUseCase
import com.pga.magiccollection.domain.usecase.card.ObserveIndexedCardsUseCase
import com.pga.magiccollection.domain.usecase.card.SearchCardsUseCase
import com.pga.magiccollection.domain.usecase.settings.AppPreferences
import com.pga.magiccollection.domain.usecase.settings.GetAppPreferencesUseCase
import com.pga.magiccollection.domain.usecase.settings.UpdateAppPreferenceUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeIndexedCardsUseCase: ObserveIndexedCardsUseCase = mockk()
    private val bootstrapCardIndexUseCase: BootstrapCardIndexUseCase = mockk(relaxed = true)
    private val ingestRemoteCardsUseCase: IngestRemoteCardsUseCase = mockk(relaxed = true)
    private val hasCardIndexDataUseCase: HasCardIndexDataUseCase = mockk()
    private val searchCardsUseCase: SearchCardsUseCase = mockk(relaxed = true)
    private val getAppPreferencesUseCase: GetAppPreferencesUseCase = mockk()
    private val updateAppPreferenceUseCase: UpdateAppPreferenceUseCase = mockk(relaxed = true)
    private val cardSearchIndexRepository: CardSearchIndexRepository = mockk()

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeIndexedCardsUseCase(any()) } returns flowOf(emptyList<IndexedCard>())
        every { observeIndexedCardsUseCase.invokePaged(any()) } returns flowOf(PagingData.empty())
        every { hasCardIndexDataUseCase() } returns flowOf(true)
        every { getAppPreferencesUseCase() } returns flowOf(
            AppPreferences(
                darkTheme = false, gridSize = 2, startScreen = "home",
                searchLanguage = "en", appLanguage = "en", themeColor = "default",
                dynamicColor = false, downloadedLanguages = setOf("en"), lastIndexUpdate = null
            )
        )
        every { cardSearchIndexRepository.getAllSets() } returns flowOf(emptyList<MtgSetEntity>())
        coEvery { cardSearchIndexRepository.hasNamesForLanguage(any()) } returns true
        viewModel = SearchViewModel(
            searchCardsUseCase = searchCardsUseCase,
            observeIndexedCardsUseCase = observeIndexedCardsUseCase,
            bootstrapCardIndexUseCase = bootstrapCardIndexUseCase,
            ingestRemoteCardsUseCase = ingestRemoteCardsUseCase,
            hasCardIndexDataUseCase = hasCardIndexDataUseCase,
            getAppPreferencesUseCase = getAppPreferencesUseCase,
            updateAppPreferenceUseCase = updateAppPreferenceUseCase,
            cardSearchIndexRepository = cardSearchIndexRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onQueryChanged does not trigger search before debounce`() = runTest {
        viewModel.onQueryChanged("Black")
        advanceTimeBy(200L)

        verify(exactly = 0) { observeIndexedCardsUseCase(match { it.searchText == "Black" }) }
    }

    @Test
    fun `onQueryChanged triggers search after debounce`() = runTest {
        viewModel.onQueryChanged("Lotus")
        advanceTimeBy(350L)

        verify(atLeast = 1) { observeIndexedCardsUseCase(match { it.searchText == "Lotus" }) }
    }

    @Test
    fun `onColorToggled adds color to selectedColors`() = runTest {
        viewModel.onColorToggled("W")

        assertTrue(viewModel.uiState.value.selectedColors.contains("W"))
    }

    @Test
    fun `onColorToggled twice removes color from selectedColors`() = runTest {
        viewModel.onColorToggled("U")
        viewModel.onColorToggled("U")

        assertFalse(viewModel.uiState.value.selectedColors.contains("U"))
    }

    @Test
    fun `onClearQuery resets query and search state`() = runTest {
        viewModel.onQueryChanged("test")
        advanceTimeBy(350L)

        viewModel.onClearQuery()

        assertTrue(viewModel.uiState.value.query.isEmpty())
        assertFalse(viewModel.uiState.value.isSearchPerformed)
    }
}
