package pga.magiccollectionspring.card.infrastructure.dto;

import java.util.List;

public class ScryfallBulkDataResponse {
    private List<BulkDataDTO> data;

    public List<BulkDataDTO> getData() { return data; }
    public void setData(List<BulkDataDTO> data) { this.data = data; }
}
