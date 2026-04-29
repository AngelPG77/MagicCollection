package pga.magiccollectionspring.card.domain.port;

import pga.magiccollectionspring.card.api.dto.CardSuggestionDTO;
import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;
import pga.magiccollectionspring.card.infrastructure.dto.ScryfallSearchResponse;
import pga.magiccollectionspring.card.infrastructure.dto.BulkDataDTO;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface ScryfallPort {
    CompletableFuture<Optional<CardScryfallDTO>> findCardByName(String cardName, String lang);
    CompletableFuture<Optional<CardScryfallDTO>> findCardByScryfallId(String scryfallId, String lang);
    CompletableFuture<ScryfallSearchResponse> searchCards(
            String query, 
            String colors, 
            Boolean colorIdentity, 
            String colorLogic,
            String type,
            String text,
            String manaCost,
            String set,
            String rarity,
            String artist,
            String lang
    );
    CompletableFuture<List<CardSuggestionDTO>> autocomplete(String query);
    CompletableFuture<Optional<CardScryfallDTO>> getRandomCard();
    CompletableFuture<List<BulkDataDTO>> getBulkDataInfo();
    CompletableFuture<List<pga.magiccollectionspring.card.infrastructure.dto.ScryfallSetDTO>> getSets();
}