package mx.com.excalibur.operacion.users;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mx.com.excalibur.operacion.auth.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserSummary> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") Boolean includeDeleted
    ) {
        return userService.findAll(search, includeDeleted);
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return UserResponse.from(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        User created = userService.create(request, currentUser.id());
        return ResponseEntity.created(URI.create("/api/users/" + created.id())).body(UserResponse.from(created));
    }

    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return UserResponse.from(userService.update(id, request, currentUser.id()));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        userService.delete(id, currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
