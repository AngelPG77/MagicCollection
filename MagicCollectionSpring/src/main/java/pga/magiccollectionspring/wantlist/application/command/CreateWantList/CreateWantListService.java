package pga.magiccollectionspring.wantlist.application.command.CreateWantList;

import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ConflictException;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.application.IUserInternalService;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.wantlist.domain.IWantListRepository;
import pga.magiccollectionspring.wantlist.domain.WantList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateWantListService implements ICommandService<CreateWantListCommand, CreateWantListResponse> {

    private final IWantListRepository wantListRepository;
    private final IUserInternalService userInternalService;
    private final CurrentUserProvider currentUserProvider;

    public CreateWantListService(IWantListRepository wantListRepository, 
                                 IUserInternalService userInternalService,
                                 CurrentUserProvider currentUserProvider) {
        this.wantListRepository = wantListRepository;
        this.userInternalService = userInternalService;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public CreateWantListResponse execute(CreateWantListCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        User owner = userInternalService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        if (wantListRepository.existsByNameAndOwnerId(command.name(), owner.getId())) {
            throw new ConflictException("Ya existe una lista con ese nombre");
        }

        WantList wantList = new WantList(command.name(), owner);
        WantList saved = wantListRepository.save(wantList);
        
        return new CreateWantListResponse(saved.getId());
    }
}
