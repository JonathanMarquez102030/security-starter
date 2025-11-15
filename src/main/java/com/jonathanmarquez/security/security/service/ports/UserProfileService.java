package com.jonathanmarquez.security.security.service.ports;

import com.jonathanmarquez.security.security.enums.Role;
import com.jonathanmarquez.security.security.model.UserProfile;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserProfileService {

  UserProfile createUser(String email, String rawPassword, List<Role> roles);

  UserDetails getUserDetails(String email);

  UserProfile updateProfile(UserProfile profile);

  void updatePassword(String email, String newRawPassword);

  void deleteUser(String email);

  void setEnabled(String email, boolean enabled);

  boolean userExists(String email);
}
