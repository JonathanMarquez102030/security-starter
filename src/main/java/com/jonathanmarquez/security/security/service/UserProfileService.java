package com.jonathanmarquez.security.security.service;

import com.jonathanmarquez.security.security.enums.Role;
import com.jonathanmarquez.security.security.model.UserProfile;
import com.jonathanmarquez.security.security.model.dto.RegisterRequestDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserProfileService {

  UserProfile createUser(RegisterRequestDto registerRequestDto, List<Role> roles);

  UserDetails getUserDetails(String email);

  UserProfile updateProfile(UserProfile profile);

  void updatePassword(String email, String newRawPassword);

  void deleteUser(String email);

  void setEnabled(String email, boolean enabled);

  boolean userExists(String email);

  void setEmailVerified(String email, boolean verified);
}
