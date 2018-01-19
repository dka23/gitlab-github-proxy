//
// Utils.java
//
// Copyright (C) 2018
// GEBIT Solutions GmbH, 
// Berlin, Duesseldorf, Stuttgart (Germany)
// All rights reserved.
//
package com.dkaedv.glghproxy;

import java.util.List;
import java.util.Optional;

import org.gitlab.api.models.GitlabUser;

/**
 * A place for some helper methods.
 */
public class Utils {

	private Utils() {}

	/**
	 * Returns the first user of the given list matching the givern email or username.
	 * GitlabAPI.findUsers() appears to return inexact matches, so we perform an exact match here.
	 * @param users
	 * @param emailOrUsername
	 * @return the found user, or <code>null</code>
	 */
	public static GitlabUser findSingleUser(List<GitlabUser> users, String emailOrUsername) {
		if (users == null) {
			return null;
		}
		
		Optional<GitlabUser> result = null;
		if (emailOrUsername.contains("@")) {
			result = users.stream().filter(user -> emailOrUsername.equalsIgnoreCase(user.getEmail()) || emailOrUsername.equalsIgnoreCase(user.getName())).findFirst();
		} else {
			result = users.stream().filter(user -> emailOrUsername.equalsIgnoreCase(user.getName())).findFirst();
		}
		if (result.isPresent()) {
			result.get();
		}
		return null;
	}
}
