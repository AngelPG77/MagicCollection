package com.pga.magiccollection.domain.usecase.collection

import com.pga.magiccollection.data.repository.CollectionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFailsWith

class AddCardToCollectionUseCaseTest {

    private val collectionRepository: CollectionRepository = mockk(relaxed = true)
    private val useCase = AddCardToCollectionUseCase(collectionRepository)

    @Test
    fun `invoke delegates all params to repository`() = runTest {
        useCase(
            collectionLocalId = 1L,
            scryfallId = "scry-001",
            name = "Lightning Bolt",
            typeLine = "Instant",
            manaCost = "{R}",
            imageUrl = "https://example.com/img.jpg",
            quantity = 2,
            foil = false,
            language = "en",
            condition = "NEAR_MINT"
        )

        coVerify {
            collectionRepository.addCardToCollection(
                collectionLocalId = 1L,
                scryfallId = "scry-001",
                name = "Lightning Bolt",
                typeLine = "Instant",
                manaCost = "{R}",
                imageUrl = "https://example.com/img.jpg",
                quantity = 2,
                foil = false,
                language = "en",
                condition = "NEAR_MINT"
            )
        }
    }

    @Test
    fun `invoke propagates exception when repository throws`() = runTest {
        coEvery { collectionRepository.addCardToCollection(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws
                RuntimeException("db error")

        assertFailsWith<RuntimeException> {
            useCase(1L, "scry-001", "Lightning Bolt", null, null, null, 1, false, "en", "NEAR_MINT")
        }
    }

    @Test
    fun `invoke uses correct collection id`() = runTest {
        useCase(42L, "scry-001", "Lightning Bolt", null, null, null, 1, false, "en", "NEAR_MINT")

        coVerify { collectionRepository.addCardToCollection(collectionLocalId = 42L, any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }
}
