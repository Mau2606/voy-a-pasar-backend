package com.manualjudicial.users;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @Transactional
    public User create(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already in use: " + dto.getEmail());
        }
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole() != null ? dto.getRole() : Role.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
                
        applyAccessType(user, dto.getAccessType());
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, UserDTO dto) {
        User user = findById(id);
        user.setName(dto.getName());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getAccessType() != null) {
            applyAccessType(user, dto.getAccessType());
        }
        return userRepository.save(user);
    }
    
    private void applyAccessType(User user, AccessType type) {
        if (type == null) return;
        user.setAccessType(type);
        user.setAccountStatus(AccountStatus.ACTIVE);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        switch (type) {
            case ONE_DAY -> user.setExpirationDate(now.plusDays(1));
            case THIRTY_DAYS -> user.setExpirationDate(now.plusDays(30));
            case PERMANENT -> user.setExpirationDate(null);
        }
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
