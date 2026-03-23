package pga.magiccollectionspring.inventory.application.command.DeleteCard;

import pga.magiccollectionspring.inventory.domain.CardYouOwn;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DeleteCardService implements ICommandService<DeleteCardCommand, DeleteCardResponse> {

    private final ICardYouOwnRepository inventoryRepo;

    public DeleteCardService(ICardYouOwnRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    @Override
    public DeleteCardResponse execute(DeleteCardCommand command) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CardYouOwn record = inventoryRepo.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException("Registro", command.id()));

        if (!record.getCollection().getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para eliminar este registro");
        }

        inventoryRepo.deleteById(command.id());
        return new DeleteCardResponse("Carta eliminada correctamente");
    }
}