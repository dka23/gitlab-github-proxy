package com.dkaedv.glghproxy.converter;

import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.User;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabCommitDiff;
import org.gitlab.api.models.GitlabProject;

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

		List<Commit> parents = new Vector<Commit>();
		for (String parentSha : glcommit.getParentIds()) {
			Commit parent = new Commit();
			parent.setSha(parentSha);
			parents.add(parent);
		}
		repoCommit.setParents(parents);

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
}
