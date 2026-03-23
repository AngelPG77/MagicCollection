package pga.magiccollectionspring.collection.application.query.GetCollectionById;

import pga.magiccollectionspring.collection.api.CollectionMapper;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class GetCollectionByIdService implements IQueryService<GetCollectionByIdQuery, GetCollectionByIdResponse> {

    private final ICollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;

    public GetCollectionByIdService(ICollectionRepository collectionRepository, CollectionMapper collectionMapper) {
        this.collectionRepository = collectionRepository;
        this.collectionMapper = collectionMapper;
    }

    @Override
    public GetCollectionByIdResponse execute(GetCollectionByIdQuery query) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Collection collection = collectionRepository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", query.id()));
        if (!collection.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para ver esta coleccion");
        }
        return new GetCollectionByIdResponse(collectionMapper.map(collection));
    }
}