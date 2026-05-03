package pga.magiccollectionspring.wantlist.application.command.UpdateWantList;

import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ConflictException;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.application.IUserInternalService;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.wantlist.domain.IWantListRepository;
import pga.magiccollectionspring.wantlist.domain.WantList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateWantListService implements ICommandService<UpdateWantListCommand, UpdateWantListResponse> {

    private final IWantListRepository wantListRepository;
    private final IUserInternalService userInternalService;
    private final CurrentUserProvider currentUserProvider;

    public UpdateWantListService(IWantListRepository wantListRepository,
                                 IUserInternalService userInternalService,
                                 CurrentUserProvider currentUserProvider) {
        this.wantListRepository = wantListRepository;
        this.userInternalService = userInternalService;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public UpdateWantListResponse execute(UpdateWantListCommand command) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userInternalService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        WantList wantList = wantListRepository.findById(command.wantListId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista de deseados", command.wantListId().toString()));

        if (!user.getId().equals(wantList.getOwner().getId())) {
            throw new UnauthorizedException("No tienes permiso para modificar esta lista");
        }

        if (!wantList.getName().equals(command.newName()) && 
            wantListRepository.existsByNameAndOwnerId(command.newName(), user.getId())) {
            throw new ConflictException("Ya existe una lista con ese nombre");
        }

        wantList.setName(command.newName());
        WantList saved = wantListRepository.save(wantList);
        
        return new UpdateWantListResponse(saved.getId());
    }
}
