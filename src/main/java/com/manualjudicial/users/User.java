package com.manualjudicial.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore // never include password in JSON responses
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Account status for admin approval workflow.
     * Default: ACTIVE (so existing users aren't locked out on migration).
     * New registrations set this to PENDING.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "account_status", nullable = false, columnDefinition = "varchar(20) default 'ACTIVE'")
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    /**
     * Per-user unlock threshold (0-100). A chapter is unlocked when the user's
     * best score in the PREVIOUS chapter meets or exceeds this value.
     * Default: 70 (%).
     */
    @Builder.Default
    @Column(name = "custom_threshold", nullable = false, columnDefinition = "integer default 70")
    private Integer customThreshold = 70;

    // ── UserDetails impl ─────────────────────────────────────────────────────

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }


    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return accountStatus == null || accountStatus != AccountStatus.SUSPENDED;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return accountStatus == null || accountStatus == AccountStatus.ACTIVE;
    }
}
