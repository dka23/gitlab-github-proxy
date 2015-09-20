package com.dkaedv.glghproxy.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.egit.github.core.Repository;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dkaedv.glghproxy.converter.GitlabToGithubConverter;
import com.dkaedv.glghproxy.gitlabclient.GitlabSessionProvider;

@Controller
@RequestMapping("/api/v3/users")
public class UsersController {
	private final static Log LOG = LogFactory.getLog(UsersController.class);

	@Autowired
	private GitlabSessionProvider gitlab;

	@RequestMapping("/{username}/repos")
	@ResponseBody
	public List<Repository> getReposForUser(
			@PathVariable String username,
			@RequestParam String per_page,
			@RequestParam String page,
			@RequestHeader("Authorization") String authorization) throws IOException {

		LOG.info("Received request: username=" + username + ", per_page=" + per_page + ", page=" + page);

		GitlabAPI api = gitlab.connect(authorization);
		List<GitlabProject> projects = api.getAllProjects();
		
		return GitlabToGithubConverter.convertRepositories(projects);
	}

}
