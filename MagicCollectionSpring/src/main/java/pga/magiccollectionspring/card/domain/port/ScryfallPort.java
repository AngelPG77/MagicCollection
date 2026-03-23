package pga.magiccollectionspring.card.domain.port;

import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;
import pga.magiccollectionspring.card.infrastructure.dto.ScryfallSearchResponse;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ScryfallPort {
    CompletableFuture<Optional<CardScryfallDTO>> findCardByName(String cardName);
    CompletableFuture<ScryfallSearchResponse> searchCards(String query);
}