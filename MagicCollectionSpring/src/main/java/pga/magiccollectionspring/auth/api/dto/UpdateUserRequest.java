package pga.magiccollectionspring.auth.api.dto;

public class UpdateUserRequest {
    private String newUsername;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
}
