package pga.magiccollectionspring.inventory.api;

import pga.magiccollectionspring.inventory.api.dto.CardYouOwnDTO;
import pga.magiccollectionspring.inventory.api.dto.CardYouOwnRequest;
import pga.magiccollectionspring.inventory.application.command.AddCard.AddCardCommand;
import pga.magiccollectionspring.inventory.application.command.AddCard.AddCardResponse;
import pga.magiccollectionspring.inventory.application.command.AddCard.AddCardService;
import pga.magiccollectionspring.inventory.application.command.DeleteCard.DeleteCardCommand;
import pga.magiccollectionspring.inventory.application.command.DeleteCard.DeleteCardService;
import pga.magiccollectionspring.inventory.application.command.UpdateCard.UpdateCardCommand;
import pga.magiccollectionspring.inventory.application.command.UpdateCard.UpdateCardService;
import pga.magiccollectionspring.inventory.application.query.GetCardYouOwnById.GetCardYouOwnByIdQuery;
import pga.magiccollectionspring.inventory.application.query.GetCardYouOwnById.GetCardYouOwnByIdService;
import pga.magiccollectionspring.inventory.application.query.GetCardsByCollection.GetCardsByCollectionQuery;
import pga.magiccollectionspring.inventory.application.query.GetCardsByCollection.GetCardsByCollectionService;
import pga.magiccollectionspring.inventory.application.query.SearchByType.SearchByTypeQuery;
import pga.magiccollectionspring.inventory.application.query.SearchByType.SearchByTypeService;
import pga.magiccollectionspring.inventory.application.query.SearchGlobal.SearchGlobalQuery;
import pga.magiccollectionspring.inventory.application.query.SearchGlobal.SearchGlobalService;
import pga.magiccollectionspring.inventory.application.query.SearchInCollection.SearchInCollectionQuery;
import pga.magiccollectionspring.inventory.application.query.SearchInCollection.SearchInCollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/your-cards")
public class CardYouOwnController {

    private final AddCardService addCardService;
    private final UpdateCardService updateCardService;
    private final DeleteCardService deleteCardService;
    private final GetCardsByCollectionService getCardsByCollectionService;
    private final SearchGlobalService searchGlobalService;
    private final SearchInCollectionService searchInCollectionService;
    private final SearchByTypeService searchByTypeService;
    private final GetCardYouOwnByIdService getCardYouOwnByIdService;

    public CardYouOwnController(AddCardService addCardService,
                                UpdateCardService updateCardService,
                                DeleteCardService deleteCardService,
                                GetCardsByCollectionService getCardsByCollectionService,
                                SearchGlobalService searchGlobalService,
                                SearchInCollectionService searchInCollectionService,
                                SearchByTypeService searchByTypeService,
                                GetCardYouOwnByIdService getCardYouOwnByIdService) {
        this.addCardService = addCardService;
        this.updateCardService = updateCardService;
        this.deleteCardService = deleteCardService;
        this.getCardsByCollectionService = getCardsByCollectionService;
        this.searchGlobalService = searchGlobalService;
        this.searchInCollectionService = searchInCollectionService;
        this.searchByTypeService = searchByTypeService;
        this.getCardYouOwnByIdService = getCardYouOwnByIdService;
    }

    @PostMapping("/add")
    public ResponseEntity<CardYouOwnDTO> addCard(@Valid @RequestBody CardYouOwnRequest request) {
        AddCardCommand command = new AddCardCommand(
                request.getCollectionId(),
                request.getCardName(),
                request.getQuantity(),
                request.getCondition(),
                request.getIsFoil(),
                request.getLanguage(),
                request.getLang()
        );
        
        // Command execution (Write side)
        AddCardResponse response = addCardService.execute(command);
        
        // Query execution (Read side) to return the full object for API consistency
        CardYouOwnDTO result = getCardYouOwnByIdService.execute(new GetCardYouOwnByIdQuery(response.id())).card();
        
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CardYouOwnDTO> updateCard(@PathVariable Long id, @Valid @RequestBody CardYouOwnRequest request) {
        // Command
        updateCardService.execute(new UpdateCardCommand(
                id, request.getQuantity(), request.getCondition(), request.getIsFoil(), request.getLanguage()));
        
        // Query
        CardYouOwnDTO result = getCardYouOwnByIdService.execute(new GetCardYouOwnByIdQuery(id)).card();
        
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteCard(@PathVariable Long id) {
        deleteCardService.execute(new DeleteCardCommand(id));
        return ResponseEntity.ok(Map.of("message", "Carta eliminada correctamente"));
    }

    @GetMapping("/collection/{collectionId}")
    public ResponseEntity<List<CardYouOwnDTO>> listByCollection(@PathVariable Long collectionId) {
        return ResponseEntity.ok(getCardsByCollectionService.execute(new GetCardsByCollectionQuery(collectionId)).cards());
    }

    @GetMapping("/search/global")
    public ResponseEntity<List<CardYouOwnDTO>> searchGlobal(@RequestParam String term) {
        return ResponseEntity.ok(searchGlobalService.execute(new SearchGlobalQuery(term)).cards());
    }

    @GetMapping("/search/collection/{collectionId}")
    public ResponseEntity<List<CardYouOwnDTO>> searchInCollection(@PathVariable Long collectionId, @RequestParam String term) {
        return ResponseEntity.ok(searchInCollectionService.execute(new SearchInCollectionQuery(collectionId, term)).cards());
    }

    @GetMapping("/search/type")
    public ResponseEntity<List<CardYouOwnDTO>> searchByType(@RequestParam String type) {
        return ResponseEntity.ok(searchByTypeService.execute(new SearchByTypeQuery(type)).cards());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CardYouOwnDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(getCardYouOwnByIdService.execute(new GetCardYouOwnByIdQuery(id)).card());
    }
}
