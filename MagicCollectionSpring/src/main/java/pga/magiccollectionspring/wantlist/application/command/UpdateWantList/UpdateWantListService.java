package pga.magiccollectionspring.wantlist.application.command.UpdateWantList;

import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ConflictException;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.domain.IUserRepository;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.wantlist.api.WantListMapper;
import pga.magiccollectionspring.wantlist.api.dto.WantListDTO;
import pga.magiccollectionspring.wantlist.domain.IWantListRepository;
import pga.magiccollectionspring.wantlist.domain.WantList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateWantListService implements ICommandService<UpdateWantListCommand, UpdateWantListResponse> {

    private final IWantListRepository wantListRepository;
    private final IUserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final WantListMapper mapper;

    public UpdateWantListService(IWantListRepository wantListRepository,
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
    public UpdateWantListResponse execute(UpdateWantListCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        WantList wantList = wantListRepository.findById(command.wantListId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista de deseados", command.wantListId().toString()));

        if (!wantList.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("No tienes permiso para modificar esta lista");
        }

        if (!wantList.getName().equals(command.newName()) && 
            wantListRepository.existsByNameAndOwnerId(command.newName(), user.getId())) {
            throw new ConflictException("Ya existe una lista con ese nombre");
        }

        wantList.setName(command.newName());
        WantList saved = wantListRepository.save(wantList);
        
        return new UpdateWantListResponse(mapper.toDto(saved));
    }
}
