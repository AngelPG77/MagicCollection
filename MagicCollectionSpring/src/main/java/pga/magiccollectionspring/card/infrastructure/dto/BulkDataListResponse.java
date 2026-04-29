package pga.magiccollectionspring.card.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkDataListResponse {
    private List<BulkDataDTO> data;

    public List<BulkDataDTO> getData() { return data; }
    public void setData(List<BulkDataDTO> data) { this.data = data; }
}
