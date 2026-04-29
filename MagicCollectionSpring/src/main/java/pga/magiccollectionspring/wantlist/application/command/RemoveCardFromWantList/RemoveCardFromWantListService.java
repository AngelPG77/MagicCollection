package pga.magiccollectionspring.wantlist.application.command.RemoveCardFromWantList;

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
public class RemoveCardFromWantListService implements ICommandService<RemoveCardFromWantListCommand, Void> {

    private final IWantListRepository wantListRepository;
    private final IUserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public RemoveCardFromWantListService(IWantListRepository wantListRepository,
                                         IUserRepository userRepository,
                                         CurrentUserProvider currentUserProvider) {
        this.wantListRepository = wantListRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public Void execute(RemoveCardFromWantListCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        WantList wantList = wantListRepository.findById(command.wantListId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista de deseados", command.wantListId().toString()));

        if (!wantList.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("No tienes permiso para modificar esta lista");
        }

        WantListCard cardToRemove = wantList.getCards().stream()
                .filter(c -> c.getId().equals(command.cardId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Carta", command.cardId().toString()));

        wantList.removeCard(cardToRemove);
        wantListRepository.save(wantList);
        
        return null;
    }
}
