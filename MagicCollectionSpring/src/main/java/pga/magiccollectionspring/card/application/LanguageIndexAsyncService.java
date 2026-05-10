package pga.magiccollectionspring.card.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LanguageIndexAsyncService {
    private static final Logger log = LoggerFactory.getLogger(LanguageIndexAsyncService.class);
    private final LanguageIndexBuildService languageIndexBuildService;

    public LanguageIndexAsyncService(LanguageIndexBuildService languageIndexBuildService) {
        this.languageIndexBuildService = languageIndexBuildService;
    }

    @Async
    public void rebuildAllSupportedLanguagesAsync() {
        log.info("[ASYNC-INDEX] Starting background rebuild for all supported languages...");
        List<String> languages = languageIndexBuildService.getSupportedLanguages();
        for (String lang : languages) {
            try {
                rebuildLanguageAsync(lang);
            } catch (Exception e) {
                log.error("[ASYNC-INDEX] Failed to queue rebuild for {}: {}", lang, e.getMessage());
            }
        }
    }

    @Async
    public void rebuildLanguageAsync(String languageCode) {
        try {
            log.info("[ASYNC-INDEX] Triggering background rebuild for language: {}", languageCode);
            languageIndexBuildService.rebuildLanguage(languageCode);
        } catch (Exception e) {
            log.error("[ASYNC-INDEX] Background rebuild failed for {}: {}", languageCode, e.getMessage());
        }
    }
}
