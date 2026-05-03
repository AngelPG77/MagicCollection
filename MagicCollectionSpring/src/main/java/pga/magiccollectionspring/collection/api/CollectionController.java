package pga.magiccollectionspring.collection.api;

import pga.magiccollectionspring.collection.api.dto.AddCardToCollectionRequest;
import pga.magiccollectionspring.collection.api.dto.CollectionCardDTO;
import pga.magiccollectionspring.collection.api.dto.CollectionDTO;
import pga.magiccollectionspring.collection.api.dto.CollectionRequest;
import pga.magiccollectionspring.collection.api.dto.UpdateCardInCollectionRequest;
import pga.magiccollectionspring.collection.application.command.AddCardToCollection.AddCardToCollectionCommand;
import pga.magiccollectionspring.collection.application.command.AddCardToCollection.AddCardToCollectionService;
import pga.magiccollectionspring.collection.application.command.CreateCollection.CreateCollectionCommand;
import pga.magiccollectionspring.collection.application.command.CreateCollection.CreateCollectionResponse;
import pga.magiccollectionspring.collection.application.command.CreateCollection.CreateCollectionService;
import pga.magiccollectionspring.collection.application.command.DeleteCollection.DeleteCollectionCommand;
import pga.magiccollectionspring.collection.application.command.DeleteCollection.DeleteCollectionService;
import pga.magiccollectionspring.collection.application.command.RemoveCardFromCollection.RemoveCardFromCollectionCommand;
import pga.magiccollectionspring.collection.application.command.RemoveCardFromCollection.RemoveCardFromCollectionService;
import pga.magiccollectionspring.collection.application.command.UpdateCardInCollection.UpdateCardInCollectionCommand;
import pga.magiccollectionspring.collection.application.command.UpdateCardInCollection.UpdateCardInCollectionService;
import pga.magiccollectionspring.collection.application.command.UpdateCollection.UpdateCollectionCommand;
import pga.magiccollectionspring.collection.application.command.UpdateCollection.UpdateCollectionService;
import pga.magiccollectionspring.collection.application.query.GetAllUserCards.GetAllUserCardsQuery;
import pga.magiccollectionspring.collection.application.query.GetAllUserCards.GetAllUserCardsService;
import pga.magiccollectionspring.collection.application.query.GetCollectionById.GetCollectionByIdQuery;
import pga.magiccollectionspring.collection.application.query.GetCollectionById.GetCollectionByIdService;
import pga.magiccollectionspring.collection.application.query.GetCollectionsByUser.GetCollectionsByUserQuery;
import pga.magiccollectionspring.collection.application.query.GetCollectionsByUser.GetCollectionsByUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collections")
public class CollectionController {

    private final CreateCollectionService createCollectionService;
    private final UpdateCollectionService updateCollectionService;
    private final DeleteCollectionService deleteCollectionService;
    private final GetCollectionsByUserService getCollectionsByUserService;
    private final GetCollectionByIdService getCollectionByIdService;
    private final AddCardToCollectionService addCardToCollectionService;
    private final UpdateCardInCollectionService updateCardInCollectionService;
    private final RemoveCardFromCollectionService removeCardFromCollectionService;
    private final GetAllUserCardsService getAllUserCardsService;

    public CollectionController(CreateCollectionService createCollectionService,
                                UpdateCollectionService updateCollectionService,
                                DeleteCollectionService deleteCollectionService,
                                GetCollectionsByUserService getCollectionsByUserService,
                                GetCollectionByIdService getCollectionByIdService,
                                AddCardToCollectionService addCardToCollectionService,
                                UpdateCardInCollectionService updateCardInCollectionService,
                                RemoveCardFromCollectionService removeCardFromCollectionService,
                                GetAllUserCardsService getAllUserCardsService) {
        this.createCollectionService = createCollectionService;
        this.updateCollectionService = updateCollectionService;
        this.deleteCollectionService = deleteCollectionService;
        this.getCollectionsByUserService = getCollectionsByUserService;
        this.getCollectionByIdService = getCollectionByIdService;
        this.addCardToCollectionService = addCardToCollectionService;
        this.updateCardInCollectionService = updateCardInCollectionService;
        this.removeCardFromCollectionService = removeCardFromCollectionService;
        this.getAllUserCardsService = getAllUserCardsService;
    }

    @PostMapping
    public ResponseEntity<CollectionDTO> create(@RequestBody CollectionRequest request) {
        CreateCollectionResponse response = createCollectionService.execute(new CreateCollectionCommand(request.getName()));
        CollectionDTO result = getCollectionByIdService.execute(new GetCollectionByIdQuery(response.id())).collection();
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public ResponseEntity<List<CollectionDTO>> getAll() {
        return ResponseEntity.ok(getCollectionsByUserService.execute(new GetCollectionsByUserQuery()).collections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(getCollectionByIdService.execute(new GetCollectionByIdQuery(id)).collection());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionDTO> update(@PathVariable Long id, @RequestBody CollectionRequest request) {
        updateCollectionService.execute(new UpdateCollectionCommand(id, request.getName()));
        CollectionDTO result = getCollectionByIdService.execute(new GetCollectionByIdQuery(id)).collection();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        deleteCollectionService.execute(new DeleteCollectionCommand(id));
        return ResponseEntity.ok(Map.of("message", "Coleccion eliminada correctamente"));
    }

    @PostMapping("/{id}/cards")
    public ResponseEntity<Long> addCard(@PathVariable Long id, @RequestBody AddCardToCollectionRequest request) {
        Long cardId = addCardToCollectionService.execute(new AddCardToCollectionCommand(
                id,
                request.getScryfallId(),
                request.getName(),
                request.getTypeLine(),
                request.getManaCost(),
                request.getImageUrl(),
                request.getQuantity(),
                request.getFoil(),
                request.getLanguage(),
                request.getCondition()
        ));
        return ResponseEntity.ok(cardId);
    }

    @PutMapping("/{id}/cards/{cardId}")
    public ResponseEntity<Void> updateCard(
            @PathVariable Long id,
            @PathVariable Long cardId,
            @RequestBody UpdateCardInCollectionRequest request) {
        updateCardInCollectionService.execute(new UpdateCardInCollectionCommand(
                id,
                cardId,
                request.getQuantity(),
                request.getFoil(),
                request.getLanguage(),
                request.getCondition()
        ));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/cards/{cardId}")
    public ResponseEntity<Void> removeCard(@PathVariable Long id, @PathVariable Long cardId) {
        removeCardFromCollectionService.execute(new RemoveCardFromCollectionCommand(id, cardId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all-cards")
    public ResponseEntity<List<CollectionCardDTO>> getAllUserCards() {
        return ResponseEntity.ok(getAllUserCardsService.execute(new GetAllUserCardsQuery()).cards());
    }
}
