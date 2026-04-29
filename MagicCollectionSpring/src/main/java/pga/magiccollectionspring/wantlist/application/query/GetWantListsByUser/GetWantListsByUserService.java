package pga.magiccollectionspring.wantlist.application.query.GetWantListsByUser;

import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.domain.IUserRepository;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.wantlist.api.WantListMapper;
import pga.magiccollectionspring.wantlist.domain.IWantListRepository;
import org.springframework.stereotype.Service;

@Service
public class GetWantListsByUserService implements IQueryService<GetWantListsByUserQuery, GetWantListsByUserResponse> {

    private final IWantListRepository wantListRepository;
    private final IUserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final WantListMapper mapper;

    public GetWantListsByUserService(IWantListRepository wantListRepository,
                                     IUserRepository userRepository,
                                     CurrentUserProvider currentUserProvider,
                                     WantListMapper mapper) {
        this.wantListRepository = wantListRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
    }

    @Override
    public GetWantListsByUserResponse execute(GetWantListsByUserQuery query) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));

        var wantLists = wantListRepository.findByOwnerId(user.getId())
                .stream()
                .map(mapper::toDto)
                .toList();
        return new GetWantListsByUserResponse(wantLists);
    }
}
