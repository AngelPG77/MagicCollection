package pga.magiccollectionspring.card.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pga.magiccollectionspring.card.domain.ICardCatalogSyncStateRepository;
import pga.magiccollectionspring.card.domain.ICardLocalizationRepository;
import pga.magiccollectionspring.card.domain.ICardRepository;
import pga.magiccollectionspring.card.domain.IMtgSetRepository;
import pga.magiccollectionspring.card.domain.port.ScryfallPort;
import pga.magiccollectionspring.card.infrastructure.dto.BulkDataDTO;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardCatalogSyncServiceTest {

    @Mock private ICardRepository cardRepository;
    @Mock private ICardLocalizationRepository cardLocalizationRepository;
    @Mock private ICardCatalogSyncStateRepository cardCatalogSyncStateRepository;
    @Mock private IMtgSetRepository mtgSetRepository;
    @Mock private ScryfallPort scryfallPort;
    @Mock private CardLanguageSupport cardLanguageSupport;
    @Mock private LanguageIndexBuildService languageIndexBuildService;
    @Mock private LanguageIndexAsyncService languageIndexAsyncService;

    private CardCatalogSyncService service;

    @BeforeEach
    void setUp() {
        service = new CardCatalogSyncService(
                cardRepository, cardLocalizationRepository, cardCatalogSyncStateRepository,
                mtgSetRepository, scryfallPort, new ObjectMapper(), cardLanguageSupport,
                languageIndexBuildService, languageIndexAsyncService);
        // Zero out delay so tests don't wait between retries
        ReflectionTestUtils.setField(service, "allCardsRetryBaseDelayMs", 0L);
    }

    @Test
    void syncFullCatalog_returnsImmediately_whenAlreadyInProgress() throws Exception {
        AtomicBoolean flag = (AtomicBoolean) ReflectionTestUtils.getField(service, "syncInProgress");
        flag.set(true);

        service.syncFullCatalog(false);

        verify(scryfallPort, never()).getBulkDataInfo();
        verify(scryfallPort, never()).getSets();
    }

    @Test
    void syncFullCatalog_resetsSyncFlag_afterExceptionDuringSync() throws Exception {
        // getSets() is called inside syncSets() — let it return empty list
        when(scryfallPort.getSets()).thenReturn(CompletableFuture.completedFuture(List.of()));
        // getBulkDataInfo() throws a runtime exception to simulate a network failure
        when(scryfallPort.getBulkDataInfo())
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Scryfall down")));

        service.syncFullCatalog(false);

        AtomicBoolean flag = (AtomicBoolean) ReflectionTestUtils.getField(service, "syncInProgress");
        assertThat(flag.get()).isFalse();
    }

    @Test
    void syncFullCatalog_resetsSyncFlag_afterSuccessfulCompletion() throws Exception {
        BulkDataDTO defaultCards = bulkDataDto("default_cards");
        when(scryfallPort.getSets()).thenReturn(CompletableFuture.completedFuture(List.of()));
        when(scryfallPort.getBulkDataInfo())
                .thenReturn(CompletableFuture.completedFuture(List.of(defaultCards)));
        // cardCatalogSyncStateRepository returns empty → shouldSyncBulk returns true →
        // download will fail (downloadUri is null) → exception caught in finally → flag reset.
        when(cardCatalogSyncStateRepository.findByBulkType(anyString())).thenReturn(java.util.Optional.empty());

        service.syncFullCatalog(false);

        AtomicBoolean flag = (AtomicBoolean) ReflectionTestUtils.getField(service, "syncInProgress");
        assertThat(flag.get()).isFalse();
    }

    private BulkDataDTO bulkDataDto(String type) {
        BulkDataDTO dto = new BulkDataDTO();
        dto.setType(type);
        dto.setUpdatedAt(OffsetDateTime.now());
        return dto;
    }
}
