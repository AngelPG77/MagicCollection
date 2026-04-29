package pga.magiccollectionspring.card.application;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CardLanguageSupport {

    private static final List<String> SUPPORTED_LANGUAGES = List.of(
            "en", "es", "fr", "de", "it", "pt", "ja", "ko", "ru", "zhs", "zht"
    );

    public List<String> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }

    public Set<String> getSupportedLanguagesSet() {
        return SUPPORTED_LANGUAGES.stream().collect(Collectors.toSet());
    }

    public String normalize(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return "en";
        }
        return languageCode.toLowerCase(Locale.ROOT);
    }

    public boolean isSupported(String languageCode) {
        return getSupportedLanguagesSet().contains(normalize(languageCode));
    }
}
