package pga.magiccollectionspring.inventory.domain.enums;

public enum CardCondition {
    MINT("Mint"),
    NEAR_MINT("Near Mint"),
    EXCELLENT("Excellent"),
    GOOD("Good"),
    LIGHTLY_PLAYED("Lightly Played"),
    PLAYED("Played"),
    POOR("Poor");

    private final String displayName;

    CardCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static CardCondition fromString(String condition) {
        if (condition == null) throw new IllegalArgumentException("La condicion no puede ser nula");
        for (CardCondition con : CardCondition.values()) {
            if (con.displayName.equalsIgnoreCase(condition) || con.name().equalsIgnoreCase(condition)) {
                return con;
            }
        }
        throw new IllegalArgumentException("Condicion no valida: " + condition);
    }
}