package pga.magiccollectionspring.collection.application.query.GetCollectionsByUser;

import pga.magiccollectionspring.collection.api.dto.CollectionDTO;
import java.util.List;

public record GetCollectionsByUserResponse(List<CollectionDTO> collections) {}