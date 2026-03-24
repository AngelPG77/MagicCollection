package pga.magiccollectionspring.collection.application.query.GetCollectionById;

import pga.magiccollectionspring.collection.api.CollectionMapper;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

@Service
public class GetCollectionByIdService implements IQueryService<GetCollectionByIdQuery, GetCollectionByIdResponse> {

    private final ICollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;
    private final CurrentUserProvider currentUserProvider;

    public GetCollectionByIdService(ICollectionRepository collectionRepository,
                                    CollectionMapper collectionMapper,
                                    CurrentUserProvider currentUserProvider) {
        this.collectionRepository = collectionRepository;
        this.collectionMapper = collectionMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public GetCollectionByIdResponse execute(GetCollectionByIdQuery query) {
        String username = currentUserProvider.getCurrentUsername();
        Collection collection = collectionRepository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", query.id()));
        if (!collection.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para ver esta coleccion");
        }
        return new GetCollectionByIdResponse(collectionMapper.map(collection));
    }
}
