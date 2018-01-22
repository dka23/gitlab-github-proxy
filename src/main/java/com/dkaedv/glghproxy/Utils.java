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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gitlab.api.models.GitlabUser;

/**
 * A place for some helper methods.
 */
public class Utils {

	private final static Log LOG = LogFactory.getLog(Utils.class);

	private Utils() {}

	/**
	 * Returns the first user of the given list matching the givern email or username.
	 * GitlabAPI.findUsers() appears to return inexact matches, so we perform an exact match here.
	 * @param users
	 * @param emailOrUsername
	 * @return the found user, or <code>null</code>
	 */
	public static GitlabUser findSingleUser(List<GitlabUser> users, String emailOrUsername) {
		if (users == null || users.isEmpty()) {
			LOG.info("Cannot find user " + emailOrUsername + " in null/empty list");
			return null;
		}

		if (users.size() == 1) {
			return users.get(0);
		}

//		users.stream().forEach(user -> LOG.warn("check: " + user.getName() + " : " + user.getEmail() + " : " + user.getUsername()));
		
		Optional<GitlabUser> result = null;
		if (emailOrUsername.contains("@")) {
			result = users.stream().filter(user -> emailOrUsername.equalsIgnoreCase(user.getEmail()) || emailOrUsername.equalsIgnoreCase(user.getUsername())).findFirst();
		} else {
			result = users.stream().filter(user -> emailOrUsername.equalsIgnoreCase(user.getUsername())).findFirst();
		}
//		LOG.warn("findSingleUser: " + emailOrUsername + " in " + users.size() + " users. Found " + (result.isPresent() ? result.get().getUsername() : "none"));

		if (result.isPresent()) {
			return result.get();
		}
//		LOG.warn("input user: " + users.get(0).getUsername() + " : " + users.get(0).getEmail());
		return null;
	}
}
