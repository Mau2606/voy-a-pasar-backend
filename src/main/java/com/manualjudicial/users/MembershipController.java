package com.manualjudicial.users;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/memberships")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approveUser(@PathVariable("id") Long id) {
        User user = membershipService.approveUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario " + user.getEmail() + " aprobado y pendiente de pago."));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activateMembership(
            @PathVariable("id") Long id,
            @RequestParam("type") AccessType type) {
        User user = membershipService.activateMembership(id, type);
        return ResponseEntity.ok(Map.of("message", "Membresía activada como " + type.name() + "."));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable("id") Long id) {
        User user = membershipService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario desactivado exitosamente."));
    }
}
