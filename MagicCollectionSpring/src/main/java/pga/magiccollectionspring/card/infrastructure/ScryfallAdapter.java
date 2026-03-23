package pga.magiccollectionspring.card.infrastructure;

import pga.magiccollectionspring.card.domain.port.ScryfallPort;
import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;
import pga.magiccollectionspring.card.infrastructure.dto.ScryfallSearchResponse;
import pga.magiccollectionspring.shared.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
    public CompletableFuture<Optional<CardScryfallDTO>> findCardByName(String cardName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CardScryfallDTO dto = scryfallClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/named")
                                .queryParam("exact", cardName)
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
    public CompletableFuture<ScryfallSearchResponse> searchCards(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ScryfallSearchResponse response = scryfallClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards/search")
                                .queryParam("q", query)
                                .build())
                        .retrieve()
                        .body(ScryfallSearchResponse.class);
                return response != null ? response : new ScryfallSearchResponse();
            } catch (Exception e) {
                log.error("Error buscando en Scryfall con query '{}': {}", query, e.getMessage());
                throw new ExternalServiceException("Error al buscar en Scryfall", e);
            }
        });
    }
}