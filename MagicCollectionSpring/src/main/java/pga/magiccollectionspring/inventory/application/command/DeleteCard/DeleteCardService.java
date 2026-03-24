package pga.magiccollectionspring.inventory.application.command.DeleteCard;

import pga.magiccollectionspring.inventory.domain.CardYouOwn;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import org.springframework.stereotype.Service;

@Service
public class DeleteCardService implements ICommandService<DeleteCardCommand, DeleteCardResponse> {

    private final ICardYouOwnRepository inventoryRepo;
    private final CurrentUserProvider currentUserProvider;

    public DeleteCardService(ICardYouOwnRepository inventoryRepo, CurrentUserProvider currentUserProvider) {
        this.inventoryRepo = inventoryRepo;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public DeleteCardResponse execute(DeleteCardCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        CardYouOwn record = inventoryRepo.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException("Registro", command.id()));

        if (!record.getCollection().getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para eliminar este registro");
        }

        inventoryRepo.deleteById(command.id());
        return new DeleteCardResponse("Carta eliminada correctamente");
    }
}
