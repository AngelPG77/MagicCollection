package pga.magiccollectionspring.collection.api;

import pga.magiccollectionspring.collection.api.dto.CollectionCardDTO;
import pga.magiccollectionspring.collection.api.dto.CollectionDTO;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.shared.abstractions.IMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollectionMapper implements IMapper<Collection, CollectionDTO> {

    private final CollectionCardMapper collectionCardMapper;

    public CollectionMapper(CollectionCardMapper collectionCardMapper) {
        this.collectionCardMapper = collectionCardMapper;
    }

    @Override
    public CollectionDTO map(Collection collection) {
        if (collection == null) return null;
        
        List<CollectionCardDTO> cardDTOs = null;
        int cardCount = 0;
        
        if (collection.getCards() != null) {
            cardDTOs = collection.getCards().stream()
                    .map(collectionCardMapper::map)
                    .collect(Collectors.toList());
            cardCount = collection.getCards().stream()
                    .mapToInt(c -> c.getQuantity() != null ? c.getQuantity() : 0)
                    .sum();
        }
        
        return new CollectionDTO(
                collection.getId(),
                collection.getName(),
                collection.getOwner() != null ? collection.getOwner().getUsername() : null,
                cardCount,
                cardDTOs
        );
    }
}