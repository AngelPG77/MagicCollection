package pga.magiccollectionspring.collection.api;

import pga.magiccollectionspring.collection.api.dto.CollectionCardDTO;
import pga.magiccollectionspring.collection.domain.CollectionCard;
import pga.magiccollectionspring.shared.abstractions.IMapper;
import org.springframework.stereotype.Component;

@Component
public class CollectionCardMapper implements IMapper<CollectionCard, CollectionCardDTO> {

    @Override
    public CollectionCardDTO map(CollectionCard card) {
        if (card == null) return null;
        return new CollectionCardDTO(
                card.getId(),
                card.getScryfallId(),
                card.getName(),
                card.getTypeLine(),
                card.getManaCost(),
                card.getImageUrl(),
                card.getQuantity(),
                card.getFoil(),
                card.getLanguage(),
                card.getCondition(),
                card.getCollection() != null ? card.getCollection().getId() : null,
                card.getCollection() != null ? card.getCollection().getName() : null
        );
    }
}
