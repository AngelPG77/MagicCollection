package pga.magiccollectionspring.wantlist.application.command.UpdateCardInWantList;

import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.domain.IUserRepository;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.wantlist.domain.IWantListRepository;
import pga.magiccollectionspring.wantlist.domain.WantList;
import pga.magiccollectionspring.wantlist.domain.WantListCard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCardInWantListService implements ICommandService<UpdateCardInWantListCommand, Void> {

    private final IWantListRepository wantListRepository;
    private final IUserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public UpdateCardInWantListService(IWantListRepository wantListRepository,
                                       IUserRepository userRepository,
                                       CurrentUserProvider currentUserProvider) {
        this.wantListRepository = wantListRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public Void execute(UpdateCardInWantListCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        WantList wantList = wantListRepository.findById(command.wantListId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista de deseados", command.wantListId().toString()));

        if (!wantList.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("No tienes permiso para modificar esta lista");
        }

        WantListCard card = wantList.getCards().stream()
                .filter(c -> c.getId().equals(command.cardId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Carta en lista", command.cardId().toString()));

        if (command.quantity() != null) card.setQuantity(command.quantity());
        if (command.foil() != null) card.setFoil(command.foil());
        if (command.language() != null) card.setLanguage(command.language());
        if (command.condition() != null) card.setCondition(command.condition());

        wantListRepository.save(wantList);
        return null;
    }
}
