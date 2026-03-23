package pga.magiccollectionspring.inventory.api;

import pga.magiccollectionspring.inventory.api.dto.CardYouOwnDTO;
import pga.magiccollectionspring.inventory.api.dto.CardYouOwnRequest;
import pga.magiccollectionspring.inventory.application.command.AddCard.AddCardCommand;
import pga.magiccollectionspring.inventory.application.command.AddCard.AddCardService;
import pga.magiccollectionspring.inventory.application.command.DeleteCard.DeleteCardCommand;
import pga.magiccollectionspring.inventory.application.command.DeleteCard.DeleteCardService;
import pga.magiccollectionspring.inventory.application.command.UpdateCard.UpdateCardCommand;
import pga.magiccollectionspring.inventory.application.command.UpdateCard.UpdateCardService;
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

    public CardYouOwnController(AddCardService addCardService,
                                UpdateCardService updateCardService,
                                DeleteCardService deleteCardService,
                                GetCardsByCollectionService getCardsByCollectionService,
                                SearchGlobalService searchGlobalService,
                                SearchInCollectionService searchInCollectionService,
                                SearchByTypeService searchByTypeService) {
        this.addCardService = addCardService;
        this.updateCardService = updateCardService;
        this.deleteCardService = deleteCardService;
        this.getCardsByCollectionService = getCardsByCollectionService;
        this.searchGlobalService = searchGlobalService;
        this.searchInCollectionService = searchInCollectionService;
        this.searchByTypeService = searchByTypeService;
    }

    @PostMapping("/add")
    public ResponseEntity<CardYouOwnDTO> addCard(@RequestBody CardYouOwnRequest request) {
        return ResponseEntity.ok(addCardService.execute(new AddCardCommand(
                request.getCollectionId(), request.getCardName(), request.getQuantity(),
                request.getCondition(), request.getIsFoil(), request.getLanguage())).card());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CardYouOwnDTO> updateCard(@PathVariable Long id, @RequestBody CardYouOwnRequest request) {
        return ResponseEntity.ok(updateCardService.execute(new UpdateCardCommand(
                id, request.getQuantity(), request.getCondition(), request.getIsFoil(), request.getLanguage())).card());
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
}