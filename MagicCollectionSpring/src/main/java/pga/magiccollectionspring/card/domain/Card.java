package pga.magiccollectionspring.card.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "master_cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "set_code", nullable = false, length = 10)
    private String setCode;

    @Column(name = "scryfall_id", unique = true, nullable = false)
    private String scryfallId;

    @Column(name = "oracle_text", columnDefinition = "TEXT")
    private String oracleText;

    @Column(name = "type_line")
    private String typeLine;

    public Card() {}

    public Card(Long id, String name, String setCode, String scryfallId, String oracleText, String typeLine) {
        this.id = id;
        this.name = name;
        this.setCode = setCode;
        this.scryfallId = scryfallId;
        this.oracleText = oracleText;
        this.typeLine = typeLine;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSetCode() { return setCode; }
    public void setSetCode(String setCode) { this.setCode = setCode; }
    public String getScryfallId() { return scryfallId; }
    public void setScryfallId(String scryfallId) { this.scryfallId = scryfallId; }
    public String getOracleText() { return oracleText; }
    public void setOracleText(String oracleText) { this.oracleText = oracleText; }
    public String getTypeLine() { return typeLine; }
    public void setTypeLine(String typeLine) { this.typeLine = typeLine; }
}