package pga.magiccollectionspring.card.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.util.Optional;

public interface ICardCatalogSyncStateRepository extends IRepository<CardCatalogSyncState, String> {
    Optional<CardCatalogSyncState> findByBulkType(String bulkType);
}
