package com.jonathanmarquez.security.security.service;

import com.jonathanmarquez.security.security.model.UserProfile;
import com.jonathanmarquez.security.security.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestionar usuarios completos:
 * - Crea usuario en 'users' (Spring Security)
 * - Asigna authorities/groups
 * - Crea perfil extendido en 'user_profiles' (JPA)
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final JdbcUserDetailsManager jdbcUserDetailsManager;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea un usuario completo: credenciales + perfil extendido.
     * 
     * @param username username único
     * @param rawPassword contraseña en texto plano (se encodificará)
     * @param email email del usuario
     * @param authorities lista de authorities a asignar
     * @return el perfil creado
     */
    @Transactional
    public UserProfile createUser(String username, String rawPassword, String email, List<String> authorities) {
        
        // 1. Crear usuario en Spring Security
        var userDetails = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .authorities(authorities.toArray(new String[0]))
                .build();
        
        jdbcUserDetailsManager.createUser(userDetails);

        // 2. Crear perfil extendido
        UserProfile profile = UserProfile.builder()
                .username(username)
                .email(email)
                .build();

        return userProfileRepository.save(profile);
    }

    /**
     * Actualiza solo el perfil extendido (no afecta credenciales).
     */
    public UserProfile updateProfile(UserProfile profile) {
        return userProfileRepository.save(profile);
    }

    /**
     * Elimina completamente un usuario (cascada elimina perfil).
     */
    @Transactional
    public void deleteUser(String username) {
        jdbcUserDetailsManager.deleteUser(username);
        // La FK con ON DELETE CASCADE eliminará el perfil automáticamente
    }
}