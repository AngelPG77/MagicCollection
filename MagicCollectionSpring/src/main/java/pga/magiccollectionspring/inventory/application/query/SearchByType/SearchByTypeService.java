package pga.magiccollectionspring.inventory.application.query.SearchByType;

import pga.magiccollectionspring.card.application.ICardInternalService;
import pga.magiccollectionspring.collection.application.ICollectionInternalService;
import pga.magiccollectionspring.inventory.api.CardYouOwnMapper;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Service to search owned cards by card type across all user collections.
 * Decoupled from other modules via internal services.
 */
@Service
public class SearchByTypeService implements IQueryService<SearchByTypeQuery, SearchByTypeResponse> {

    private final CardYouOwnMapper mapper;
    private final ICardInternalService cardInternalService;
    private final ICardYouOwnRepository inventoryRepo;
    private final ICollectionInternalService collectionInternalService;
    private final CurrentUserProvider currentUserProvider;

    public SearchByTypeService(CardYouOwnMapper mapper,
                               ICardInternalService cardInternalService,
                               ICardYouOwnRepository inventoryRepo,
                               ICollectionInternalService collectionInternalService,
                               CurrentUserProvider currentUserProvider) {
        this.mapper = mapper;
        this.cardInternalService = cardInternalService;
        this.inventoryRepo = inventoryRepo;
        this.collectionInternalService = collectionInternalService;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public SearchByTypeResponse execute(SearchByTypeQuery query) {
        String username = currentUserProvider.getCurrentUsername();

        // 1. Get scryfall IDs matching type from Card Module
        var scryfallIdsWithType = new HashSet<>(cardInternalService.findScryfallIdsByType(query.type()));

        // 2. Get all collection IDs for the user from Collection Module
        var userCollectionIds = new HashSet<>(collectionInternalService.getUserCollectionIds(username));

        // 3. Filter owned cards
        var results = inventoryRepo.findAll().stream()
                .filter(co -> userCollectionIds.contains(co.getCollection().getId()) &&
                        scryfallIdsWithType.contains(co.getCardMasterData().getScryfallId()))
                .collect(Collectors.toList());

        return new SearchByTypeResponse(mapper.mapList(results));
    }
}
