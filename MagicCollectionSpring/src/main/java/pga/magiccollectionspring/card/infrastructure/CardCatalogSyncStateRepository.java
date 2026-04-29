package pga.magiccollectionspring.card.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pga.magiccollectionspring.card.domain.CardCatalogSyncState;
import pga.magiccollectionspring.card.domain.ICardCatalogSyncStateRepository;

import java.util.Optional;

@Repository
public interface CardCatalogSyncStateRepository extends JpaRepository<CardCatalogSyncState, String>, ICardCatalogSyncStateRepository {
    @Override
    Optional<CardCatalogSyncState> findByBulkType(String bulkType);
}
