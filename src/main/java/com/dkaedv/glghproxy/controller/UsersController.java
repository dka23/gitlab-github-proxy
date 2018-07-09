package com.dkaedv.glghproxy.controller;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dkaedv.glghproxy.Utils;
import com.dkaedv.glghproxy.converter.GitlabToGithubConverter;
import com.dkaedv.glghproxy.gitlabclient.GitlabSessionProvider;

@Controller
@RequestMapping("/api/v3/users")
public class UsersController {

	@Autowired
	private GitlabSessionProvider gitlab;

	@Value("${treatOrgaAsOwner}")
	private Boolean treatOrgaAsOwner;
	
	@RequestMapping("/{username}/repos")
	@ResponseBody
	public List<Repository> getReposForUser(@PathVariable String username, @RequestParam String per_page,
			@RequestParam String page, @RequestHeader("Authorization") String authorization) throws IOException {

		GitlabAPI api = gitlab.connect(authorization);
		List<GitlabProject> projects = OwnerFixup.getRepositories(api);

		return GitlabToGithubConverter.convertRepositories(projects, treatOrgaAsOwner);
	}

	@RequestMapping("/{username}")
	public ResponseEntity<User> getUser(@PathVariable String username,
			@RequestHeader("Authorization") String authorization) throws IOException {

		GitlabAPI api = gitlab.connect(authorization);
		List<GitlabUser> users = api.findUsers(username);
		GitlabUser user = Utils.findSingleUser(users, username);

		if (user != null) {
			return new ResponseEntity<User>(GitlabToGithubConverter.convertUser(user), HttpStatus.OK);
		} else {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
	}

}
