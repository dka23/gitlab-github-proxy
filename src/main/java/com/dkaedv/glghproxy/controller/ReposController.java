package com.dkaedv.glghproxy.controller;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.event.Event;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabCommitDiff;
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
@RequestMapping("/api/v3/repos")
public class ReposController {
	private static final Log LOG = LogFactory.getLog(ReposController.class);

	@Autowired
	private GitlabSessionProvider gitlab;
	
	@RequestMapping("/{namespace}/{repo}/branches")
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
	
	@RequestMapping("/{namespace}/{repo}/commits/{sha}")
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
	
	@RequestMapping("/{namespace}/{repo}/events")
	@ResponseBody
	public List<Event> getEvents(
			@PathVariable String namespace,
			@PathVariable String repo,
			@RequestHeader("Authorization") String authorization
			) {
				
		return new Vector<Event>();
	}
			
}
