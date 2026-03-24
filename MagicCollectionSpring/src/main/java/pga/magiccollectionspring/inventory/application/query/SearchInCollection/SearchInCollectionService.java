package pga.magiccollectionspring.inventory.application.query.SearchInCollection;

import pga.magiccollectionspring.card.domain.ICardRepository;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.inventory.api.CardYouOwnMapper;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Service to search owned cards within a specific collection.
 * Searches through card catalog and filters by the specific collection.
 */
@Service
public class SearchInCollectionService implements IQueryService<SearchInCollectionQuery, SearchInCollectionResponse> {

    private final ICardRepository cardRepository;
    private final ICollectionRepository collectionRepository;
    private final ICardYouOwnRepository inventoryRepo;
    private final CardYouOwnMapper mapper;
    private final CurrentUserProvider currentUserProvider;

    public SearchInCollectionService(ICardRepository cardRepository,
                                     ICollectionRepository collectionRepository,
                                     ICardYouOwnRepository inventoryRepo,
                                     CardYouOwnMapper mapper,
                                     CurrentUserProvider currentUserProvider) {
        this.cardRepository = cardRepository;
        this.collectionRepository = collectionRepository;
        this.inventoryRepo = inventoryRepo;
        this.mapper = mapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public SearchInCollectionResponse execute(SearchInCollectionQuery query) {
        String username = currentUserProvider.getCurrentUsername();
        Collection collection = collectionRepository.findById(query.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", query.collectionId()));

        if (!collection.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para buscar en esta coleccion");
        }

        // Search in card catalog
        var cardsInCatalog = cardRepository.globalSearch(query.term());

        // Get all cards owned in this collection
        var ownedCardsInCollection = inventoryRepo.findByCollection_Id(query.collectionId());

        // Filter to matching cards
        var cardIdsInSearch = cardsInCatalog.stream()
                .map(c -> c.getId())
                .collect(Collectors.toSet());

        var results = ownedCardsInCollection.stream()
                .filter(co -> cardIdsInSearch.contains(co.getCardMasterData().getId()))
                .collect(Collectors.toList());

        return new SearchInCollectionResponse(mapper.mapList(results));
    }
}
