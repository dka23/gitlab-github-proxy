package com.dkaedv.glghproxy.converter;

import java.util.List;
import java.util.Vector;

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
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.User;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabCommitDiff;
import org.gitlab.api.models.GitlabMergeRequest;
import org.gitlab.api.models.GitlabMilestone;
import org.gitlab.api.models.GitlabNote;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabUser;

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
		List<RepositoryBranch> branches = new Vector<RepositoryBranch>();

		for (GitlabBranch glbranch : glbranches) {
			RepositoryBranch branch = convertBranch(glbranch);
			branches.add(branch);
		}
		return branches;
	}

	public static RepositoryCommit convertCommit(GitlabCommit glcommit, List<GitlabCommitDiff> gldiffs) {
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
		user.setLogin(glcommit.getAuthorName());
		repoCommit.setAuthor(user);
		repoCommit.setCommitter(user);

		if (glcommit.getParentIds() != null) {
			List<Commit> parents = new Vector<Commit>();
			for (String parentSha : glcommit.getParentIds()) {
				Commit parent = new Commit();
				parent.setSha(parentSha);
				parents.add(parent);
			}
			repoCommit.setParents(parents);
		}
		
		if (gldiffs != null) {
			List<CommitFile> files = new Vector<CommitFile>();
			for (GitlabCommitDiff diff : gldiffs) {
				CommitFile file = new CommitFile();
				file.setFilename(diff.getNewPath());
	
				int additions = StringUtils.countMatches(diff.getDiff(), "\n+") - StringUtils.countMatches(diff.getDiff(), "\n+++");
				int deletions = StringUtils.countMatches(diff.getDiff(), "\n-") - StringUtils.countMatches(diff.getDiff(), "\n---");
				
				file.setAdditions(additions);
				file.setDeletions(deletions);
				
				files.add(file);
			}
			repoCommit.setFiles(files);
		}
		
		return repoCommit;
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
		List<Repository> repos = new Vector<Repository>();
		
		for (GitlabProject project : projects) {
			repos.add(convertRepository(project));
		}
		
		return repos;
	}

	public static List<PullRequest> convertMergeRequests(List<GitlabMergeRequest> glmergerequests) {
		List<PullRequest> pulls = new Vector<PullRequest>();
		
		for (GitlabMergeRequest glmr : glmergerequests) {
			pulls.add(convertMergeRequest(glmr));
		}
		
		return pulls;
	}

	private static PullRequest convertMergeRequest(GitlabMergeRequest glmr) {
		PullRequest pull = new PullRequest();
		
		pull.setAssignee(convertUser(glmr.getAssignee()));
		pull.setUser(convertUser(glmr.getAuthor()));
		pull.setCreatedAt(glmr.getCreatedAt());
		pull.setBody(glmr.getDescription());
		pull.setId(glmr.getId());
		pull.setMilestone(convertMilestone(glmr.getMilestone()));
		pull.setNumber(glmr.getIid());
		pull.setHead(createPullRequestMarker(glmr.getSourceBranch()));
		pull.setBase(createPullRequestMarker(glmr.getTargetBranch()));
		convertMergeRequestState(pull, glmr.getState());
		pull.setTitle(glmr.getTitle());
		
		if (glmr.getUpdatedAt() != null) {
			pull.setUpdatedAt(glmr.getUpdatedAt());
		} else {
			pull.setUpdatedAt(glmr.getCreatedAt());
		}
		
		return pull;
	}

	private static void convertMergeRequestState(PullRequest pull, String state) {
		if ("opened".equals(state)) {
			pull.setState("open");
			pull.setMerged(false);
		} else if ("closed".equals(state)) {
			pull.setState("closed");
			pull.setMerged(false);
		} else if ("merged".equals(state)) {
			pull.setState("closed");
			pull.setMerged(true);
		} else {
			throw new RuntimeException("Unknown MR state: " + state);
		}
	}

	private static PullRequestMarker createPullRequestMarker(String branch) {
		PullRequestMarker marker = new PullRequestMarker();
		marker.setLabel(branch);
		marker.setRef(branch);
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

	private static User convertUser(GitlabUser gluser) {
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
		
		return user;
	}

	public static List<RepositoryCommit> convertCommits(List<GitlabCommit> glcommits) {
		List<RepositoryCommit> commits = new Vector<RepositoryCommit>();
		
		for (GitlabCommit glcommit : glcommits) {
			commits.add(convertCommit(glcommit, null));
		}
		
		return commits;
	}

	public static List<Comment> convertComments(List<GitlabNote> glnotes) {
		List<Comment> comments = new Vector<Comment>();
		
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
}
