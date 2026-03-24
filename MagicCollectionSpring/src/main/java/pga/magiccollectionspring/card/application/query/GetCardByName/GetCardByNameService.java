package pga.magiccollectionspring.card.application.query.GetCardByName;

import pga.magiccollectionspring.card.api.CardMapper;
import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.card.domain.ICardRepository;
import pga.magiccollectionspring.card.domain.port.ScryfallPort;
import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;
import pga.magiccollectionspring.shared.abstractions.IQueryServiceAsync;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class GetCardByNameService implements IQueryServiceAsync<GetCardByNameQuery, GetCardByNameResponse> {

    private final ICardRepository cardRepository;
    private final ScryfallPort scryfallPort;
    private final CardMapper cardMapper;

    public GetCardByNameService(ICardRepository cardRepository, ScryfallPort scryfallPort, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.scryfallPort = scryfallPort;
        this.cardMapper = cardMapper;
    }

    @Override
    public CompletableFuture<GetCardByNameResponse> execute(GetCardByNameQuery query) {
        Optional<Card> local = cardRepository.findByNameIgnoreCase(query.name());
        if (local.isPresent()) {
            return CompletableFuture.completedFuture(new GetCardByNameResponse(cardMapper.map(local.get())));
        }
        return scryfallPort.findCardByName(query.name()).thenApply(optDto -> {
            CardScryfallDTO dto = optDto.orElseThrow(() -> new ResourceNotFoundException("Carta", query.name()));
            Card card = new Card();
            card.setScryfallId(dto.getScryfallId());
            card.setName(dto.getName());
            card.setSetCode(dto.getSetCode());
            card.setOracleText(dto.getOracleText());
            card.setTypeLine(dto.getTypeLine());
            card.setManaCost(dto.getManaCost());
            card.setConvertedManaCost(dto.getCmc() != null ? (int) Math.floor(dto.getCmc()) : null);
            Card saved = cardRepository.save(card);
            return new GetCardByNameResponse(cardMapper.map(saved));
        });
    }
}
