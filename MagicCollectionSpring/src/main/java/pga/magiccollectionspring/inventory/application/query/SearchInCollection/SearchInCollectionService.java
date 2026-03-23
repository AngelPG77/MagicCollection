package pga.magiccollectionspring.inventory.application.query.SearchInCollection;

import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.inventory.api.CardYouOwnMapper;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SearchInCollectionService implements IQueryService<SearchInCollectionQuery, SearchInCollectionResponse> {

    private final ICardYouOwnRepository inventoryRepo;
    private final ICollectionRepository collectionRepository;
    private final CardYouOwnMapper mapper;

    public SearchInCollectionService(ICardYouOwnRepository inventoryRepo, ICollectionRepository collectionRepository, CardYouOwnMapper mapper) {
        this.inventoryRepo = inventoryRepo;
        this.collectionRepository = collectionRepository;
        this.mapper = mapper;
    }

    @Override
    public SearchInCollectionResponse execute(SearchInCollectionQuery query) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Collection collection = collectionRepository.findById(query.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", query.collectionId()));

        if (!collection.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para buscar en esta coleccion");
        }

        return new SearchInCollectionResponse(mapper.mapList(inventoryRepo.searchInSpecificCollection(query.collectionId(), query.term())));
    }
}