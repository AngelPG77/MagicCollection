package pga.magiccollectionspring.card.application;

import org.springframework.stereotype.Service;
import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.card.domain.ICardRepository;
import pga.magiccollectionspring.card.domain.port.ScryfallPort;
import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
class CardInternalService implements ICardInternalService {

    private final ICardRepository cardRepository;
    private final ScryfallPort scryfallPort;
    private final CardCatalogSyncService cardCatalogSyncService;

    public CardInternalService(ICardRepository cardRepository, 
                               ScryfallPort scryfallPort,
                               CardCatalogSyncService cardCatalogSyncService) {
        this.cardRepository = cardRepository;
        this.scryfallPort = scryfallPort;
        this.cardCatalogSyncService = cardCatalogSyncService;
    }

    @Override
    public Card getOrFetchCard(String name, String lang) {
        return cardRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    CardScryfallDTO dto = scryfallPort.findCardByName(name, lang != null ? lang : "en").join()
                            .orElseThrow(() -> new ResourceNotFoundException("Carta en Scryfall", name));
                    return cardCatalogSyncService.sync(dto);
                });
    }

    @Override
    public Optional<Card> findById(String scryfallId) {
        return cardRepository.findById(scryfallId);
    }

    @Override
    public List<String> findScryfallIdsByType(String type) {
        return cardRepository.findByTypeLineContainingIgnoreCase(type).stream()
                .map(Card::getScryfallId)
                .collect(Collectors.toList());
    }
}
