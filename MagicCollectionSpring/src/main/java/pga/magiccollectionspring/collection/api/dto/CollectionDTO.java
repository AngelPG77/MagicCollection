package pga.magiccollectionspring.collection.api.dto;

public class CollectionDTO {
    private Long id;
    private String name;
    private String ownerUsername;

    public CollectionDTO() {}

    public CollectionDTO(Long id, String name, String ownerUsername) {
        this.id = id;
        this.name = name;
        this.ownerUsername = ownerUsername;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}