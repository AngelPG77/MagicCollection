package pga.magiccollectionspring.inventory.application.query.GetCardsByCollection;

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
public class GetCardsByCollectionService implements IQueryService<GetCardsByCollectionQuery, GetCardsByCollectionResponse> {

    private final ICardYouOwnRepository inventoryRepo;
    private final ICollectionRepository collectionRepository;
    private final CardYouOwnMapper mapper;

    public GetCardsByCollectionService(ICardYouOwnRepository inventoryRepo, ICollectionRepository collectionRepository, CardYouOwnMapper mapper) {
        this.inventoryRepo = inventoryRepo;
        this.collectionRepository = collectionRepository;
        this.mapper = mapper;
    }

    @Override
    public GetCardsByCollectionResponse execute(GetCardsByCollectionQuery query) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Collection collection = collectionRepository.findById(query.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", query.collectionId()));

        if (!collection.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para ver esta coleccion");
        }

        return new GetCardsByCollectionResponse(
                mapper.mapList(inventoryRepo.findByCollection_Id(query.collectionId()))
        );
    }
}