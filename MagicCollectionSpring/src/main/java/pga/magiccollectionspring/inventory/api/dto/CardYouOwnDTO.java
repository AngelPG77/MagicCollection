package pga.magiccollectionspring.inventory.api.dto;

public class CardYouOwnDTO {
    private Long id;
    private int quantity;
    private boolean isFoil;
    private String condition;
    private String language;
    private Long cardId;
    private String cardName;
    private Long collectionId;

    public CardYouOwnDTO() {}

    public CardYouOwnDTO(Long id, int quantity, boolean isFoil, String condition, String language, Long cardId, String cardName, Long collectionId) {
        this.id = id;
        this.quantity = quantity;
        this.isFoil = isFoil;
        this.condition = condition;
        this.language = language;
        this.cardId = cardId;
        this.cardName = cardName;
        this.collectionId = collectionId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public boolean isIsFoil() { return isFoil; }
    public void setIsFoil(boolean isFoil) { this.isFoil = isFoil; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }
    public String getCardName() { return cardName; }
    public void setCardName(String cardName) { this.cardName = cardName; }
    public Long getCollectionId() { return collectionId; }
    public void setCollectionId(Long collectionId) { this.collectionId = collectionId; }
}