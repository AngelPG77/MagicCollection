package pga.magiccollectionspring.wantlist.application.command.AddCardToWantList;

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
public class AddCardToWantListService implements ICommandService<AddCardToWantListCommand, Long> {

    private final IWantListRepository wantListRepository;
    private final IUserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public AddCardToWantListService(IWantListRepository wantListRepository,
                                    IUserRepository userRepository,
                                    CurrentUserProvider currentUserProvider) {
        this.wantListRepository = wantListRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public Long execute(AddCardToWantListCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        WantList wantList = wantListRepository.findById(command.wantListId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista de deseados", command.wantListId().toString()));

        if (!wantList.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("No tienes permiso para modificar esta lista");
        }

        // Check if card already exists with same scryfallId, foil, language and condition
        var existingOpt = wantList.getCards().stream()
                .filter(c -> c.getScryfallId().equals(command.scryfallId()) 
                        && c.getFoil().equals(command.foil())
                        && c.getLanguage().equals(command.language())
                        && c.getCondition().equals(command.condition()))
                .findFirst();
        
        WantListCard card;
        if (existingOpt.isPresent()) {
            card = existingOpt.get();
            card.setQuantity(card.getQuantity() + command.quantity());
        } else {
            card = new WantListCard(
                    command.scryfallId(),
                    command.name(),
                    command.typeLine(),
                    command.manaCost(),
                    command.imageUrl(),
                    command.quantity(),
                    command.foil(),
                    command.language(),
                    command.condition()
            );
            wantList.addCard(card);
        }

        wantList = wantListRepository.save(wantList);
        // Find the saved card to get its ID (it might be new)
        return wantList.getCards().stream()
                .filter(c -> c.getScryfallId().equals(command.scryfallId()) 
                        && c.getFoil().equals(command.foil())
                        && c.getLanguage().equals(command.language())
                        && c.getCondition().equals(command.condition()))
                .findFirst()
                .map(WantListCard::getId)
                .orElse(null);
    }
}
