package com.jonathanmarquez.security.security.model;

import com.jonathanmarquez.security.jpa.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad JPA para extender la información del usuario.
 * Se relaciona con la tabla 'users' de Spring Security mediante el username.
 * <p>
 * JPA creará esta tabla automáticamente con FK a 'users'.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends AuditableEntity {

  @Id
  @Column(name = "email", nullable = false, unique = true)
  private String email; // FK a users.username

  @Column(name = "first_name", length = 100, nullable = false)
  private String firstName;

  @Column(name = "last_name", length = 100, nullable = false)
  private String lastName;


  @Column(name = "phone", length = 20)
  private String phone;

  @Column(name = "profile_picture_url", length = 500)
  private String profilePictureUrl;

  @Column(name = "date_of_birth", nullable = false)
  private LocalDate dateOfBirth;

  @Column(name = "is_email_verified", nullable = false)
  private boolean isEmailVerified;
}