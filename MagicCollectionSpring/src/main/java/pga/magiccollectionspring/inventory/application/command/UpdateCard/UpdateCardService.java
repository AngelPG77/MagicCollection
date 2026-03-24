package pga.magiccollectionspring.inventory.application.command.UpdateCard;

import pga.magiccollectionspring.inventory.api.CardYouOwnMapper;
import pga.magiccollectionspring.inventory.api.dto.CardYouOwnRequest;
import pga.magiccollectionspring.inventory.domain.CardYouOwn;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.inventory.domain.enums.CardCondition;
import pga.magiccollectionspring.inventory.domain.enums.Language;
import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UpdateCardService implements ICommandService<UpdateCardCommand, UpdateCardResponse> {

    private final ICardYouOwnRepository inventoryRepo;
    private final CardYouOwnMapper mapper;
    private final CurrentUserProvider currentUserProvider;

    public UpdateCardService(ICardYouOwnRepository inventoryRepo,
                             CardYouOwnMapper mapper,
                             CurrentUserProvider currentUserProvider) {
        this.inventoryRepo = inventoryRepo;
        this.mapper = mapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public UpdateCardResponse execute(UpdateCardCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        CardYouOwn original = inventoryRepo.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException("Registro", command.id()));

        if (!original.getCollection().getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("No tienes permiso para modificar este registro");
        }

        CardCondition targetCond = command.condition() != null
                ? CardCondition.fromString(command.condition())
                : original.getCardCondition();
        Language targetLang = command.language() != null
                ? Language.fromCode(command.language())
                : original.getLanguage();
        boolean targetFoil = command.isFoil() != null ? command.isFoil() : original.isFoil();

        return inventoryRepo.findExactCardInCollection(
                original.getCollection().getId(), original.getCardMasterData().getId(), targetCond, targetFoil, targetLang)
                .map(conflict -> {
                    if (!conflict.getId().equals(command.id())) {
                        int numToAdd = command.quantity() != null ? command.quantity() : original.getQuantity();
                        conflict.setQuantity(conflict.getQuantity() + numToAdd);
                        inventoryRepo.deleteById(original.getId());
                        return new UpdateCardResponse(mapper.map(inventoryRepo.save(conflict)));
                    }
                    CardYouOwnRequest req = buildRequest(command);
                    mapper.updateFromRequest(req, original);
                    return new UpdateCardResponse(mapper.map(inventoryRepo.save(original)));
                })
                .orElseGet(() -> {
                    CardYouOwnRequest req = buildRequest(command);
                    mapper.updateFromRequest(req, original);
                    return new UpdateCardResponse(mapper.map(inventoryRepo.save(original)));
                });
    }

    private CardYouOwnRequest buildRequest(UpdateCardCommand command) {
        CardYouOwnRequest req = new CardYouOwnRequest();
        req.setQuantity(command.quantity());
        req.setCondition(command.condition());
        req.setIsFoil(command.isFoil());
        req.setLanguage(command.language());
        return req;
    }
}
