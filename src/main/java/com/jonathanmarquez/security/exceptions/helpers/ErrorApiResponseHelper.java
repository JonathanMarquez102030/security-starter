package com.jonathanmarquez.security.exceptions.helpers;

import com.jonathanmarquez.security.security.enums.SpringProfile;
import com.jonathanmarquez.security.utils.ProfileDetector;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ErrorApiResponseHelper {

  public static String buildDetails(ProfileDetector profileDetector, Exception ex, String additionalContext) {
    if (profileDetector.isProfileActive(SpringProfile.PROD.getProfileName())) {
      return null;
    }

    StringBuilder details = new StringBuilder();

    if (additionalContext != null) {
      details.append(additionalContext);
    } else if (ex.getMessage() != null) {
      details.append(ex.getMessage());
    }

    if (details.isEmpty()) {
      details.append(ex.getClass().getSimpleName());
    }

    return details.toString();
  }
}