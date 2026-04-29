package pga.magiccollectionspring.card.infrastructure;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pga.magiccollectionspring.card.domain.IIndexBuildLogRepository;
import pga.magiccollectionspring.card.domain.IndexBuildLog;

import java.util.List;

@Repository
public interface IndexBuildLogRepository extends JpaRepository<IndexBuildLog, Long>, IIndexBuildLogRepository {
    List<IndexBuildLog> findByLanguageCodeOrderByStartedAtDesc(String languageCode, Pageable pageable);
}
