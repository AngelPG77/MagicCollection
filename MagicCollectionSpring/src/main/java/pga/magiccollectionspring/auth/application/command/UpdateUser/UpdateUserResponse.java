package pga.magiccollectionspring.auth.application.command.UpdateUser;

public record UpdateUserResponse(boolean success, String message, String newUsername, String token) {
    
    public UpdateUserResponse(boolean success, String message, String newUsername) {
        this(success, message, newUsername, null);
    }
}
