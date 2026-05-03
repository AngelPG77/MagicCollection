package pga.magiccollectionspring.collection.application.command.AddCardToCollection;

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
public class AddCardToCollectionService implements ICommandService<AddCardToCollectionCommand, Long> {

    private final ICollectionRepository collectionRepository;
    private final IUserInternalService userInternalService;
    private final CurrentUserProvider currentUserProvider;

    public AddCardToCollectionService(ICollectionRepository collectionRepository,
                                    IUserInternalService userInternalService,
                                    CurrentUserProvider currentUserProvider) {
        this.collectionRepository = collectionRepository;
        this.userInternalService = userInternalService;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public Long execute(AddCardToCollectionCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userInternalService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        Collection collection = collectionRepository.findById(command.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", command.collectionId().toString()));

        if (!user.getId().equals(collection.getOwner().getId())) {
            throw new UnauthorizedException("No tienes permiso para modificar esta coleccion");
        }

        // Check if card already exists with same scryfallId, foil, language and condition
        var existingOpt = collection.getCards().stream()
                .filter(c -> c.getScryfallId().equals(command.scryfallId()) 
                        && c.getFoil().equals(command.foil())
                        && c.getLanguage().equals(command.language())
                        && c.getCondition().equals(command.condition()))
                .findFirst();
        
        CollectionCard card;
        if (existingOpt.isPresent()) {
            card = existingOpt.get();
            card.setQuantity(card.getQuantity() + command.quantity());
        } else {
            card = new CollectionCard(
                    command.scryfallId(),
                    command.name(),
                    command.typeLine(),
                    command.manaCost(),
                    command.imageUrl(),
                    command.quantity(),
                    command.foil(),
                    command.language(),
                    command.condition()
            );
            collection.addCard(card);
        }

        collection = collectionRepository.save(collection);
        
        // Find the saved card to get its ID
        return collection.getCards().stream()
                .filter(c -> c.getScryfallId().equals(command.scryfallId()) 
                        && c.getFoil().equals(command.foil())
                        && c.getLanguage().equals(command.language())
                        && c.getCondition().equals(command.condition()))
                .findFirst()
                .map(CollectionCard::getId)
                .orElse(null);
    }
}
