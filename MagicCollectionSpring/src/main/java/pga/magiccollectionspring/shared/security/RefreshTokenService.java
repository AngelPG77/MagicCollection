package pga.magiccollectionspring.shared.security;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pga.magiccollectionspring.user.domain.IRefreshTokenRepository;
import pga.magiccollectionspring.user.domain.IUserRepository;
import pga.magiccollectionspring.user.domain.RefreshToken;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUserRepository userRepository;

    public RefreshTokenService(IRefreshTokenRepository refreshTokenRepository, IUserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId.toString()));

        Instant expiryDate = Instant.now().plusMillis(2592000000L); // 30 days
        String tokenValue = UUID.randomUUID().toString();

        // Eliminar tokens previos para este usuario para evitar duplicados (UK en user_id)
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(expiryDate);
        refreshToken.setToken(tokenValue);

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}
