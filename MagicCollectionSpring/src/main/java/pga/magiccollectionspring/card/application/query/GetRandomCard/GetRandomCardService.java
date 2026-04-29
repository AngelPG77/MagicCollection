package pga.magiccollectionspring.card.application.query.GetRandomCard;

import pga.magiccollectionspring.card.api.CardMapper;
import pga.magiccollectionspring.card.application.CardCatalogSyncService;
import pga.magiccollectionspring.card.domain.port.ScryfallPort;
import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;
import pga.magiccollectionspring.shared.abstractions.IQueryServiceAsync;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class GetRandomCardService implements IQueryServiceAsync<GetRandomCardQuery, GetRandomCardResponse> {

    private final CardCatalogSyncService cardCatalogSyncService;
    private final ScryfallPort scryfallPort;
    private final CardMapper cardMapper;

    public GetRandomCardService(CardCatalogSyncService cardCatalogSyncService, ScryfallPort scryfallPort, CardMapper cardMapper) {
        this.cardCatalogSyncService = cardCatalogSyncService;
        this.scryfallPort = scryfallPort;
        this.cardMapper = cardMapper;
    }

    @Override
    public CompletableFuture<GetRandomCardResponse> execute(GetRandomCardQuery query) {
        return scryfallPort.getRandomCard().thenApply(optDto -> {
            CardScryfallDTO dto = optDto.orElseThrow(() -> new ResourceNotFoundException("Carta Aleatoria", "random"));
            cardCatalogSyncService.sync(dto);
            return new GetRandomCardResponse(cardMapper.map(dto));
        });
    }
}
