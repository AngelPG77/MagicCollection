package pga.magiccollectionspring.card.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorMaskCodecTest {

    @Test
    void toMaskIncludesColorlessBit() {
        int mask = ColorMaskCodec.toMask(List.of("U", "R", "C"));
        assertEquals(42, mask);
    }

    @Test
    void toMaskFromManaCostParsesHybridAndColorless() {
        int mask = ColorMaskCodec.toMaskFromManaCost("{2/W}{U/B}{C}");
        assertEquals(ColorMaskCodec.WHITE | ColorMaskCodec.BLUE | ColorMaskCodec.BLACK | ColorMaskCodec.COLORLESS, mask);
    }

    @Test
    void rarityRankUsesExpectedScale() {
        assertEquals(0, ColorMaskCodec.rarityRank("common"));
        assertEquals(1, ColorMaskCodec.rarityRank("uncommon"));
        assertEquals(2, ColorMaskCodec.rarityRank("rare"));
        assertEquals(3, ColorMaskCodec.rarityRank("mythic"));
    }
}
