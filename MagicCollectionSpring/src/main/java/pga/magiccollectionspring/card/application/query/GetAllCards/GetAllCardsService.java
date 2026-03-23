package pga.magiccollectionspring.card.application.query.GetAllCards;

import pga.magiccollectionspring.card.api.CardMapper;
import pga.magiccollectionspring.card.domain.ICardRepository;
import pga.magiccollectionspring.shared.abstractions.IQueryService;
import org.springframework.stereotype.Service;

@Service
public class GetAllCardsService implements IQueryService<GetAllCardsQuery, GetAllCardsResponse> {

    private final ICardRepository cardRepository;
    private final CardMapper cardMapper;

    public GetAllCardsService(ICardRepository cardRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }

    @Override
    public GetAllCardsResponse execute(GetAllCardsQuery query) {
        return new GetAllCardsResponse(cardMapper.mapList(cardRepository.findAll()));
    }
}