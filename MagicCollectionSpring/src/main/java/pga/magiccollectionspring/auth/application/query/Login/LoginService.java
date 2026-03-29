package pga.magiccollectionspring.auth.application.query.Login;

import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.security.JwtService;
import pga.magiccollectionspring.user.domain.IUserRepository;
import pga.magiccollectionspring.user.domain.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements IQueryService<LoginQuery, LoginResponse> {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final IUserRepository userRepository;

    public LoginService(AuthenticationManager authenticationManager, 
                        JwtService jwtService, 
                        IUserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public LoginResponse execute(LoginQuery query) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(query.username(), query.password())
        );
        
        User user = userRepository.findByUsername(query.username())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", query.username()));
        
        String token = jwtService.generateToken(query.username());
        return new LoginResponse(token, user.getId());
    }
}