package pga.magiccollectionspring.card.domain;

import org.springframework.data.domain.Pageable;
import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.util.List;

public interface IIndexBuildLogRepository extends IRepository<IndexBuildLog, Long> {
    List<IndexBuildLog> findByLanguageCodeOrderByStartedAtDesc(String languageCode, Pageable pageable);
}
