package pga.magiccollectionspring.collection.application;

import pga.magiccollectionspring.collection.domain.Collection;
import java.util.Optional;
import java.util.List;

public interface ICollectionInternalService {
    Optional<Collection> findById(Long id);
    List<Long> getUserCollectionIds(String username);
}
