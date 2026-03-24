package pga.magiccollectionspring.inventory.application.query.SearchByType;

import pga.magiccollectionspring.card.domain.ICardRepository;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.inventory.api.CardYouOwnMapper;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Service to search owned cards by card type across all user collections.
 * Uses ICardRepository to find cards by type, then filters by owned instances.
 */
@Service
public class SearchByTypeService implements IQueryService<SearchByTypeQuery, SearchByTypeResponse> {

    private final CardYouOwnMapper mapper;
    private final ICardRepository cardRepository;
    private final ICardYouOwnRepository inventoryRepo;
    private final ICollectionRepository collectionRepository;
    private final CurrentUserProvider currentUserProvider;

    public SearchByTypeService(CardYouOwnMapper mapper,
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
    public SearchByTypeResponse execute(SearchByTypeQuery query) {
        String username = currentUserProvider.getCurrentUsername();

        // Search for cards by type in catalog
        var cardsWithType = cardRepository.findByTypeLineContainingIgnoreCase(query.type());

        // Get all collection IDs for the user
        var userCollections = collectionRepository.findByOwner_Username(username);
        var userCollectionIds = userCollections.stream()
                .map(c -> c.getId())
                .collect(Collectors.toSet());

        // Filter by cards matching type
        var cardIdsWithType = cardsWithType.stream()
                .map(c -> c.getId())
                .collect(Collectors.toSet());

        var results = inventoryRepo.findAll().stream()
                .filter(co -> userCollectionIds.contains(co.getCollection().getId()) &&
                        cardIdsWithType.contains(co.getCardMasterData().getId()))
                .collect(Collectors.toList());

        return new SearchByTypeResponse(mapper.mapList(results));
    }
}
