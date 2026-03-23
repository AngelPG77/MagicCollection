package pga.magiccollectionspring.auth.application.command.UpdateUser;

public record UpdateUserCommand(Long userId, String newUsername) {}
