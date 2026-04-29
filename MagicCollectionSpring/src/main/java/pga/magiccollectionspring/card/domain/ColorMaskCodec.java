package pga.magiccollectionspring.card.domain;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorMaskCodec {
    public static final int WHITE = 1;      // 2^0
    public static final int BLUE = 2;       // 2^1
    public static final int BLACK = 4;      // 2^2
    public static final int RED = 8;        // 2^3
    public static final int GREEN = 16;     // 2^4
    public static final int COLORLESS = 32; // 2^5

    private static final Pattern MANA_TOKEN_PATTERN = Pattern.compile("\\{([^}]*)}");

    private ColorMaskCodec() {
    }

    public static int toMask(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return 0;
        }

        int mask = 0;
        for (String raw : symbols) {
            if (raw == null || raw.isBlank()) {
                continue;
            }

            String symbol = raw.trim().toUpperCase(Locale.ROOT);
            for (int i = 0; i < symbol.length(); i++) {
                mask |= bitFor(symbol.charAt(i));
            }
        }
        return mask;
    }

    public static int toMaskFromManaCost(String manaCost) {
        if (manaCost == null || manaCost.isBlank()) {
            return 0;
        }

        int mask = 0;
        Matcher matcher = MANA_TOKEN_PATTERN.matcher(manaCost.toUpperCase(Locale.ROOT));
        while (matcher.find()) {
            String token = matcher.group(1);
            for (int i = 0; i < token.length(); i++) {
                mask |= bitFor(token.charAt(i));
            }
        }
        return mask;
    }

    public static int colorMask(List<String> colors, String manaCost) {
        int fromColors = toMask(colors);
        if (fromColors != 0) {
            return fromColors;
        }
        return toMaskFromManaCost(manaCost);
    }

    public static int rarityRank(String rarity) {
        if (rarity == null) {
            return 0;
        }

        return switch (rarity.trim().toLowerCase(Locale.ROOT)) {
            case "common" -> 0;
            case "uncommon" -> 1;
            case "rare" -> 2;
            case "mythic" -> 3;
            default -> 0;
        };
    }

    private static int bitFor(char symbol) {
        return switch (symbol) {
            case 'W' -> WHITE;
            case 'U' -> BLUE;
            case 'B' -> BLACK;
            case 'R' -> RED;
            case 'G' -> GREEN;
            case 'C' -> COLORLESS;
            default -> 0;
        };
    }
}
