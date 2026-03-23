package pga.magiccollectionspring.auth.application.query.Login;

import pga.magiccollectionspring.shared.abstractions.IQueryService;
import pga.magiccollectionspring.shared.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements IQueryService<LoginQuery, LoginResponse> {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse execute(LoginQuery query) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(query.username(), query.password())
        );
        String token = jwtService.generateToken(query.username());
        return new LoginResponse(token);
    }
}