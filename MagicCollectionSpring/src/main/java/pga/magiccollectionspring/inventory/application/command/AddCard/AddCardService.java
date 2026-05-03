package pga.magiccollectionspring.inventory.application.command.AddCard;

import pga.magiccollectionspring.card.application.ICardInternalService;
import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.collection.application.ICollectionInternalService;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.inventory.domain.enums.CardCondition;
import pga.magiccollectionspring.inventory.domain.enums.Language;
import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

@Service
public class AddCardService implements ICommandService<AddCardCommand, AddCardResponse> {

    private final AddCardPersistenceService persistenceService;
    private final ICollectionInternalService collectionInternalService;
    private final ICardInternalService cardInternalService;
    private final CurrentUserProvider currentUserProvider;

    public AddCardService(AddCardPersistenceService persistenceService,
                          ICollectionInternalService collectionInternalService,
                          ICardInternalService cardInternalService,
                          CurrentUserProvider currentUserProvider) {
        this.persistenceService = persistenceService;
        this.collectionInternalService = collectionInternalService;
        this.cardInternalService = cardInternalService;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public AddCardResponse execute(AddCardCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        Collection collection = collectionInternalService.findById(command.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", command.collectionId()));

        if (!collection.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para modificar esta coleccion");
        }

        // Logic decoupled: inventory doesn't know about Scryfall or card repositories
        Card masterCard = cardInternalService.getOrFetchCard(command.cardName(), command.lang());

        CardCondition cond = resolveCondition(command.condition(), CardCondition.NEAR_MINT);
        Language langProp = resolveLanguage(command.language(), Language.ENGLISH);
        boolean isFoil = command.isFoil() != null ? command.isFoil() : false;
        int quantityToAdd = command.quantity() != null ? command.quantity() : 1;

        Long resultId = persistenceService.saveCardAndInventory(
                masterCard, collection, cond, langProp, isFoil, quantityToAdd
        );

        return new AddCardResponse(resultId);
    }

    private CardCondition resolveCondition(String condition, CardCondition fallback) {
        return condition != null ? CardCondition.fromString(condition) : fallback;
    }

    private Language resolveLanguage(String language, Language fallback) {
        return language != null ? Language.fromCode(language) : fallback;
    }
}
