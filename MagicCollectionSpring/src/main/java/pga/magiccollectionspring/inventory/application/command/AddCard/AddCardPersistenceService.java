package pga.magiccollectionspring.inventory.application.command.AddCard;

import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.card.domain.ICardRepository;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.inventory.domain.CardYouOwn;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.inventory.domain.enums.CardCondition;
import pga.magiccollectionspring.inventory.domain.enums.Language;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AddCardPersistenceService {

    private final ICardYouOwnRepository inventoryRepo;
    private final ICardRepository cardRepository;

    public AddCardPersistenceService(ICardYouOwnRepository inventoryRepo, ICardRepository cardRepository) {
        this.inventoryRepo = inventoryRepo;
        this.cardRepository = cardRepository;
    }

    @Transactional
    public CardYouOwn saveCardAndInventory(Card card, Collection collection, CardCondition cond, Language langProp, boolean isFoil, int quantityToAdd) {
        // Find if card already exists by scryfallId before saving a new one
        Card masterCard = cardRepository.findById(card.getScryfallId()).orElseGet(() -> cardRepository.save(card));

        return inventoryRepo.findExactCardInCollection(collection.getId(), masterCard.getScryfallId(), cond, isFoil, langProp)
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + quantityToAdd);
                    return inventoryRepo.save(existing);
                })
                .orElseGet(() -> {
                    CardYouOwn newEntry = new CardYouOwn();
                    newEntry.setCollection(collection);
                    newEntry.setCardMasterData(masterCard);
                    newEntry.setCardCondition(cond);
                    newEntry.setLanguage(langProp);
                    newEntry.setFoil(isFoil);
                    newEntry.setQuantity(quantityToAdd);
                    return inventoryRepo.save(newEntry);
                });
    }
}
