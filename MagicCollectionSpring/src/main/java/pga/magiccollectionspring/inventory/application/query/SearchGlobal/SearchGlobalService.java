package pga.magiccollectionspring.inventory.application.query.SearchGlobal;

import pga.magiccollectionspring.inventory.api.CardYouOwnMapper;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.user.domain.IUserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SearchGlobalService implements IQueryService<SearchGlobalQuery, SearchGlobalResponse> {

    private final ICardYouOwnRepository inventoryRepo;
    private final IUserRepository userRepository;
    private final CardYouOwnMapper mapper;

    public SearchGlobalService(ICardYouOwnRepository inventoryRepo, IUserRepository userRepository, CardYouOwnMapper mapper) {
        this.inventoryRepo = inventoryRepo;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public SearchGlobalResponse execute(SearchGlobalQuery query) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));
        return new SearchGlobalResponse(mapper.mapList(inventoryRepo.searchInMyGlobalInventory(user.getId(), query.term())));
    }
}