package mx.com.excalibur.operacion.auth;

import java.util.Collection;
import java.util.List;
import mx.com.excalibur.operacion.users.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails {

    private final User user;
    private final List<GrantedAuthority> authorities;

    public AuthenticatedUser(User user) {
        this.user = user;
        this.authorities = user.permisos().stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    public Long id() {
        return user.id();
    }

    public List<String> roles() {
        return user.roles();
    }

    public List<String> permisos() {
        return user.permisos();
    }

    public boolean requiereCambioPassword() {
        return user.requiereCambioPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.passwordHash();
    }

    @Override
    public String getUsername() {
        return user.username();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.bloqueado();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.activo() && !user.eliminado();
    }
}
