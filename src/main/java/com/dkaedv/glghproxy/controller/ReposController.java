package com.dkaedv.glghproxy.controller;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.event.Event;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabCommitDiff;
import org.gitlab.api.models.GitlabMergeRequest;
import org.gitlab.api.models.GitlabNote;
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

	@RequestMapping("/{namespace}/{repo}/pulls")
	@ResponseBody
	public List<PullRequest> getPulls(
			@PathVariable String namespace,
			@PathVariable String repo,
			@RequestParam String state,
			@RequestParam String sort,
			@RequestParam String direction,
			@RequestParam String per_page,
			@RequestParam String page,
			@RequestHeader("Authorization") String authorization
			) throws IOException {

		GitlabAPI api = gitlab.connect(authorization);
		List<GitlabMergeRequest> glmergerequests = api.getMergeRequests(namespace + "/" + repo);
		
		return GitlabToGithubConverter.convertMergeRequests(glmergerequests);
	}
	
	
	@RequestMapping("/{namespace}/{repo}/pulls/{id}/commits")
	@ResponseBody
	public List<RepositoryCommit> getCommitsOnPullRequest(
			@PathVariable String namespace,
			@PathVariable String repo,
			@PathVariable Integer id,
			@RequestHeader("Authorization") String authorization
			) throws IOException {

		GitlabAPI api = gitlab.connect(authorization);
		GitlabProject project = api.getProject(namespace + "/" + repo);
		GitlabMergeRequest mergeRequest = api.getMergeRequest(project, id);
		List<GitlabCommit> commits = api.getCommits(mergeRequest);
		
		return GitlabToGithubConverter.convertCommits(commits);
	}

	/**
	 * In Github, each Merge Request is automatically also an issue. Therefore we return its comments here. 
	 */
	@RequestMapping("/{namespace}/{repo}/issues/{id}/comments")
	@ResponseBody
	public List<Comment> getCommentsOnPullRequest(
			@PathVariable String namespace,
			@PathVariable String repo,
			@PathVariable Integer id,
			@RequestHeader("Authorization") String authorization
			) throws IOException {

		GitlabAPI api = gitlab.connect(authorization);
		GitlabProject project = api.getProject(namespace + "/" + repo);
		GitlabMergeRequest mergeRequest = api.getMergeRequest(project, id);
		List<GitlabNote> notes = api.getNotes(mergeRequest);
		
		return GitlabToGithubConverter.convertComments(notes);
	}

	/**
	 * Github additionally has review comments on merge requests (those on the diff). Gitlab has those also,
	 * but doesn't distinguish in its API. Therefore return an empty list here.
	 */
	@RequestMapping("/{namespace}/{repo}/pulls/{id}/comments")
	@ResponseBody
	public List<Comment> getReviewCommentsOnPullRequest(
			@PathVariable String namespace,
			@PathVariable String repo,
			@PathVariable Integer id,
			@RequestHeader("Authorization") String authorization
			) throws IOException {

		return new Vector<Comment>();
	}

}
