package pga.magiccollectionspring.card.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import pga.magiccollectionspring.card.domain.IndexLanguageState;
import pga.magiccollectionspring.card.domain.LanguageIndexStatus;
import pga.magiccollectionspring.card.infrastructure.IndexLanguageStateRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("languageIndex")
public class LanguageIndexHealthIndicator implements HealthIndicator {
    private final IndexLanguageStateRepository indexLanguageStateRepository;
    private final CardLanguageSupport cardLanguageSupport;
    private final long maxStalenessHours;

    public LanguageIndexHealthIndicator(
            IndexLanguageStateRepository indexLanguageStateRepository,
            CardLanguageSupport cardLanguageSupport,
            @Value("${magic.index.max-staleness-hours:48}") long maxStalenessHours
    ) {
        this.indexLanguageStateRepository = indexLanguageStateRepository;
        this.cardLanguageSupport = cardLanguageSupport;
        this.maxStalenessHours = maxStalenessHours;
    }

    @Override
    public Health health() {
        LocalDateTime staleThreshold = LocalDateTime.now().minusHours(maxStalenessHours);
        List<String> failed = new ArrayList<>();
        List<String> stale = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String language : cardLanguageSupport.getSupportedLanguages()) {
            IndexLanguageState state = indexLanguageStateRepository.findByLanguageCode(language).orElse(null);
            if (state == null) {
                missing.add(language);
                continue;
            }
            if (state.getStatus() == LanguageIndexStatus.FAILED) {
                failed.add(language);
                continue;
            }
            if (state.getGeneratedAt() == null || state.getGeneratedAt().isBefore(staleThreshold)) {
                stale.add(language);
            }
        }

        if (!failed.isEmpty() || !stale.isEmpty() || !missing.isEmpty()) {
            return Health.down()
                    .withDetail("failedLanguages", failed)
                    .withDetail("staleLanguages", stale)
                    .withDetail("missingLanguages", missing)
                    .withDetail("maxStalenessHours", maxStalenessHours)
                    .build();
        }

        return Health.up()
                .withDetail("supportedLanguages", cardLanguageSupport.getSupportedLanguages())
                .withDetail("maxStalenessHours", maxStalenessHours)
                .build();
    }
}
