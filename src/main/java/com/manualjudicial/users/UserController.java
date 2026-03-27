package com.manualjudicial.users;

import com.manualjudicial.auth.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * GET /api/admin/users?page=0&size=10&sort=id,asc
     * Returns paginated user list.
     */
    @GetMapping
    public Page<User> getAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable("id") Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public User create(@RequestBody UserDTO dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable("id") Long id, @RequestBody UserDTO dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/admin/users/{id}/approve
     * Approves a PENDING user, sets status to ACTIVE, sends notification email.
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approveUser(@PathVariable("id") Long id) {
        User user = userService.findById(id);
        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            return ResponseEntity.ok(Map.of("message", "El usuario ya está activo."));
        }
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        emailService.sendAccountApprovedEmail(user);
        return ResponseEntity.ok(Map.of("message",
                "Usuario aprobado: " + user.getEmail()));
    }

    /**
     * PUT /api/admin/users/{id}/suspend
     * Suspends a user account.
     */
    @PutMapping("/{id}/suspend")
    public ResponseEntity<Map<String, String>> suspendUser(@PathVariable("id") Long id) {
        User user = userService.findById(id);
        user.setAccountStatus(AccountStatus.SUSPENDED);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message",
                "Usuario suspendido: " + user.getEmail()));
    }
}
