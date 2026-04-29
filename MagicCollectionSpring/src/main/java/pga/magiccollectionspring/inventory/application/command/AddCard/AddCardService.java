package pga.magiccollectionspring.inventory.application.command.AddCard;

import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.card.domain.ICardRepository;
import pga.magiccollectionspring.card.domain.port.ScryfallPort;
import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.inventory.api.CardYouOwnMapper;
import pga.magiccollectionspring.inventory.domain.CardYouOwn;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.inventory.domain.enums.CardCondition;
import pga.magiccollectionspring.inventory.domain.enums.Language;
import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AddCardService implements ICommandService<AddCardCommand, AddCardResponse> {

    private final AddCardPersistenceService persistenceService;
    private final ICollectionRepository collectionRepository;
    private final ICardRepository cardRepository;
    private final ScryfallPort scryfallPort;
    private final CardYouOwnMapper mapper;
    private final CurrentUserProvider currentUserProvider;

    public AddCardService(AddCardPersistenceService persistenceService,
                          ICollectionRepository collectionRepository,
                          ICardRepository cardRepository,
                          ScryfallPort scryfallPort,
                          CardYouOwnMapper mapper,
                          CurrentUserProvider currentUserProvider) {
        this.persistenceService = persistenceService;
        this.collectionRepository = collectionRepository;
        this.cardRepository = cardRepository;
        this.scryfallPort = scryfallPort;
        this.mapper = mapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public AddCardResponse execute(AddCardCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        Collection collection = collectionRepository.findById(command.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", command.collectionId()));

        if (!collection.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para modificar esta coleccion");
        }

        Card masterCard = cardRepository.findByNameIgnoreCase(command.cardName())
                .orElseGet(() -> fetchFromScryfall(command.cardName(), command.lang()));

        CardCondition cond = resolveCondition(command.condition(), CardCondition.NEAR_MINT);
        Language langProp = resolveLanguage(command.language(), Language.ENGLISH);
        boolean isFoil = command.isFoil() != null ? command.isFoil() : false;
        int quantityToAdd = command.quantity() != null ? command.quantity() : 1;

        CardYouOwn result = persistenceService.saveCardAndInventory(
                masterCard, collection, cond, langProp, isFoil, quantityToAdd
        );

        return new AddCardResponse(mapper.map(result));
    }

    private Card fetchFromScryfall(String cardName, String lang) {
        CardScryfallDTO dto = scryfallPort.findCardByName(cardName, lang != null ? lang : "en").join()
                .orElseThrow(() -> new ResourceNotFoundException("Carta en Scryfall", cardName));
        Card card = new Card();
        card.setScryfallId(dto.getScryfallId());
        card.setName(dto.getName());
        card.setSetCode(dto.getSetCode());
        card.setOracleText(dto.getOracleText());
        card.setTypeLine(dto.getTypeLine());
        card.setManaCost(dto.getManaCost());
        card.setConvertedManaCost(dto.getCmc() != null ? (int) Math.floor(dto.getCmc()) : null);
        return card; // Returned without saving yet, persistenceService will handle it
    }

    private CardCondition resolveCondition(String condition, CardCondition fallback) {
        return condition != null ? CardCondition.fromString(condition) : fallback;
    }

    private Language resolveLanguage(String language, Language fallback) {
        return language != null ? Language.fromCode(language) : fallback;
    }
}
