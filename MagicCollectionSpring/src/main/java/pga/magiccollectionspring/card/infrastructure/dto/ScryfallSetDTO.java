package pga.magiccollectionspring.card.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScryfallSetDTO {
    private String code;
    private String name;
    
    @JsonProperty("released_at")
    private LocalDate releasedAt;
    
    @JsonProperty("set_type")
    private String setType;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDate releasedAt) { this.releasedAt = releasedAt; }
    public String getSetType() { return setType; }
    public void setSetType(String setType) { this.setType = setType; }
}
