package pga.magiccollectionspring.wantlist.api;

import pga.magiccollectionspring.wantlist.api.dto.*;
import pga.magiccollectionspring.wantlist.application.command.AddCardToWantList.*;
import pga.magiccollectionspring.wantlist.application.command.CreateWantList.*;
import pga.magiccollectionspring.wantlist.application.command.DeleteWantList.*;
import pga.magiccollectionspring.wantlist.application.command.RemoveCardFromWantList.*;
import pga.magiccollectionspring.wantlist.application.command.UpdateWantList.*;
import pga.magiccollectionspring.wantlist.application.command.UpdateCardInWantList.*;
import pga.magiccollectionspring.wantlist.application.query.GetWantListById.*;
import pga.magiccollectionspring.wantlist.application.query.GetWantListsByUser.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wantlists")
public class WantListController {

    private final CreateWantListService createWantListService;
    private final UpdateWantListService updateWantListService;
    private final DeleteWantListService deleteWantListService;
    private final AddCardToWantListService addCardToWantListService;
    private final UpdateCardInWantListService updateCardInWantListService;
    private final RemoveCardFromWantListService removeCardFromWantListService;
    private final GetWantListsByUserService getWantListsByUserService;
    private final GetWantListByIdService getWantListByIdService;

    public WantListController(
            CreateWantListService createWantListService,
            UpdateWantListService updateWantListService,
            DeleteWantListService deleteWantListService,
            AddCardToWantListService addCardToWantListService,
            UpdateCardInWantListService updateCardInWantListService,
            RemoveCardFromWantListService removeCardFromWantListService,
            GetWantListsByUserService getWantListsByUserService,
            GetWantListByIdService getWantListByIdService) {
        this.createWantListService = createWantListService;
        this.updateWantListService = updateWantListService;
        this.deleteWantListService = deleteWantListService;
        this.addCardToWantListService = addCardToWantListService;
        this.updateCardInWantListService = updateCardInWantListService;
        this.removeCardFromWantListService = removeCardFromWantListService;
        this.getWantListsByUserService = getWantListsByUserService;
        this.getWantListByIdService = getWantListByIdService;
    }

    @GetMapping
    public ResponseEntity<List<WantListDTO>> getMyWantLists() {
        var response = getWantListsByUserService.execute(new GetWantListsByUserQuery());
        return ResponseEntity.ok(response.wantLists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WantListDTO> getWantListById(@PathVariable Long id) {
        var response = getWantListByIdService.execute(new GetWantListByIdQuery(id));
        return ResponseEntity.ok(response.wantList());
    }

    @PostMapping
    public ResponseEntity<WantListDTO> createWantList(@RequestBody CreateWantListRequest request) {
        // Command (Write side)
        var response = createWantListService.execute(new CreateWantListCommand(request.getName()));
        
        // Query (Read side)
        var result = getWantListByIdService.execute(new GetWantListByIdQuery(response.id())).wantList();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WantListDTO> updateWantList(
            @PathVariable Long id,
            @RequestBody UpdateWantListRequest request) {
        // Command
        updateWantListService.execute(new UpdateWantListCommand(id, request.getName()));
        
        // Query
        var result = getWantListByIdService.execute(new GetWantListByIdQuery(id)).wantList();
        
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteWantList(@PathVariable Long id) {
        deleteWantListService.execute(new DeleteWantListCommand(id));
        return ResponseEntity.ok(Map.of("message", "Lista eliminada correctamente"));
    }

    @PostMapping("/{id}/cards")
    public ResponseEntity<Long> addCardToWantList(
            @PathVariable Long id,
            @RequestBody AddCardToWantListRequest request) {
        Long cardId = addCardToWantListService.execute(new AddCardToWantListCommand(
                id,
                request.getScryfallId(),
                request.getName(),
                request.getTypeLine(),
                request.getManaCost(),
                request.getImageUrl(),
                request.getQuantity() != null ? request.getQuantity() : 1,
                request.getFoil() != null ? request.getFoil() : false,
                request.getLanguage() != null ? request.getLanguage() : "en",
                request.getCondition() != null ? request.getCondition() : "NEAR_MINT"
        ));
        return ResponseEntity.ok(cardId);
    }

    @PutMapping("/{id}/cards/{cardId}")
    public ResponseEntity<Void> updateCardInWantList(
            @PathVariable Long id,
            @PathVariable Long cardId,
            @RequestBody UpdateCardInWantListRequest request) {
        updateCardInWantListService.execute(new UpdateCardInWantListCommand(
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
    public ResponseEntity<Void> removeCardFromWantList(
            @PathVariable Long id,
            @PathVariable Long cardId) {
        removeCardFromWantListService.execute(new RemoveCardFromWantListCommand(id, cardId));
        return ResponseEntity.noContent().build();
    }
}
