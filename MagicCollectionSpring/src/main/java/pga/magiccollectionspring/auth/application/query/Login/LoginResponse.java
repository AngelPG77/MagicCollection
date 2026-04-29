package pga.magiccollectionspring.auth.application.query.Login;

public record LoginResponse(String token, String refreshToken, Long userId) {}
