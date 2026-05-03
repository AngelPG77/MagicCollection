package pga.magiccollectionspring.inventory.application.query.GetCardsByCollection;

import pga.magiccollectionspring.collection.application.ICollectionInternalService;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.inventory.api.CardYouOwnMapper;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

@Service
public class GetCardsByCollectionService implements IQueryService<GetCardsByCollectionQuery, GetCardsByCollectionResponse> {

    private final ICardYouOwnRepository inventoryRepo;
    private final ICollectionInternalService collectionInternalService;
    private final CardYouOwnMapper cardYouOwnMapper;
    private final CurrentUserProvider currentUserProvider;

    public GetCardsByCollectionService(ICardYouOwnRepository inventoryRepo,
                                       ICollectionInternalService collectionInternalService,
                                       CardYouOwnMapper cardYouOwnMapper,
                                       CurrentUserProvider currentUserProvider) {
        this.inventoryRepo = inventoryRepo;
        this.collectionInternalService = collectionInternalService;
        this.cardYouOwnMapper = cardYouOwnMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public GetCardsByCollectionResponse execute(GetCardsByCollectionQuery query) {
        String username = currentUserProvider.getCurrentUsername();
        Collection collection = collectionInternalService.findById(query.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", query.collectionId()));

        if (!collection.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para ver esta coleccion");
        }

        return new GetCardsByCollectionResponse(
                cardYouOwnMapper.mapList(inventoryRepo.findByCollection_Id(query.collectionId()))
        );
    }
}
