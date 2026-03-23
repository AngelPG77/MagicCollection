package pga.magiccollectionspring.inventory.api;

import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.inventory.api.dto.CardYouOwnDTO;
import pga.magiccollectionspring.inventory.api.dto.CardYouOwnRequest;
import pga.magiccollectionspring.inventory.domain.CardYouOwn;
import pga.magiccollectionspring.inventory.domain.enums.CardCondition;
import pga.magiccollectionspring.inventory.domain.enums.Language;
import pga.magiccollectionspring.shared.abstractions.IMapper;
import org.springframework.stereotype.Component;

@Component
public class CardYouOwnMapper implements IMapper<CardYouOwn, CardYouOwnDTO> {

    @Override
    public CardYouOwnDTO map(CardYouOwn entity) {
        if (entity == null) return null;
        return new CardYouOwnDTO(
                entity.getId(),
                entity.getQuantity(),
                entity.isFoil(),
                entity.getCardCondition() != null ? entity.getCardCondition().name() : null,
                entity.getLanguage() != null ? entity.getLanguage().getCode() : null,
                entity.getCardMasterData() != null ? entity.getCardMasterData().getId() : null,
                entity.getCardMasterData() != null ? entity.getCardMasterData().getName() : null,
                entity.getCollection() != null ? entity.getCollection().getId() : null
        );
    }

    public CardYouOwn toEntity(CardYouOwnRequest request, Collection collection, Card masterCard) {
        CardYouOwn entity = new CardYouOwn();
        entity.setCollection(collection);
        entity.setCardMasterData(masterCard);
        entity.setCardCondition(resolveCondition(request.getCondition(), CardCondition.NEAR_MINT));
        entity.setLanguage(resolveLanguage(request.getLanguage(), Language.ENGLISH));
        entity.setFoil(request.getIsFoil() != null ? request.getIsFoil() : false);
        entity.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        return entity;
    }

    public void updateFromRequest(CardYouOwnRequest request, CardYouOwn existing) {
        if (request.getQuantity() != null) existing.setQuantity(request.getQuantity());
        if (request.getIsFoil() != null) existing.setFoil(request.getIsFoil());
        if (request.getCondition() != null) existing.setCardCondition(CardCondition.fromString(request.getCondition()));
        if (request.getLanguage() != null) existing.setLanguage(Language.fromCode(request.getLanguage()));
    }

    private CardCondition resolveCondition(String condition, CardCondition fallback) {
        return condition != null ? CardCondition.fromString(condition) : fallback;
    }

    private Language resolveLanguage(String language, Language fallback) {
        return language != null ? Language.fromCode(language) : fallback;
    }
}