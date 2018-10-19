package com.dkaedv.glghproxy.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.event.Event;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabCommitDiff;
import org.gitlab.api.models.GitlabMergeRequest;
import org.gitlab.api.models.GitlabNote;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabProjectHook;
import org.gitlab.api.models.GitlabUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.dkaedv.glghproxy.Constants;
import com.dkaedv.glghproxy.Utils;
import com.dkaedv.glghproxy.converter.GitlabToGithubConverter;
import com.dkaedv.glghproxy.githubentity.HookRequest;
import com.dkaedv.glghproxy.gitlabclient.GitlabSessionProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/api/v3/repos")
public class ReposController {

	private static final Log LOG = LogFactory.getLog(ReposController.class);

	@Autowired
	private GitlabSessionProvider gitlab;

	@Value("${gitlabUrl}")
	private String gitlabUrl;

	@Value("${treatOrgaAsOwner}")
	private Boolean treatOrgaAsOwner;

	@Autowired
	ObjectMapper objectMapper;

	@RequestMapping("/{namespace}/{repo}/branches")
	@ResponseBody
	public List<RepositoryBranch> getBranches(@PathVariable String namespace, @PathVariable String repo,
			@RequestParam String per_page, @RequestParam String page,
			@RequestHeader("Authorization") String authorization) throws IOException {

		String dummy = "x";
		LOG.warn("dummy: " + dummy + "; namespace: " + namespace + "; repo: " + repo);

		namespace = OwnerFixup.fixupNamespace(repo, treatOrgaAsOwner);
		repo = OwnerFixup.fixupRepo(repo, treatOrgaAsOwner);
		GitlabAPI api = gitlab.connect(authorization);

		LOG.warn("2 dummy: " + dummy + "; namespace: " + namespace + "; repo: " + repo);

		List<GitlabBranch> glbranches = api.getBranches(namespace + "/" + repo);

		return GitlabToGithubConverter.convertBranches(glbranches);
	}

	@RequestMapping("/{namespace}/{repo}/commits/{sha}")
	@ResponseBody
	public RepositoryCommit getCommit(@PathVariable String namespace, @PathVariable String repo,
			@PathVariable String sha, @RequestHeader("Authorization") String authorization) throws IOException {

		namespace = OwnerFixup.fixupNamespace(repo, treatOrgaAsOwner);
		repo = OwnerFixup.fixupRepo(repo, treatOrgaAsOwner);
		GitlabAPI api = gitlab.connect(authorization);
		GitlabCommit glcommit = api.getCommit(namespace + "/" + repo, sha);
		List<GitlabCommitDiff> gldiffs = api.getCommitDiffs(namespace + "/" + repo, sha);
		GitlabUser user = null;
		try {
			List<GitlabUser> users = api.findUsers(glcommit.getAuthorEmail());
			user = Utils.findSingleUser(users, glcommit.getAuthorEmail());
		} catch (IOException ex) {
			LOG.warn("Unable to find gitlab user based on email: " + glcommit.getAuthorEmail() + " in repository: " + namespace + "/" + repo);
		}

		return GitlabToGithubConverter.convertCommit(glcommit, gldiffs, user);
	}

	@RequestMapping("/{namespace}/{repo}/events")
	@ResponseBody
	public List<Event> getEvents(@PathVariable String namespace, @PathVariable String repo,
			@RequestHeader("Authorization") String authorization) throws IOException {

		namespace = OwnerFixup.fixupNamespace(repo, treatOrgaAsOwner);
		repo = OwnerFixup.fixupRepo(repo, treatOrgaAsOwner);
		GitlabAPI api = gitlab.connect(authorization);
		List<GitlabMergeRequest> glmergerequests = api.getMergeRequests(namespace + "/" + repo);
		Map<Integer,GitlabUser> userCache = new HashMap<Integer, GitlabUser>();

		return GitlabToGithubConverter.convertMergeRequestsToEvents(glmergerequests, gitlabUrl, namespace, repo);
	}

	@RequestMapping("/{namespace}/{repo}/pulls")
	@ResponseBody
	public List<PullRequest> getPulls(@PathVariable String namespace, @PathVariable String repo,
			@RequestParam String state, @RequestParam String sort, @RequestParam String direction,
			@RequestParam String per_page, @RequestParam String page,
			@RequestHeader("Authorization") String authorization) throws IOException {

		namespace = OwnerFixup.fixupNamespace(repo, treatOrgaAsOwner);
		repo = OwnerFixup.fixupRepo(repo, treatOrgaAsOwner);
		GitlabAPI api = gitlab.connect(authorization);
		List<GitlabMergeRequest> glmergerequests = api.getMergeRequestsWithStatus(namespace + "/" + repo, GitlabToGithubConverter.translatePrStateToMrStatus(state));
		Map<Integer,GitlabUser> userCache = new HashMap<Integer, GitlabUser>();

		List<PullRequest> mergeRequests = GitlabToGithubConverter.convertMergeRequests(glmergerequests,
				gitlabUrl,
				namespace,
				repo);
		// LOG.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mergeRequests));
		return mergeRequests;
	}

	@RequestMapping("/{namespace}/{repo}/pulls/{id}")
	@ResponseBody
	public PullRequest getPull(@PathVariable String namespace, @PathVariable String repo, @PathVariable Integer id,
			@RequestHeader("Authorization") String authorization) throws IOException {

		namespace = OwnerFixup.fixupNamespace(repo, treatOrgaAsOwner);
		repo = OwnerFixup.fixupRepo(repo, treatOrgaAsOwner);
		GitlabAPI api = gitlab.connect(authorization);
		GitlabMergeRequest mergeRequest = findMergeRequestByProjectAndIid(namespace, repo, id, api);

		return GitlabToGithubConverter.convertMergeRequest(mergeRequest, gitlabUrl, namespace, repo);
	}


	@RequestMapping("/{namespace}/{repo}/pulls/{id}/commits")
	@ResponseBody
	public List<RepositoryCommit> getCommitsOnPullRequest(@PathVariable String namespace, @PathVariable String repo,
			@PathVariable Integer id, @RequestHeader("Authorization") String authorization) throws IOException {

		namespace = OwnerFixup.fixupNamespace(repo, treatOrgaAsOwner);
		repo = OwnerFixup.fixupRepo(repo, treatOrgaAsOwner);
		GitlabAPI api = gitlab.connect(authorization);
		GitlabMergeRequest mergeRequest = findMergeRequestByProjectAndIid(namespace, repo, id, api);
		List<GitlabCommit> commits = api.getCommits(mergeRequest);

		return GitlabToGithubConverter.convertCommits(commits);
	}

	private GitlabMergeRequest findMergeRequestByProjectAndIid(String namespace, String repo, Integer id, GitlabAPI api)
			throws IOException {
		List<GitlabMergeRequest> mergeRequests = api.getMergeRequests(namespace + "/" + repo);
		for (GitlabMergeRequest mergeRequest : mergeRequests) {
			if (mergeRequest.getIid().equals(id)) {
				return mergeRequest;
			}
		}

		return null;
	}

	/**
	 * In Github, each Merge Request is automatically also an issue. Therefore we return its comments here.
	 */
	@RequestMapping("/{namespace}/{repo}/issues/{id}/comments")
	@ResponseBody
	public List<Comment> getCommentsOnPullRequest(@PathVariable String namespace, @PathVariable String repo,
			@PathVariable Integer id, @RequestHeader("Authorization") String authorization) throws IOException {

		namespace = OwnerFixup.fixupNamespace(repo, treatOrgaAsOwner);
		repo = OwnerFixup.fixupRepo(repo, treatOrgaAsOwner);
		GitlabAPI api = gitlab.connect(authorization);
		GitlabMergeRequest mergeRequest = findMergeRequestByProjectAndIid(namespace, repo, id, api);
		List<GitlabNote> notes = api.getNotes(mergeRequest);

		return GitlabToGithubConverter.convertComments(notes);
	}

	/**
	 * Github additionally has review comments on merge requests (those on the diff). Gitlab has those also, but doesn't
	 * distinguish in its API. Therefore return an empty list here.
	 */
	@RequestMapping("/{namespace}/{repo}/pulls/{id}/comments")
	@ResponseBody
	public List<Comment> getReviewCommentsOnPullRequest(@PathVariable String namespace, @PathVariable String repo,
			@PathVariable Integer id, @RequestHeader("Authorization") String authorization) throws IOException {

		namespace = OwnerFixup.fixupNamespace(repo, treatOrgaAsOwner);
		repo = OwnerFixup.fixupRepo(repo, treatOrgaAsOwner);
		return Collections.emptyList();
	}

	@RequestMapping(value = "/{namespace}/{repo}/hooks", method = RequestMethod.GET)
	@ResponseBody
	public List<RepositoryHook> getHooks(@PathVariable String namespace, @PathVariable String repo,
			@RequestParam("access_token") String authorization) throws IOException {

		namespace = OwnerFixup.fixupNamespace(repo, treatOrgaAsOwner);
		repo = OwnerFixup.fixupRepo(repo, treatOrgaAsOwner);
		GitlabAPI api = gitlab.connect(authorization);
		List<GitlabProjectHook> hooks = api.getProjectHooks(namespace + "/" + repo);

		return GitlabToGithubConverter.convertHooks(hooks);
	}

	@RequestMapping(value = "/{namespace}/{repo}/hooks", method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.CREATED)
	public RepositoryHook addHook(@PathVariable String namespace, @PathVariable String repo,
			@RequestParam("access_token") String authorization, @RequestBody HookRequest hook) throws IOException {

		namespace = OwnerFixup.fixupNamespace(repo, treatOrgaAsOwner);
		repo = OwnerFixup.fixupRepo(repo, treatOrgaAsOwner);
		GitlabAPI api = gitlab.connect(authorization);
		GitlabProjectHook createdHook = api.addProjectHook(namespace + "/" + repo,
				hook.getConfig().get("url"),
				hook.getEvents().contains("push"),
				false, // no issue events
				hook.getEvents().contains("pull_request"),
				false, // no note events
				hook.getEvents().contains("push"), // there's no differentiation for tag-pushes in the github API
				Constants.STRICT_SSL,
				null);

		return GitlabToGithubConverter.convertHook(createdHook);
	}

	@RequestMapping(value = "/{namespace}/{repo}/hooks/{hookId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteHook(@PathVariable String namespace, @PathVariable String repo, @PathVariable String hookId,
			@RequestParam("access_token") String authorization) throws IOException {

		namespace = OwnerFixup.fixupNamespace(repo, treatOrgaAsOwner);
		repo = OwnerFixup.fixupRepo(repo, treatOrgaAsOwner);
		GitlabAPI api = gitlab.connect(authorization);
		GitlabProject project = api.getProject(namespace + "/" + repo);
		api.deleteProjectHook(project, hookId);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void handle(HttpMessageNotReadableException e) {
		LOG.warn("Returning HTTP 400 Bad Request", e);
	}
}
