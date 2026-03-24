package pga.magiccollectionspring.collection.application.command.CreateCollection;

import pga.magiccollectionspring.collection.api.CollectionMapper;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ConflictException;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.user.domain.IUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CreateCollectionService implements ICommandService<CreateCollectionCommand, CreateCollectionResponse> {

    private final ICollectionRepository collectionRepository;
    private final IUserRepository userRepository;
    private final CollectionMapper collectionMapper;
    private final CurrentUserProvider currentUserProvider;

    public CreateCollectionService(ICollectionRepository collectionRepository,
                                   IUserRepository userRepository,
                                   CollectionMapper collectionMapper,
                                   CurrentUserProvider currentUserProvider) {
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
        this.collectionMapper = collectionMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public CreateCollectionResponse execute(CreateCollectionCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        if (collectionRepository.existsByNameAndOwner(command.name(), owner)) {
            throw new ConflictException("Ya tienes una coleccion llamada '" + command.name() + "'");
        }

        Collection collection = new Collection();
        collection.setName(command.name());
        collection.setOwner(owner);
        Collection saved = collectionRepository.save(collection);
        return new CreateCollectionResponse(collectionMapper.map(saved));
    }
}
