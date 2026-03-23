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
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AddCardService implements ICommandService<AddCardCommand, AddCardResponse> {

    private final ICardYouOwnRepository inventoryRepo;
    private final ICollectionRepository collectionRepository;
    private final ICardRepository cardRepository;
    private final ScryfallPort scryfallPort;
    private final CardYouOwnMapper mapper;

    public AddCardService(ICardYouOwnRepository inventoryRepo,
                          ICollectionRepository collectionRepository,
                          ICardRepository cardRepository,
                          ScryfallPort scryfallPort,
                          CardYouOwnMapper mapper) {
        this.inventoryRepo = inventoryRepo;
        this.collectionRepository = collectionRepository;
        this.cardRepository = cardRepository;
        this.scryfallPort = scryfallPort;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AddCardResponse execute(AddCardCommand command) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Collection collection = collectionRepository.findById(command.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", command.collectionId()));

        if (!collection.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para modificar esta coleccion");
        }

        Card masterCard = cardRepository.findByNameIgnoreCase(command.cardName())
                .orElseGet(() -> fetchAndSaveFromScryfall(command.cardName()));

        CardCondition cond = resolveCondition(command.condition(), CardCondition.NEAR_MINT);
        Language lang = resolveLanguage(command.language(), Language.ENGLISH);
        boolean isFoil = command.isFoil() != null ? command.isFoil() : false;
        int quantityToAdd = command.quantity() != null ? command.quantity() : 1;

        CardYouOwn result = inventoryRepo.findExactCardInCollection(collection.getId(), masterCard.getId(), cond, isFoil, lang)
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + quantityToAdd);
                    return inventoryRepo.save(existing);
                })
                .orElseGet(() -> {
                    CardYouOwn newEntry = new CardYouOwn();
                    newEntry.setCollection(collection);
                    newEntry.setCardMasterData(masterCard);
                    newEntry.setCardCondition(cond);
                    newEntry.setLanguage(lang);
                    newEntry.setFoil(isFoil);
                    newEntry.setQuantity(quantityToAdd);
                    return inventoryRepo.save(newEntry);
                });

        return new AddCardResponse(mapper.map(result));
    }

    private Card fetchAndSaveFromScryfall(String cardName) {
        CardScryfallDTO dto = scryfallPort.findCardByName(cardName).join()
                .orElseThrow(() -> new ResourceNotFoundException("Carta en Scryfall", cardName));
        Card card = new Card();
        card.setScryfallId(dto.getScryfallId());
        card.setName(dto.getName());
        card.setSetCode(dto.getSetCode());
        card.setOracleText(dto.getOracleText());
        card.setTypeLine(dto.getTypeLine());
        return cardRepository.save(card);
    }

    private CardCondition resolveCondition(String condition, CardCondition fallback) {
        return condition != null ? CardCondition.fromString(condition) : fallback;
    }

    private Language resolveLanguage(String language, Language fallback) {
        return language != null ? Language.fromCode(language) : fallback;
    }
}