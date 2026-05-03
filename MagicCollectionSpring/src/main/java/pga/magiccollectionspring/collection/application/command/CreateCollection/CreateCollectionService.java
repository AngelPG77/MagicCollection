package pga.magiccollectionspring.collection.application.command.CreateCollection;

import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ConflictException;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.application.IUserInternalService;
import pga.magiccollectionspring.user.domain.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CreateCollectionService implements ICommandService<CreateCollectionCommand, CreateCollectionResponse> {

    private final ICollectionRepository collectionRepository;
    private final IUserInternalService userInternalService;
    private final CurrentUserProvider currentUserProvider;

    public CreateCollectionService(ICollectionRepository collectionRepository,
                                   IUserInternalService userInternalService,
                                   CurrentUserProvider currentUserProvider) {
        this.collectionRepository = collectionRepository;
        this.userInternalService = userInternalService;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public CreateCollectionResponse execute(CreateCollectionCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        User owner = userInternalService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        if (collectionRepository.existsByNameAndOwner(command.name(), owner)) {
            throw new ConflictException("Ya tienes una coleccion llamada '" + command.name() + "'");
        }

        Collection collection = new Collection();
        collection.setName(command.name());
        collection.setOwner(owner);
        Collection saved = collectionRepository.save(collection);
        return new CreateCollectionResponse(saved.getId());
    }
}
