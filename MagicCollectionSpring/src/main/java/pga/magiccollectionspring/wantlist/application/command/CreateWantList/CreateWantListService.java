package pga.magiccollectionspring.wantlist.application.command.CreateWantList;

import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ConflictException;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.domain.IUserRepository;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.wantlist.api.WantListMapper;
import pga.magiccollectionspring.wantlist.domain.IWantListRepository;
import pga.magiccollectionspring.wantlist.domain.WantList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateWantListService implements ICommandService<CreateWantListCommand, CreateWantListResponse> {

    private final IWantListRepository wantListRepository;
    private final IUserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final WantListMapper mapper;

    public CreateWantListService(IWantListRepository wantListRepository, 
                                 IUserRepository userRepository,
                                 CurrentUserProvider currentUserProvider,
                                 WantListMapper mapper) {
        this.wantListRepository = wantListRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CreateWantListResponse execute(CreateWantListCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        if (wantListRepository.existsByNameAndOwnerId(command.name(), owner.getId())) {
            throw new ConflictException("Ya existe una lista con ese nombre");
        }

        WantList wantList = new WantList(command.name(), owner);
        WantList saved = wantListRepository.save(wantList);
        
        return new CreateWantListResponse(mapper.toDto(saved));
    }
}
