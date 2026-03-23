package pga.magiccollectionspring.card.application.query.GetCardById;

import pga.magiccollectionspring.card.api.CardMapper;
import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.card.domain.ICardRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetCardByIdService implements IQueryService<GetCardByIdQuery, GetCardByIdResponse> {

    private final ICardRepository cardRepository;
    private final CardMapper cardMapper;

    public GetCardByIdService(ICardRepository cardRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }

    @Override
    public GetCardByIdResponse execute(GetCardByIdQuery query) {
        Card card = cardRepository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException("Carta", query.id()));
        return new GetCardByIdResponse(cardMapper.map(card));
    }
}