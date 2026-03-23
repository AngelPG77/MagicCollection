package pga.magiccollectionspring.card.api;

import pga.magiccollectionspring.card.application.query.GetAllCards.GetAllCardsQuery;
import pga.magiccollectionspring.card.application.query.GetAllCards.GetAllCardsService;
import pga.magiccollectionspring.card.application.query.GetCardById.GetCardByIdQuery;
import pga.magiccollectionspring.card.application.query.GetCardById.GetCardByIdService;
import pga.magiccollectionspring.card.application.query.GetCardByName.GetCardByNameQuery;
import pga.magiccollectionspring.card.application.query.GetCardByName.GetCardByNameService;
import pga.magiccollectionspring.card.application.query.SearchCards.SearchCardsQuery;
import pga.magiccollectionspring.card.application.query.SearchCards.SearchCardsService;
import pga.magiccollectionspring.card.api.dto.CardDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/cards")
public class CardController {

    private final GetCardByNameService getCardByNameService;
    private final SearchCardsService searchCardsService;
    private final GetAllCardsService getAllCardsService;
    private final GetCardByIdService getCardByIdService;

    public CardController(GetCardByNameService getCardByNameService,
                          SearchCardsService searchCardsService,
                          GetAllCardsService getAllCardsService,
                          GetCardByIdService getCardByIdService) {
        this.getCardByNameService = getCardByNameService;
        this.searchCardsService = searchCardsService;
        this.getAllCardsService = getAllCardsService;
        this.getCardByIdService = getCardByIdService;
    }

    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<CardDTO>> getCardByName(@RequestParam String name) {
        return getCardByNameService.execute(new GetCardByNameQuery(name))
                .thenApply(response -> ResponseEntity.ok(response.card()));
    }

    @GetMapping("/library")
    public ResponseEntity<List<CardDTO>> getAllKnownCards() {
        return ResponseEntity.ok(getAllCardsService.execute(new GetAllCardsQuery()).cards());
    }

    @GetMapping("/discover")
    public CompletableFuture<ResponseEntity<?>> discoverCards(@RequestParam String query) {
        return searchCardsService.execute(new SearchCardsQuery(query))
                .thenApply(response -> ResponseEntity.ok(response.cards()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(getCardByIdService.execute(new GetCardByIdQuery(id)).card());
    }
}