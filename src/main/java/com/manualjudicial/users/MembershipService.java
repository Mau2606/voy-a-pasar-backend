package com.manualjudicial.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipService {

    private final UserRepository userRepository;

    @Transactional
    public User approveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new RuntimeException("El usuario ya está activo");
        }
        
        user.setAccountStatus(AccountStatus.PENDING_PAYMENT);
        return userRepository.save(user);
    }

    @Transactional
    public User activateMembership(Long id, AccessType type) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setAccessType(type);
        user.setAccountStatus(AccountStatus.ACTIVE);
        
        LocalDateTime now = LocalDateTime.now();
        switch (type) {
            case ONE_DAY:
                user.setExpirationDate(now.plusDays(1));
                break;
            case THIRTY_DAYS:
                user.setExpirationDate(now.plusDays(30));
                break;
            case PERMANENT:
                user.setExpirationDate(null);
                break;
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public User deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        user.setAccountStatus(AccountStatus.INACTIVE);
        return userRepository.save(user);
    }

    // Tarea programada: Ejecutar cada hora para inhabilitar cumplido el plazo
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void expireMemberships() {
        log.info("Ejecutando tarea programada: Expiración de membresías...");
        List<User> expiredUsers = userRepository.findByAccountStatusAndExpirationDateBefore(
                AccountStatus.ACTIVE, LocalDateTime.now());
                
        for (User user : expiredUsers) {
            user.setAccountStatus(AccountStatus.INACTIVE);
            log.info("Membresía expirada para usuario: {}", user.getEmail());
        }
        
        userRepository.saveAll(expiredUsers);
        log.info("Tarea programada completada. Usuarios expirados: {}", expiredUsers.size());
    }
}
