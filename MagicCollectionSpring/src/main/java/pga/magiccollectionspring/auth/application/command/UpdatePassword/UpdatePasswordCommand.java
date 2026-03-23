package pga.magiccollectionspring.auth.application.command.UpdatePassword;

public record UpdatePasswordCommand(Long userId, String currentPassword, String newPassword) {}
