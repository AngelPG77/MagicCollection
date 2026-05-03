package pga.magiccollectionspring.collection.api.dto;

public class UpdateCardInCollectionRequest {
    private Integer quantity;
    private Boolean foil;
    private String language;
    private String condition;

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Boolean getFoil() { return foil; }
    public void setFoil(Boolean foil) { this.foil = foil; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}
