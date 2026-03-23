package pga.magiccollectionspring.collection.api.dto;

public class CollectionRequest {
    private String name;

    public CollectionRequest() {}
    public CollectionRequest(String name) { this.name = name; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}