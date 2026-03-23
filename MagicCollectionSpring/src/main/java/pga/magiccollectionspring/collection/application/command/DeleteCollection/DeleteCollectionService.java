package pga.magiccollectionspring.collection.application.command.DeleteCollection;

import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DeleteCollectionService implements ICommandService<DeleteCollectionCommand, DeleteCollectionResponse> {

    private final ICollectionRepository collectionRepository;

    public DeleteCollectionService(ICollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    @Override
    @Transactional
    public DeleteCollectionResponse execute(DeleteCollectionCommand command) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Collection collection = collectionRepository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException("Coleccion", command.id()));

        if (!collection.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta coleccion");
        }

        collectionRepository.deleteById(command.id());
        return new DeleteCollectionResponse("Coleccion eliminada correctamente");
    }
}