package pga.magiccollectionspring.wantlist.application.query.GetWantListById;

import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.domain.IUserRepository;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.wantlist.api.WantListMapper;
import pga.magiccollectionspring.wantlist.domain.IWantListRepository;
import pga.magiccollectionspring.wantlist.domain.WantList;
import org.springframework.stereotype.Service;

@Service
public class GetWantListByIdService implements IQueryService<GetWantListByIdQuery, GetWantListByIdResponse> {

    private final IWantListRepository wantListRepository;
    private final IUserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final WantListMapper mapper;

    public GetWantListByIdService(IWantListRepository wantListRepository,
                                  IUserRepository userRepository,
                                  CurrentUserProvider currentUserProvider,
                                  WantListMapper mapper) {
        this.wantListRepository = wantListRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
    }

    @Override
    public GetWantListByIdResponse execute(GetWantListByIdQuery query) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        WantList wantList = wantListRepository.findById(query.wantListId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista de deseados", query.wantListId().toString()));

        if (!wantList.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("No tienes permiso para ver esta lista");
        }

        return new GetWantListByIdResponse(mapper.toDto(wantList));
    }
}
