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
        log.info("Iniciando rebuild asíncrono de todos los idiomas soportados...");
        List<String> languages = languageIndexBuildService.getSupportedLanguages();
        for (String lang : languages) {
            try {
                rebuildLanguageAsync(lang);
            } catch (Exception e) {
                log.error("Error al encolar rebuild para {}: {}", lang, e.getMessage());
            }
        }
    }

    @Async
    public void rebuildLanguageAsync(String languageCode) {
        try {
            log.info("Iniciando rebuild asíncrono para el idioma: {}", languageCode);
            languageIndexBuildService.rebuildLanguage(languageCode);
        } catch (Exception e) {
            log.error("Error reconstruyendo índice para {}: {}", languageCode, e.getMessage());
        }
    }
}
