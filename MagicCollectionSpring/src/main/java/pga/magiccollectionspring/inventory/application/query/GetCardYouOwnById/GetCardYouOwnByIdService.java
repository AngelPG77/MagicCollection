package pga.magiccollectionspring.inventory.application.query.GetCardYouOwnById;

import org.springframework.stereotype.Service;
import pga.magiccollectionspring.inventory.api.CardYouOwnMapper;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;

@Service
public class GetCardYouOwnByIdService implements IQueryService<GetCardYouOwnByIdQuery, GetCardYouOwnByIdResponse> {

    private final ICardYouOwnRepository repository;
    private final CardYouOwnMapper mapper;

    public GetCardYouOwnByIdService(ICardYouOwnRepository repository, CardYouOwnMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public GetCardYouOwnByIdResponse execute(GetCardYouOwnByIdQuery query) {
        return repository.findById(query.id())
                .map(card -> new GetCardYouOwnByIdResponse(mapper.map(card)))
                .orElseThrow(() -> new ResourceNotFoundException("Carta en inventario", query.id()));
    }
}
