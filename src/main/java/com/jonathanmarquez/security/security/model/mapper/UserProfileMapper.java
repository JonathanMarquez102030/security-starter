package com.jonathanmarquez.security.security.model.mapper;

import com.jonathanmarquez.security.security.model.UserProfile;
import com.jonathanmarquez.security.security.model.dto.UserProfileDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class UserProfileMapper {

  public abstract UserProfileDto toUserProfileDto(UserProfile userProfile);
}
