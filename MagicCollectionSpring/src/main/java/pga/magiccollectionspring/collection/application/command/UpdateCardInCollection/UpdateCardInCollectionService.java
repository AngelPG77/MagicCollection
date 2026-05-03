package pga.magiccollectionspring.collection.application.command.UpdateCardInCollection;

import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.CollectionCard;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.application.IUserInternalService;
import pga.magiccollectionspring.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCardInCollectionService implements ICommandService<UpdateCardInCollectionCommand, Void> {

    private final ICollectionRepository collectionRepository;
    private final IUserInternalService userInternalService;
    private final CurrentUserProvider currentUserProvider;

    public UpdateCardInCollectionService(ICollectionRepository collectionRepository,
                                       IUserInternalService userInternalService,
                                       CurrentUserProvider currentUserProvider) {
        this.collectionRepository = collectionRepository;
        this.userInternalService = userInternalService;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public Void execute(UpdateCardInCollectionCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userInternalService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        Collection collection = collectionRepository.findById(command.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", command.collectionId().toString()));

        if (!user.getId().equals(collection.getOwner().getId())) {
            throw new UnauthorizedException("No tienes permiso para modificar esta coleccion");
        }

        CollectionCard card = collection.getCards().stream()
                .filter(c -> c.getId().equals(command.cardId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Carta", command.cardId().toString()));

        if (command.quantity() != null) card.setQuantity(command.quantity());
        if (command.foil() != null) card.setFoil(command.foil());
        if (command.language() != null) card.setLanguage(command.language());
        if (command.condition() != null) card.setCondition(command.condition());

        collectionRepository.save(collection);
        return null;
    }
}
