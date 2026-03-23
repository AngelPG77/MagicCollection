package pga.magiccollectionspring.collection.api;

import pga.magiccollectionspring.collection.api.dto.CollectionDTO;
import pga.magiccollectionspring.collection.api.dto.CollectionRequest;
import pga.magiccollectionspring.collection.application.command.CreateCollection.CreateCollectionCommand;
import pga.magiccollectionspring.collection.application.command.CreateCollection.CreateCollectionService;
import pga.magiccollectionspring.collection.application.command.DeleteCollection.DeleteCollectionCommand;
import pga.magiccollectionspring.collection.application.command.DeleteCollection.DeleteCollectionService;
import pga.magiccollectionspring.collection.application.command.UpdateCollection.UpdateCollectionCommand;
import pga.magiccollectionspring.collection.application.command.UpdateCollection.UpdateCollectionService;
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

    public CollectionController(CreateCollectionService createCollectionService,
                                UpdateCollectionService updateCollectionService,
                                DeleteCollectionService deleteCollectionService,
                                GetCollectionsByUserService getCollectionsByUserService,
                                GetCollectionByIdService getCollectionByIdService) {
        this.createCollectionService = createCollectionService;
        this.updateCollectionService = updateCollectionService;
        this.deleteCollectionService = deleteCollectionService;
        this.getCollectionsByUserService = getCollectionsByUserService;
        this.getCollectionByIdService = getCollectionByIdService;
    }

    @PostMapping
    public ResponseEntity<CollectionDTO> create(@RequestBody CollectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createCollectionService.execute(new CreateCollectionCommand(request.getName())).collection());
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
        return ResponseEntity.ok(updateCollectionService.execute(new UpdateCollectionCommand(id, request.getName())).collection());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        deleteCollectionService.execute(new DeleteCollectionCommand(id));
        return ResponseEntity.ok(Map.of("message", "Coleccion eliminada correctamente"));
    }
}