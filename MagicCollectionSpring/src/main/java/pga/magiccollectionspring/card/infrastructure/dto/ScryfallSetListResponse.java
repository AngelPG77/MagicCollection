package pga.magiccollectionspring.card.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScryfallSetListResponse {
    private List<ScryfallSetDTO> data;
    private boolean hasMore;

    public List<ScryfallSetDTO> getData() { return data; }
    public void setData(List<ScryfallSetDTO> data) { this.data = data; }
    public boolean isHasMore() { return hasMore; }
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
}
