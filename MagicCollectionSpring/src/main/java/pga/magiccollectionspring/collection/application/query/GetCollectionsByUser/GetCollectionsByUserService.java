package pga.magiccollectionspring.collection.application.query.GetCollectionsByUser;

import pga.magiccollectionspring.collection.api.CollectionMapper;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

@Service
public class GetCollectionsByUserService implements IQueryService<GetCollectionsByUserQuery, GetCollectionsByUserResponse> {

    private final ICollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;
    private final CurrentUserProvider currentUserProvider;

    public GetCollectionsByUserService(ICollectionRepository collectionRepository,
                                       CollectionMapper collectionMapper,
                                       CurrentUserProvider currentUserProvider) {
        this.collectionRepository = collectionRepository;
        this.collectionMapper = collectionMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public GetCollectionsByUserResponse execute(GetCollectionsByUserQuery query) {
        String username = currentUserProvider.getCurrentUsername();
        return new GetCollectionsByUserResponse(
                collectionMapper.mapList(collectionRepository.findByOwner_Username(username))
        );
    }
}
