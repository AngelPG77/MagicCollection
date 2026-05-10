package pga.magiccollectionspring.card.infrastructure;

import pga.magiccollectionspring.card.api.dto.CardSuggestionDTO;
import pga.magiccollectionspring.card.domain.port.ScryfallPort;
import pga.magiccollectionspring.card.infrastructure.dto.BulkDataDTO;
import pga.magiccollectionspring.card.infrastructure.dto.BulkDataListResponse;
import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;
import pga.magiccollectionspring.card.infrastructure.dto.ScryfallSearchResponse;
import pga.magiccollectionspring.card.infrastructure.dto.ScryfallSetDTO;
import pga.magiccollectionspring.card.infrastructure.dto.ScryfallSetListResponse;
import pga.magiccollectionspring.shared.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class ScryfallAdapter implements ScryfallPort {

    private static final Logger log = LoggerFactory.getLogger(ScryfallAdapter.class);

    private final RestClient scryfallClient;

    public ScryfallAdapter(RestClient scryfallClient) {
        this.scryfallClient = scryfallClient;
    }

    @Override
    public CompletableFuture<Optional<CardScryfallDTO>> findCardByName(String cardName, String lang) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[SCRYFALL] Fetching card by name: '{}' (lang={})", cardName, lang);
                CardScryfallDTO dto = scryfallClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/named")
                                .queryParam("exact", cardName)
                                .queryParam("lang", lang != null ? lang : "en")
                                .build())
                        .retrieve()
                        .onStatus(status -> status.value() == 404, (req, res) -> {
                            throw new ExternalServiceException("Card not found on Scryfall: " + cardName);
                        })
                        .body(CardScryfallDTO.class);
                return Optional.ofNullable(dto);
            } catch (ExternalServiceException e) {
                throw e;
            } catch (Exception e) {
                log.error("[SCRYFALL] Error fetching card '{}': {}", cardName, e.getMessage());
                throw new ExternalServiceException("Error connecting to Scryfall", e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<CardScryfallDTO>> findCardByScryfallId(String scryfallId, String lang) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[SCRYFALL] Fetching card by Scryfall ID: '{}'", scryfallId);
                CardScryfallDTO dto = scryfallClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/" + scryfallId)
                                .build())
                        .retrieve()
                        .onStatus(status -> status.value() == 404, (req, res) -> {
                            throw new ExternalServiceException("Card not found on Scryfall with ID: " + scryfallId);
                        })
                        .body(CardScryfallDTO.class);
                return Optional.ofNullable(dto);
            } catch (ExternalServiceException e) {
                throw e;
            } catch (Exception e) {
                log.error("[SCRYFALL] Error fetching card ID '{}': {}", scryfallId, e.getMessage());
                throw new ExternalServiceException("Error connecting to Scryfall", e);
            }
        });
    }

    @Override
    public CompletableFuture<ScryfallSearchResponse> searchCards(
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
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                StringBuilder q = new StringBuilder();
                if (query != null && !query.isBlank()) {
                    q.append(query).append(" ");
                }
                
                if (colors != null && !colors.isBlank()) {
                    String prefix = colorIdentity ? "id" : "c";
                    String operator = switch (colorLogic != null ? colorLogic : "exactly") {
                        case "at_most" -> "<=";
                        case "including" -> ">=";
                        default -> "=";
                    };
                    q.append(prefix).append(operator).append(colors).append(" ");
                }
                
                if (type != null && !type.isBlank()) {
                    q.append("t:\"").append(type).append("\" ");
                }
                
                if (text != null && !text.isBlank()) {
                    q.append("o:\"").append(text).append("\" ");
                }
                
                if (manaCost != null && !manaCost.isBlank()) {
                    q.append("m:").append(manaCost).append(" ");
                }
                
                if (set != null && !set.isBlank()) {
                    q.append("s:").append(set).append(" ");
                }
                
                if (rarity != null && !rarity.isBlank()) {
                    if (rarity.contains(",")) {
                        q.append("(");
                        String[] parts = rarity.split(",");
                        for (int i = 0; i < parts.length; i++) {
                            q.append("r:").append(parts[i]);
                            if (i < parts.length - 1) q.append(" OR ");
                        }
                        q.append(") ");
                    } else {
                        q.append("r:").append(rarity).append(" ");
                    }
                }
                
                if (artist != null && !artist.isBlank()) {
                    q.append("a:\"").append(artist).append("\" ");
                }

                if (lang != null && !lang.equals("en")) {
                    q.append("lang:").append(lang).append(" ");
                }

                String finalQuery = q.toString().trim();
                log.info("[SCRYFALL] Searching with query: '{}'", finalQuery);

                ScryfallSearchResponse response = scryfallClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/search")
                                .queryParam("q", finalQuery)
                                .build())
                        .retrieve()
                        .body(ScryfallSearchResponse.class);
                
                int resultCount = (response != null && response.getData() != null) ? response.getData().size() : 0;
                log.info("[SCRYFALL] Search finished. Found {} results.", resultCount);
                
                return response != null ? response : new ScryfallSearchResponse();
            } catch (Exception e) {
                log.error("[SCRYFALL] Search failed: {}", e.getMessage());
                throw new ExternalServiceException("Error searching on Scryfall", e);
            }
        });
    }

    @Override
    @Cacheable(value = "cardSuggestions", key = "#query")
    public CompletableFuture<List<CardSuggestionDTO>> autocomplete(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String optimizedQuery = "name:/^" + query.trim() + "/ game:paper order:edhrec";
                
                ScryfallSearchResponse response = scryfallClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/search")
                                .queryParam("q", optimizedQuery)
                                .build())
                        .retrieve()
                        .onStatus(status -> status.value() == 404, (req, res) -> {
                        })
                        .body(ScryfallSearchResponse.class);
                
                if (response != null && response.getData() != null) {
                    return response.getData().stream()
                            .limit(20)
                            .map(card -> new CardSuggestionDTO(
                                    card.getName(),
                                    card.getImageUris() != null ? card.getImageUris().getSmall() : null,
                                    card.getScryfallId()
                            ))
                            .toList();
                }
                return Collections.emptyList();
            } catch (Exception e) {
                return Collections.emptyList();
            }
        });
    }

    @Override
    public CompletableFuture<Optional<CardScryfallDTO>> getRandomCard() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[SCRYFALL] Fetching random card...");
                CardScryfallDTO dto = scryfallClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/random")
                                .build())
                        .retrieve()
                        .body(CardScryfallDTO.class);
                return Optional.ofNullable(dto);
            } catch (Exception e) {
                log.error("[SCRYFALL] Error fetching random card: {}", e.getMessage());
                throw new ExternalServiceException("Error obtaining random card", e);
            }
        });
    }

    @Override
    public CompletableFuture<List<BulkDataDTO>> getBulkDataInfo() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[SCRYFALL] Fetching bulk data information...");
                BulkDataListResponse response = scryfallClient.get()
                        .uri("/bulk-data")
                        .retrieve()
                        .body(BulkDataListResponse.class);
                return response != null ? response.getData() : Collections.emptyList();
            } catch (Exception e) {
                log.error("[SCRYFALL] Error fetching bulk data info: {}", e.getMessage());
                return Collections.emptyList();
            }
        });
    }

    @Override
    public CompletableFuture<List<ScryfallSetDTO>> getSets() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[SCRYFALL] Fetching all expansion sets...");
                ScryfallSetListResponse response = scryfallClient.get()
                        .uri("/sets")
                        .retrieve()
                        .body(ScryfallSetListResponse.class);
                return response != null ? response.getData() : Collections.emptyList();
            } catch (Exception e) {
                log.error("[SCRYFALL] Error fetching set list: {}", e.getMessage());
                return Collections.emptyList();
            }
        });
    }
}
