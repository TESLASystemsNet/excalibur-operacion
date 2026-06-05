package mx.com.excalibur.operacion.auth;

import mx.com.excalibur.operacion.users.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final ExcaliburUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(
            ExcaliburUserDetailsService userDetailsService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository
    ) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        AuthenticatedUser user = (AuthenticatedUser) userDetailsService.loadUserByUsername(request.username());

        if (!user.isEnabled() || !user.isAccountNonLocked()
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            userRepository.recordFailedLogin(user.id());
            throw new BadCredentialsException("Credenciales invalidas");
        }

        userRepository.recordSuccessfulLogin(user.id());
        String token = jwtService.createToken(user);
        return new LoginResponse(
                "Bearer",
                token,
                user.id(),
                user.getUsername(),
                user.requiereCambioPassword(),
                user.roles(),
                user.permisos()
        );
    }

    public AuthenticatedUserResponse me(AuthenticatedUser user) {
        return AuthenticatedUserResponse.from(user);
    }

    @Transactional
    public void changeOwnPassword(AuthenticatedUser user, ChangeOwnPasswordRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Password actual incorrecto");
        }
        userRepository.changePassword(user.id(), passwordEncoder.encode(request.newPassword()), false);
    }
}
