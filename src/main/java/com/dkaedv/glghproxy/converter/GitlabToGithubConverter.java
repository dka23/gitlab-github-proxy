package com.dkaedv.glghproxy.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.PullRequestPayload;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabCommitDiff;
import org.gitlab.api.models.GitlabMergeRequest;
import org.gitlab.api.models.GitlabMilestone;
import org.gitlab.api.models.GitlabNote;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabProjectHook;
import org.gitlab.api.models.GitlabUser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitlabToGithubConverter {
	public static RepositoryBranch convertBranch(GitlabBranch glbranch) {
		RepositoryBranch branch = new RepositoryBranch();
		branch.setName(glbranch.getName());

		TypedResource commit = new TypedResource();
		commit.setType(TypedResource.TYPE_COMMIT);
		commit.setSha(glbranch.getCommit().getId());

		branch.setCommit(commit);
		return branch;
	}

	public static List<RepositoryBranch> convertBranches(List<GitlabBranch> glbranches) {
		List<RepositoryBranch> branches = new ArrayList<>(glbranches.size());

		for (GitlabBranch glbranch : glbranches) {
			RepositoryBranch branch = convertBranch(glbranch);
			branches.add(branch);
		}
		return branches;
	}

	public static RepositoryCommit convertCommit(GitlabCommit glcommit, List<GitlabCommitDiff> gldiffs, GitlabUser gluser) {
		RepositoryCommit repoCommit = new RepositoryCommit();

		repoCommit.setSha(glcommit.getId());

		Commit commit = new Commit();
		commit.setMessage(glcommit.getTitle());
		
		CommitUser commitUser = new CommitUser();
		commitUser.setName(glcommit.getAuthorName());
		commitUser.setEmail(glcommit.getAuthorEmail());
		commitUser.setDate(glcommit.getCreatedAt());
		commit.setAuthor(commitUser);
		commit.setCommitter(commitUser);

		repoCommit.setCommit(commit);

		User user = new User();
		user.setEmail(glcommit.getAuthorEmail());
		user.setLogin(gluser != null ? gluser.getUsername() : null);
		repoCommit.setAuthor(user);
		repoCommit.setCommitter(user);

		List<String> parentIds = glcommit.getParentIds();
		if (parentIds != null) {
			List<Commit> parents = new ArrayList<>(parentIds.size());
			for (String parentSha : parentIds) {
				Commit parent = new Commit();
				parent.setSha(parentSha);
				parents.add(parent);
			}
			repoCommit.setParents(parents);
		}
		
		if (gldiffs != null) {
			List<CommitFile> files = new ArrayList<>(gldiffs.size());
			for (GitlabCommitDiff diff : gldiffs) {
				convertCommitFile(files, diff);
			}
			repoCommit.setFiles(files);
		}
		
		return repoCommit;
	}

	private static void convertCommitFile(List<CommitFile> files, GitlabCommitDiff diff) {
		int additions = StringUtils.countMatches(diff.getDiff(), "\n+") - StringUtils.countMatches(diff.getDiff(), "\n+++");
		int deletions = StringUtils.countMatches(diff.getDiff(), "\n-") - StringUtils.countMatches(diff.getDiff(), "\n---");

		if (diff.getNewFile()) {
			CommitFile file = new CommitFile();
			file.setStatus("added");
			file.setFilename(diff.getNewPath());
			file.setAdditions(additions);
			file.setChanges(additions);
			files.add(file);
		} else if (diff.getDeletedFile()) {
			CommitFile file = new CommitFile();
			file.setStatus("removed");
			file.setFilename(diff.getOldPath());
			file.setDeletions(deletions);
			file.setChanges(deletions);
			files.add(file);
		} else if (diff.getRenamedFile()) {
			CommitFile oldFile = new CommitFile();
			oldFile.setStatus("removed");
			oldFile.setFilename(diff.getOldPath());
			oldFile.setDeletions(deletions);
			oldFile.setChanges(deletions);
			files.add(oldFile);

			CommitFile newFile = new CommitFile();
			newFile.setStatus("added");
			newFile.setFilename(diff.getNewPath());
			newFile.setDeletions(additions);
			newFile.setChanges(additions);
			files.add(newFile);
		} else {
			CommitFile file = new CommitFile();
			file.setStatus("modified");
			file.setFilename(diff.getNewPath());
			file.setAdditions(additions);
			file.setDeletions(deletions);
			file.setChanges(additions + deletions);
			files.add(file);
		}				
	}
	
	public static Repository convertRepository(GitlabProject project) {
		Repository repo = new Repository();
		
		repo.setId(project.getId());
		repo.setName(project.getName());
		
		User user = new User();
		user.setLogin(project.getNamespace().getName());
		repo.setOwner(user);
		
		return repo;
	}

	public static List<Repository> convertRepositories(List<GitlabProject> projects) {
		List<Repository> repos = new ArrayList<>(projects.size());
		
		for (GitlabProject project : projects) {
			repos.add(convertRepository(project));
		}
		
		return repos;
	}

	public static List<PullRequest> convertMergeRequests(List<GitlabMergeRequest> glmergerequests, String gitlabUrl, String namespace, String repo) {
		List<PullRequest> pulls = new ArrayList<>(glmergerequests.size());
		
		for (GitlabMergeRequest glmr : glmergerequests) {
			pulls.add(convertMergeRequest(glmr, gitlabUrl, namespace, repo));
		}
		
		return pulls;
	}

	public static PullRequest convertMergeRequest(GitlabMergeRequest glmr, String gitlabUrl, String namespace, String repo) {
		PullRequest pull = new PullRequest();
		
		pull.setAssignee(convertUser(glmr.getAssignee()));
		pull.setUser(convertUser(glmr.getAuthor()));
		pull.setCreatedAt(glmr.getCreatedAt());
		pull.setBody(glmr.getDescription());
		pull.setId(glmr.getId());
		pull.setMilestone(convertMilestone(glmr.getMilestone()));
		pull.setNumber(glmr.getIid());
		pull.setHead(createPullRequestMarker(glmr.getSourceBranch(), namespace, repo));
		pull.setBase(createPullRequestMarker(glmr.getTargetBranch(), namespace, repo));
		convertMergeRequestState(pull, glmr);
		pull.setTitle(glmr.getTitle());
		
		if (glmr.getUpdatedAt() != null) {
			pull.setUpdatedAt(glmr.getUpdatedAt());
		} else {
			pull.setUpdatedAt(glmr.getCreatedAt());
		}
		
		pull.setHtmlUrl(gitlabUrl + "/" + namespace + "/" + repo + "/merge_requests/" + glmr.getIid());
		
		//LOG.info("Converted merge request " + convertToJson(glmr) + " to pull request " + convertToJson(pull));
		
		return pull;
	}

	private static void convertMergeRequestState(PullRequest pull, GitlabMergeRequest glmr) {
		if ("opened".equals(glmr.getState()) || "reopened".equals(glmr.getState())) {
			pull.setState("open");
			pull.setMerged(false);
		} else if ("closed".equals(glmr.getState())) {
			pull.setState("closed");
			pull.setMerged(false);
			pull.setClosedAt(glmr.getUpdatedAt());
		} else if ("merged".equals(glmr.getState())) {
			pull.setState("closed");
			pull.setMerged(true);
			pull.setClosedAt(glmr.getUpdatedAt());
			pull.setMergedAt(glmr.getUpdatedAt());
			
			if (glmr.getAssignee() != null) {
				pull.setMergedBy(convertUser(glmr.getAssignee()));
			} else {
				pull.setMergedBy(convertUser(glmr.getAuthor()));
			}
		} else {
			throw new RuntimeException("Unknown MR state: " + glmr.getState());
		}
	}

	private static PullRequestMarker createPullRequestMarker(String branch, String namespace, String reponame) {
		PullRequestMarker marker = new PullRequestMarker();
		marker.setLabel(branch);
		marker.setRef(branch);
		
		
		Repository repo = new Repository();
		repo.setName(reponame);
		User owner = new User();
		owner.setLogin(namespace);
		repo.setOwner(owner);
		
		marker.setRepo(repo);
		
		return marker;
	}

	private static Milestone convertMilestone(GitlabMilestone glmilestone) {
		if (glmilestone == null) {
			return null;
		}
		
		Milestone milestone = new Milestone();
		
		milestone.setCreatedAt(glmilestone.getCreatedDate());
		milestone.setDescription(glmilestone.getDescription());
		milestone.setDueOn(glmilestone.getDueDate());
		milestone.setState(glmilestone.getState());
		milestone.setTitle(glmilestone.getTitle());
		
		return milestone;
	}

	public static User convertUser(GitlabUser gluser) {
		if (gluser == null) {
			return null;
		}
		
		User user = new User();
		user.setId(gluser.getId());
		user.setLogin(gluser.getUsername());
		user.setAvatarUrl(gluser.getAvatarUrl());
		user.setBio(gluser.getBio());
		user.setEmail(gluser.getEmail());
		user.setName(gluser.getName());
		user.setCreatedAt(gluser.getCreatedAt());
		user.setType(User.TYPE_USER);
		
		return user;
	}

	public static List<RepositoryCommit> convertCommits(List<GitlabCommit> glcommits) {
		List<RepositoryCommit> commits = new ArrayList<>(glcommits.size());
		
		for (GitlabCommit glcommit : glcommits) {
			commits.add(convertCommit(glcommit, null, null));
		}
		
		return commits;
	}

	public static List<Comment> convertComments(List<GitlabNote> glnotes) {
		List<Comment> comments = new ArrayList<>(glnotes.size());
		
		for (GitlabNote glnote : glnotes) {
			comments.add(convertComment(glnote));
		}
		
		return comments;
	}

	private static Comment convertComment(GitlabNote glnote) {
		Comment comment = new Comment();
		
		comment.setUser(convertUser(glnote.getAuthor()));
		comment.setBody(glnote.getBody());
		comment.setCreatedAt(glnote.getCreatedAt());
		comment.setId(glnote.getId());
		
		return comment;
	}

	public static List<RepositoryHook> convertHooks(List<GitlabProjectHook> glhooks) {
		List<RepositoryHook> hooks = new ArrayList<>(glhooks.size());
		
		for (GitlabProjectHook glhook : glhooks) {
			hooks.add(convertHook(glhook));
		}
		
		return hooks;
	}

	public static RepositoryHook convertHook(GitlabProjectHook glhook) {
		RepositoryHook hook = new RepositoryHook();
		
		hook.setCreatedAt(glhook.getCreatedAt());
		hook.setName("web");	// Always "web" for webhooks ...
		hook.setUrl(glhook.getUrl());
		hook.setActive(glhook.getPushEvents() || glhook.isMergeRequestsEvents());
		hook.setId(Integer.valueOf(glhook.getId()));
		
		hook.setConfig(new HashMap<String, String>());
		hook.getConfig().put("url", glhook.getUrl());
		
		return hook;
	}
	
	private static String convertToJson(Object o) {
		try {
			return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<Event> convertMergeRequestsToEvents(List<GitlabMergeRequest> glmergerequests, String gitlabUrl, String namespace, String repo) {
		List<Event> events = new ArrayList<>(glmergerequests.size());
		
		for (GitlabMergeRequest glmergerequest : glmergerequests) {
			events.add(convertMergeRequestToEvent(glmergerequest, gitlabUrl, namespace, repo));
		}
		
		return events;
	}

	private static Event convertMergeRequestToEvent(GitlabMergeRequest glmergerequest, String gitlabUrl, String namespace, String repo) {
		Event event = new Event();
		
		event.setType(Event.TYPE_PULL_REQUEST);
		event.setCreatedAt(glmergerequest.getUpdatedAt());
		
		PullRequestPayload payload = new PullRequestPayload();
		payload.setPullRequest(convertMergeRequest(glmergerequest, gitlabUrl, namespace, repo));
		payload.setNumber(payload.getPullRequest().getNumber());
		
		event.setPayload(payload);
		
		event.setId(glmergerequest.getId() + "-" + glmergerequest.getUpdatedAt().getTime());
				
		return event;
	}
}
