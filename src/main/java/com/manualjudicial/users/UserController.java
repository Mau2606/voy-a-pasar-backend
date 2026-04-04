package com.manualjudicial.users;

import com.manualjudicial.auth.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * GET /api/admin/users?page=0&size=15
     */
    @GetMapping
    public Page<User> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        
        Sort sort = Sort.by(Sort.Direction.ASC, "accountStatus", "role", "name");
        Pageable sortedPageable = PageRequest.of(page, size, sort);
        return userRepository.findAll(sortedPageable);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<User> users = userRepository.findAll();
        
        long active = users.stream().filter(u -> u.getAccountStatus() != null && u.getAccountStatus().name().equals("ACTIVE")).count();
        long pending = users.stream().filter(u -> u.getAccountStatus() != null && u.getAccountStatus().name().equals("PENDING_APPROVAL")).count();
        long suspended = users.stream().filter(u -> u.getAccountStatus() != null && u.getAccountStatus().name().equals("SUSPENDED")).count();
        long inactive = users.stream().filter(u -> u.getAccountStatus() != null && u.getAccountStatus().name().equals("INACTIVE")).count();
        
        long perm = users.stream().filter(u -> u.getAccountStatus() != null && u.getAccountStatus().name().equals("ACTIVE") && u.getAccessType() != null && u.getAccessType().name().equals("PERMANENT")).count();
        long thirty = users.stream().filter(u -> u.getAccountStatus() != null && u.getAccountStatus().name().equals("ACTIVE") && u.getAccessType() != null && u.getAccessType().name().equals("THIRTY_DAYS")).count();
        long one = users.stream().filter(u -> u.getAccountStatus() != null && u.getAccountStatus().name().equals("ACTIVE") && u.getAccessType() != null && u.getAccessType().name().equals("ONE_DAY")).count();
        
        return Map.of(
            "active", active,
            "pending", pending,
            "suspended", suspended + inactive, // treating both as disabled
            "permanent", perm,
            "thirtyDays", thirty,
            "oneDay", one
        );
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

}
