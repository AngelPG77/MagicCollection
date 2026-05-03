package pga.magiccollectionspring.collection.application.query.GetAllUserCards;

import pga.magiccollectionspring.collection.api.CollectionCardMapper;
import pga.magiccollectionspring.collection.domain.ICollectionCardRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.application.IUserInternalService;
import pga.magiccollectionspring.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class GetAllUserCardsService implements IQueryService<GetAllUserCardsQuery, GetAllUserCardsResponse> {

    private final ICollectionCardRepository collectionCardRepository;
    private final IUserInternalService userInternalService;
    private final CurrentUserProvider currentUserProvider;
    private final CollectionCardMapper collectionCardMapper;

    public GetAllUserCardsService(ICollectionCardRepository collectionCardRepository,
                                IUserInternalService userInternalService,
                                CurrentUserProvider currentUserProvider,
                                CollectionCardMapper collectionCardMapper) {
        this.collectionCardRepository = collectionCardRepository;
        this.userInternalService = userInternalService;
        this.currentUserProvider = currentUserProvider;
        this.collectionCardMapper = collectionCardMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public GetAllUserCardsResponse execute(GetAllUserCardsQuery query) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userInternalService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        var cards = collectionCardRepository.findByCollectionOwnerId(user.getId());

        var cardDTOs = cards.stream()
                .map(collectionCardMapper::map)
                .collect(Collectors.toList());

        return new GetAllUserCardsResponse(cardDTOs);
    }
}
