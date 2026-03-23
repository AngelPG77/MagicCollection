package pga.magiccollectionspring.card.api;

import pga.magiccollectionspring.card.api.dto.CardDTO;
import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.shared.abstractions.IMapper;
import org.springframework.stereotype.Component;

@Component
public class CardMapper implements IMapper<Card, CardDTO> {

    @Override
    public CardDTO map(Card card) {
        if (card == null) return null;
        return new CardDTO(
                card.getId(),
                card.getName(),
                card.getSetCode(),
                card.getScryfallId(),
                card.getOracleText(),
                card.getTypeLine()
        );
    }
}