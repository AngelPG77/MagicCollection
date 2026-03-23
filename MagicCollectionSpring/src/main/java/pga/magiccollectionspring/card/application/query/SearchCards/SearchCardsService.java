package pga.magiccollectionspring.card.application.query.SearchCards;

import pga.magiccollectionspring.card.domain.port.ScryfallPort;
import pga.magiccollectionspring.shared.abstractions.IQueryServiceAsync;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Service
public class SearchCardsService implements IQueryServiceAsync<SearchCardsQuery, SearchCardsResponse> {

    private final ScryfallPort scryfallPort;

    public SearchCardsService(ScryfallPort scryfallPort) {
        this.scryfallPort = scryfallPort;
    }

    @Override
    public CompletableFuture<SearchCardsResponse> execute(SearchCardsQuery query) {
        return scryfallPort.searchCards(query.query()).thenApply(response -> {
            if (response == null || response.getData() == null) {
                return new SearchCardsResponse(Collections.emptyList(), 0);
            }
            return new SearchCardsResponse(response.getData(), response.getTotalCards());
        });
    }
}