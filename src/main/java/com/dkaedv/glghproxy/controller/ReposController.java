package com.dkaedv.glghproxy.controller;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dkaedv.glghproxy.githubentity.Repository;
import com.dkaedv.glghproxy.githubentity.User;
import com.dkaedv.glghproxy.gitlabclient.GitlabSessionProvider;

@Controller
@RequestMapping("/api/v3")
public class ReposController {
	private static final Log LOG = LogFactory.getLog(ReposController.class);

	@Autowired
	private GitlabSessionProvider gitlab;
	
	@RequestMapping("/orgs/{orgname}/repos")
	@ResponseBody
	public List<Repository> getReposForOrg(
			@PathVariable String orgname,
			@RequestParam String per_page,
			@RequestParam String page,
			@RequestHeader("Authorization") String authorization) throws IOException {
		
		LOG.info("Received request: orgname=" + orgname + ", per_page=" + per_page + ", page=" + page + ", authorization=" + authorization);

		return new Vector<Repository>();
	}

	@RequestMapping("/users/{username}/repos")
	@ResponseBody
	public List<Repository> getReposForUser(
			@PathVariable String username,
			@RequestParam String per_page,
			@RequestParam String page,
			@RequestHeader("Authorization") String authorization) throws IOException {

		LOG.info("Received request: username=" + username + ", per_page=" + per_page + ", page=" + page);

		GitlabAPI api = gitlab.connect(authorization);
		List<GitlabProject> projects = api.getAllProjects();
		List<Repository> repos = new Vector<Repository>();
		
		for (GitlabProject p : projects) {
			Repository repo = new Repository(p.getId(), p.getName(), p.getPathWithNamespace());
			
			// JIRA does only display repositories that have an owner with the same name as the name in JIRA
			// This behaviour seems to be correlating best with the namespace concept in Gitlab
			repo.setOwner(new User(p.getNamespace().getName()));
			repos.add(repo);
		}
		
		return repos;
	}

	@RequestMapping("/user/repos")
	@ResponseBody
	public List<Repository> getReposForCurrentUser(
			@RequestParam String per_page,
			@RequestParam String page,
			@RequestHeader("Authorization") String authorization) throws IOException {

		LOG.info("Received request: per_page=" + per_page + ", page=" + page);

		return new Vector<Repository>();
	}
}
