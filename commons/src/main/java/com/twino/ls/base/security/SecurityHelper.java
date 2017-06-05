package com.twino.ls.base.security;

import com.google.common.base.Preconditions;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SecurityHelper {

	public static final String UNKNOWN_USER = "unknown";

	public static boolean setUser(String user) {
		Preconditions.checkArgument(isNotBlank(user), "User required");
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, user);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			return true;
		} else {
			throw new IllegalStateException("User already authenticated, remove user first");
		}
	}

	public static String getCurrentUserName() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return UNKNOWN_USER;
		}
		return authentication.getName();
	}

	public static void removeUser() {
		SecurityContextHolder.clearContext();
	}

}
