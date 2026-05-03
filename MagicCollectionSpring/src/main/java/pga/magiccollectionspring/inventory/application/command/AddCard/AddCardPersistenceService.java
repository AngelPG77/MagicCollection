package pga.magiccollectionspring.inventory.application.command.AddCard;

import pga.magiccollectionspring.card.application.ICardInternalService;
import pga.magiccollectionspring.card.domain.Card;
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
    private final ICardInternalService cardInternalService;

    public AddCardPersistenceService(ICardYouOwnRepository inventoryRepo, ICardInternalService cardInternalService) {
        this.inventoryRepo = inventoryRepo;
        this.cardInternalService = cardInternalService;
    }

    @Transactional
    public Long saveCardAndInventory(Card card, Collection collection, CardCondition cond, Language langProp, boolean isFoil, int quantityToAdd) {
        // Ensure card exists in master data through internal service
        Card masterCard = cardInternalService.findById(card.getScryfallId())
                .orElse(card); // If not found, use the provided one (though internal service should have handled it)

        CardYouOwn result = inventoryRepo.findExactCardInCollection(collection.getId(), masterCard.getScryfallId(), cond, isFoil, langProp)
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
                
        return result.getId();
    }
}
