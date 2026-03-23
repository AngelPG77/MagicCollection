package pga.magiccollectionspring.collection.api;

import pga.magiccollectionspring.collection.api.dto.CollectionDTO;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.shared.abstractions.IMapper;
import org.springframework.stereotype.Component;

@Component
public class CollectionMapper implements IMapper<Collection, CollectionDTO> {

    @Override
    public CollectionDTO map(Collection collection) {
        if (collection == null) return null;
        return new CollectionDTO(
                collection.getId(),
                collection.getName(),
                collection.getOwner() != null ? collection.getOwner().getUsername() : null
        );
    }
}