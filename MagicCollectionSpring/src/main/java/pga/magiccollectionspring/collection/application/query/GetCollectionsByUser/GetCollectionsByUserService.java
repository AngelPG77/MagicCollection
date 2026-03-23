package pga.magiccollectionspring.collection.application.query.GetCollectionsByUser;

import pga.magiccollectionspring.collection.api.CollectionMapper;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class GetCollectionsByUserService implements IQueryService<GetCollectionsByUserQuery, GetCollectionsByUserResponse> {

    private final ICollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;

    public GetCollectionsByUserService(ICollectionRepository collectionRepository, CollectionMapper collectionMapper) {
        this.collectionRepository = collectionRepository;
        this.collectionMapper = collectionMapper;
    }

    @Override
    public GetCollectionsByUserResponse execute(GetCollectionsByUserQuery query) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return new GetCollectionsByUserResponse(
                collectionMapper.mapList(collectionRepository.findByOwner_Username(username))
        );
    }
}