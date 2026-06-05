package mx.com.excalibur.operacion.users;

import java.util.List;
import mx.com.excalibur.operacion.common.BadRequestException;
import mx.com.excalibur.operacion.common.NotFoundException;
import mx.com.excalibur.operacion.roles.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserSummary> findAll(String search, Boolean includeDeleted) {
        return userRepository.findAll(search, includeDeleted);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
    }

    @Transactional
    public User create(UserCreateRequest request, Long currentUserId) {
        Long userId = userRepository.create(request, passwordEncoder.encode(request.password()));
        replaceRolesIfPresent(userId, request.roles(), currentUserId);
        return findById(userId);
    }

    @Transactional
    public User update(Long id, UserUpdateRequest request, Long currentUserId) {
        findById(id);
        userRepository.update(id, request);
        replaceRolesIfPresent(id, request.roles(), currentUserId);
        return findById(id);
    }

    @Transactional
    public void changePassword(Long id, PasswordChangeRequest request) {
        findById(id);
        userRepository.changePassword(id, passwordEncoder.encode(request.password()), false);
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        findById(id);
        userRepository.softDelete(id, currentUserId);
    }

    private void replaceRolesIfPresent(Long userId, List<String> roleNames, Long currentUserId) {
        if (roleNames == null) {
            return;
        }
        List<String> normalized = roleNames.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.trim().toUpperCase())
                .distinct()
                .toList();
        List<Long> roleIds = roleRepository.findActiveIdsByNames(normalized);
        if (roleIds.size() != normalized.size()) {
            throw new BadRequestException("Uno o mas roles no existen o estan inactivos");
        }
        userRepository.replaceRoles(userId, roleIds, currentUserId);
    }
}
