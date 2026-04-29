package pga.magiccollectionspring.card.application.query.SearchCards;

import pga.magiccollectionspring.card.api.CardMapper;
import pga.magiccollectionspring.card.domain.port.ScryfallPort;
import pga.magiccollectionspring.shared.abstractions.IQueryServiceAsync;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Service
public class SearchCardsService implements IQueryServiceAsync<SearchCardsQuery, SearchCardsResponse> {

    private final ScryfallPort scryfallPort;
    private final CardMapper cardMapper;

    public SearchCardsService(ScryfallPort scryfallPort, CardMapper cardMapper) {
        this.scryfallPort = scryfallPort;
        this.cardMapper = cardMapper;
    }

    @Override
    public CompletableFuture<SearchCardsResponse> execute(SearchCardsQuery query) {
        return scryfallPort.searchCards(
                query.query(),
                query.colors(),
                query.colorIdentity(),
                query.colorLogic(),
                query.type(),
                query.text(),
                query.manaCost(),
                query.set(),
                query.rarity(),
                query.artist(),
                query.lang()
        ).thenApply(response -> {
            if (response == null || response.getData() == null) {
                return new SearchCardsResponse(Collections.emptyList(), 0);
            }
            return new SearchCardsResponse(
                    response.getData().stream().map(cardMapper::map).toList(),
                    response.getTotalCards()
            );
        });
    }
}