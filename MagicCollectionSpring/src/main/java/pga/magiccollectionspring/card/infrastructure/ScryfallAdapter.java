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
                CardScryfallDTO dto = scryfallClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/named")
                                .queryParam("exact", cardName)
                                .queryParam("lang", lang != null ? lang : "en")
                                .build())
                        .retrieve()
                        .onStatus(status -> status.value() == 404, (req, res) -> {
                            throw new ExternalServiceException("Carta no encontrada en Scryfall: " + cardName);
                        })
                        .body(CardScryfallDTO.class);
                return Optional.ofNullable(dto);
            } catch (ExternalServiceException e) {
                throw e;
            } catch (Exception e) {
                log.error("Error llamando a Scryfall para la carta '{}': {}", cardName, e.getMessage());
                throw new ExternalServiceException("Error al conectar con Scryfall", e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<CardScryfallDTO>> findCardByScryfallId(String scryfallId, String lang) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CardScryfallDTO dto = scryfallClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/" + scryfallId)
                                .build())
                        .retrieve()
                        .onStatus(status -> status.value() == 404, (req, res) -> {
                            throw new ExternalServiceException("Carta no encontrada en Scryfall con ID: " + scryfallId);
                        })
                        .body(CardScryfallDTO.class);
                return Optional.ofNullable(dto);
            } catch (ExternalServiceException e) {
                throw e;
            } catch (Exception e) {
                log.error("Error llamando a Scryfall para la carta con ID '{}': {}", scryfallId, e.getMessage());
                throw new ExternalServiceException("Error al conectar con Scryfall", e);
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
                log.info("Buscando en Scryfall con q='{}'", finalQuery);

                ScryfallSearchResponse response = scryfallClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/search")
                                .queryParam("q", finalQuery)
                                .build())
                        .retrieve()
                        .body(ScryfallSearchResponse.class);
                return response != null ? response : new ScryfallSearchResponse();
            } catch (Exception e) {
                log.error("Error buscando en Scryfall: {}", e.getMessage());
                throw new ExternalServiceException("Error al buscar en Scryfall", e);
            }
        });
    }

    @Override
    @Cacheable(value = "cardSuggestions", key = "#query")
    public CompletableFuture<List<CardSuggestionDTO>> autocomplete(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // name:/^.../ -> Búsqueda ultrarrápida por inicio de nombre
                // order:edhrec -> Prioriza cartas populares
                // game:paper -> Solo cartas físicas
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
                CardScryfallDTO dto = scryfallClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/random")
                                .build())
                        .retrieve()
                        .body(CardScryfallDTO.class);
                return Optional.ofNullable(dto);
            } catch (Exception e) {
                log.error("Error obteniendo carta aleatoria de Scryfall: {}", e.getMessage());
                throw new ExternalServiceException("Error al obtener carta aleatoria", e);
            }
        });
    }

    @Override
    public CompletableFuture<List<BulkDataDTO>> getBulkDataInfo() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BulkDataListResponse response = scryfallClient.get()
                        .uri("/bulk-data")
                        .retrieve()
                        .body(BulkDataListResponse.class);
                return response != null ? response.getData() : Collections.emptyList();
            } catch (Exception e) {
                log.error("Error obteniendo info de bulk data de Scryfall: {}", e.getMessage());
                return Collections.emptyList();
            }
        });
    }

    @Override
    public CompletableFuture<List<ScryfallSetDTO>> getSets() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ScryfallSetListResponse response = scryfallClient.get()
                        .uri("/sets")
                        .retrieve()
                        .body(ScryfallSetListResponse.class);
                return response != null ? response.getData() : Collections.emptyList();
            } catch (Exception e) {
                log.error("Error obteniendo lista de expansiones de Scryfall: {}", e.getMessage());
                return Collections.emptyList();
            }
        });
    }
}
