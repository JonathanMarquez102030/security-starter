package com.jonathanmarquez.security.security.config;

import com.jonathanmarquez.security.security.enums.AuthorizationMode;
import com.jonathanmarquez.security.security.model.SecurityUserDetails;
import com.jonathanmarquez.security.security.model.UserProfile;
import com.jonathanmarquez.security.security.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio personalizado que combina:
 * 1. Datos de autenticación desde las tablas de Spring Security (users, authorities/groups)
 * 2. Datos de perfil extendidos desde user_profiles (JPA)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final JdbcTemplate jdbcTemplate;
  private final UserProfileRepository userProfileRepository;
  private final SecurityProperties securityProperties;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    // 1. Cargar usuario base desde 'users'
    String userSql = "SELECT username, password, enabled FROM users WHERE username = ?";

    var userOpt = jdbcTemplate.query(userSql, new Object[]{username}, (rs, rowNum) ->
        new Object[]{
            rs.getString("username"),
            rs.getString("password"),
            rs.getBoolean("enabled")
        }
    ).stream().findFirst();

    if (userOpt.isEmpty()) {
      throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }

    Object[] userData = userOpt.get();
    String dbUsername = (String) userData[0];
    String password = (String) userData[1];
    boolean enabled = (Boolean) userData[2];

    // 2. Cargar autoridades según el modo configurado
    List<String> authorities = loadAuthorities(dbUsername);

    // 3. Cargar perfil extendido (opcional)
    UserProfile profile = userProfileRepository.findById(dbUsername).orElse(null);

    // 4. Construir SecurityUser
    return new SecurityUserDetails(dbUsername, password, enabled, authorities, profile);
  }

  /**
   * Carga las autoridades según el modo configurado (AUTHORITIES o GROUPS).
   */
  private List<String> loadAuthorities(String username) {

    if (securityProperties.getAuthorizationMode() == AuthorizationMode.GROUPS) {
      // Consulta para obtener autoridades desde grupos
      String groupSql = """
              SELECT ga.authority
              FROM group_authorities ga
              INNER JOIN group_members gm ON ga.group_id = gm.group_id
              WHERE gm.username = ?
          """;
      return jdbcTemplate.queryForList(groupSql, String.class, username);

    } else {
      // Consulta para obtener autoridades directas
      String authSql = "SELECT authority FROM authorities WHERE username = ?";
      return jdbcTemplate.queryForList(authSql, String.class, username);
    }
  }
}