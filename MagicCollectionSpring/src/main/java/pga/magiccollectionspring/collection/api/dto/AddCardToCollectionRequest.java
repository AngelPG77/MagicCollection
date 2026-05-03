package pga.magiccollectionspring.collection.api.dto;

public class AddCardToCollectionRequest {
    private String scryfallId;
    private String name;
    private String typeLine;
    private String manaCost;
    private String imageUrl;
    private Integer quantity;
    private Boolean foil;
    private String language;
    private String condition;

    public String getScryfallId() { return scryfallId; }
    public void setScryfallId(String scryfallId) { this.scryfallId = scryfallId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTypeLine() { return typeLine; }
    public void setTypeLine(String typeLine) { this.typeLine = typeLine; }
    public String getManaCost() { return manaCost; }
    public void setManaCost(String manaCost) { this.manaCost = manaCost; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Boolean getFoil() { return foil; }
    public void setFoil(Boolean foil) { this.foil = foil; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}
