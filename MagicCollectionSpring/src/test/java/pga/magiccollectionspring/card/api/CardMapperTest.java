package pga.magiccollectionspring.card.api;

import org.junit.jupiter.api.Test;
import pga.magiccollectionspring.card.api.dto.CardMetadataDTO;
import pga.magiccollectionspring.card.domain.CardIndexView;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardMapperTest {

    private final CardMapper mapper = new CardMapper();

    @Test
    void mapMetadataUsesLocalizedOverrideForRequestedLanguage() {
        CardIndexView card = buildCard("Lightning Bolt");

        CardMetadataDTO dto = mapper.mapMetadata(card, "es", "Fulminar");

        assertEquals("Fulminar", dto.localizedName());
        assertEquals("Lightning Bolt", dto.defaultName());
    }

    @Test
    void mapMetadataKeepsEnglishNameWhenLanguageIsEnglish() {
        CardIndexView card = buildCard("Lightning Bolt");

        CardMetadataDTO dto = mapper.mapMetadata(card, "en", "Fulminar");

        assertEquals("Lightning Bolt", dto.localizedName());
        assertEquals("Lightning Bolt", dto.defaultName());
    }

    private CardIndexView buildCard(String name) {
        return new CardIndexView() {
            @Override
            public String getOracleId() { return "oracle-id"; }

            @Override
            public String getScryfallId() { return "test-id"; }

            @Override
            public String getName() { return name; }

            @Override
            public Integer getColorMask() { return 0; }

            @Override
            public Integer getIdentityMask() { return 0; }

            @Override
            public String getManaCost() { return "{R}"; }

            @Override
            public Float getCmc() { return 1.0f; }

            @Override
            public Integer getRarityRank() { return 2; }

            @Override
            public String getTypeLine() { return "Instant"; }

            @Override
            public String getSetCode() { return "lea"; }

            @Override
            public String getImageSmallUrl() { return null; }
        };
    }
}
