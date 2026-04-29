package pga.magiccollectionspring.wantlist.api.dto;

public class CreateWantListRequest {
    private String name;

    public CreateWantListRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
