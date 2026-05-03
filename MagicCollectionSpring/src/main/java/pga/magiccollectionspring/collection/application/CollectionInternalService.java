package pga.magiccollectionspring.collection.application;

import org.springframework.stereotype.Service;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
class CollectionInternalService implements ICollectionInternalService {

    private final ICollectionRepository collectionRepository;

    public CollectionInternalService(ICollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    @Override
    public Optional<Collection> findById(Long id) {
        return collectionRepository.findById(id);
    }

    @Override
    public List<Long> getUserCollectionIds(String username) {
        return collectionRepository.findByOwner_Username(username).stream()
                .map(Collection::getId)
                .collect(Collectors.toList());
    }
}
