package com.dkaedv.glghproxy.controller;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabCommitDiff;
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
		
		return GitlabToGithubConverter.convertRepositories(projects);
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
	
	@RequestMapping("/repos/{namespace}/{repo}/branches")
	@ResponseBody
	public List<RepositoryBranch> getBranches(
			@PathVariable String namespace,
			@PathVariable String repo,
			@RequestParam String per_page,
			@RequestParam String page,
			@RequestHeader("Authorization") String authorization
			) throws IOException {
		
		GitlabAPI api = gitlab.connect(authorization);
		List<GitlabBranch> glbranches = api.getBranches(namespace + "/" + repo);
		
		return GitlabToGithubConverter.convertBranches(glbranches);		
	}
	
	@RequestMapping("/repos/{namespace}/{repo}/commits/{sha}")
	@ResponseBody
	public RepositoryCommit getCommit(
			@PathVariable String namespace,
			@PathVariable String repo,
			@PathVariable String sha,
			@RequestHeader("Authorization") String authorization
			) throws IOException {
		
		GitlabAPI api = gitlab.connect(authorization);
		GitlabCommit glcommit = api.getCommit(namespace + "/" + repo, sha);
		List<GitlabCommitDiff> gldiffs = api.getCommitDiffs(namespace + "/" + repo, sha);
		
		return GitlabToGithubConverter.convertCommit(glcommit, gldiffs);
	}
}
