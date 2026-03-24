package pga.magiccollectionspring.inventory.application.query.SearchGlobal;

import pga.magiccollectionspring.card.domain.ICardRepository;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.inventory.api.CardYouOwnMapper;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Service to search owned cards across all user collections by card properties.
 * Searches through card catalog (ICardRepository) and filters by owned instances.
 */
@Service
public class SearchGlobalService implements IQueryService<SearchGlobalQuery, SearchGlobalResponse> {

    private final CardYouOwnMapper mapper;
    private final ICardRepository cardRepository;
    private final ICardYouOwnRepository inventoryRepo;
    private final ICollectionRepository collectionRepository;
    private final CurrentUserProvider currentUserProvider;

    public SearchGlobalService(CardYouOwnMapper mapper,
                               ICardRepository cardRepository,
                               ICardYouOwnRepository inventoryRepo,
                               ICollectionRepository collectionRepository,
                               CurrentUserProvider currentUserProvider) {
        this.mapper = mapper;
        this.cardRepository = cardRepository;
        this.inventoryRepo = inventoryRepo;
        this.collectionRepository = collectionRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public SearchGlobalResponse execute(SearchGlobalQuery query) {
        String username = currentUserProvider.getCurrentUsername();

        // First search in card catalog
        var cardsInCatalog = cardRepository.globalSearch(query.term());

        // Get all collection IDs for the user
        var userCollections = collectionRepository.findByOwner_Username(username);
        var userCollectionIds = userCollections.stream()
                .map(c -> c.getId())
                .collect(Collectors.toSet());

        // Filter owned cards to only those matching catalog search and user collections
        var cardIdsInSearch = cardsInCatalog.stream()
                .map(c -> c.getId())
                .collect(Collectors.toSet());

        var ownedCards = inventoryRepo.findAll().stream()
                .filter(co -> userCollectionIds.contains(co.getCollection().getId()) &&
                        cardIdsInSearch.contains(co.getCardMasterData().getId()))
                .collect(Collectors.toList());

        return new SearchGlobalResponse(mapper.mapList(ownedCards));
    }
}
