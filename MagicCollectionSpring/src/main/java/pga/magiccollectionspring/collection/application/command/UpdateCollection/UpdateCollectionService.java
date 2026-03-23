package pga.magiccollectionspring.collection.application.command.UpdateCollection;

import pga.magiccollectionspring.collection.api.CollectionMapper;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ConflictException;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UpdateCollectionService implements ICommandService<UpdateCollectionCommand, UpdateCollectionResponse> {

    private final ICollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;

    public UpdateCollectionService(ICollectionRepository collectionRepository, CollectionMapper collectionMapper) {
        this.collectionRepository = collectionRepository;
        this.collectionMapper = collectionMapper;
    }

    @Override
    @Transactional
    public UpdateCollectionResponse execute(UpdateCollectionCommand command) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Collection collection = collectionRepository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", command.id()));

        if (!collection.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para modificar esta coleccion");
        }

        if (!collection.getName().equals(command.newName()) &&
                collectionRepository.existsByNameAndOwner(command.newName(), collection.getOwner())) {
            throw new ConflictException("Ya tienes otra coleccion llamada '" + command.newName() + "'");
        }

        collection.setName(command.newName());
        Collection saved = collectionRepository.save(collection);
        return new UpdateCollectionResponse(collectionMapper.map(saved));
    }
}