package com.dkaedv.glghproxy.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dkaedv.glghproxy.converter.GitlabToGithubConverter;
import com.dkaedv.glghproxy.gitlabclient.GitlabSessionProvider;

@Controller
@RequestMapping("/api/v3/orgs")
public class OrgsController {

	private final static Log LOG = LogFactory.getLog(OrgsController.class);

	@Value("${treatOrgaAsOwner}")
	private Boolean treatOrgaAsOwner;

	@Autowired
	private GitlabSessionProvider gitlab;

	@RequestMapping("/{orgname}/repos")
	@ResponseBody
	public List<Repository> getReposForOrg(@PathVariable String orgname, @RequestParam String per_page,
			@RequestParam String page, @RequestHeader("Authorization") String authorization) throws IOException {

		LOG.info("Received request: orgname="
				+ orgname
				+ ", per_page="
				+ per_page
				+ ", page="
				+ page);

		GitlabAPI api = gitlab.connect(authorization);
		List<GitlabProject> projects = OwnerFixup.getRepositories(api);
		LOG.info("GitlabAPI returned projects: " + projects.size());

		List<Repository> repositories = GitlabToGithubConverter.convertRepositories(projects, treatOrgaAsOwner);
		if (Boolean.TRUE.equals(treatOrgaAsOwner)) {
			setRepositoryOwner(repositories, orgname);
		}
		return repositories;
	}

	/**
	 * Sets the given ownerName as the owner of all repositories. The original "owner"
	 * (= GitLab namespace) is encoded into the repository name.
	 * 
	 * @param repositories
	 * @param ownerName
	 */
	private void setRepositoryOwner(List<Repository> repositories, String ownerName) {
		for (Repository repository : repositories) {
			User owner = repository.getOwner();
			if (owner == null) {
				owner = new User();
			}
			String tempOldLogin = owner.getLogin();
			if (tempOldLogin == null || tempOldLogin.length() == 0) {
				LOG.warn("No owner/login available for repository " + repository.getName() + "; synchronization may not work.");
			}
			owner.setLogin(ownerName);
			repository.setOwner(owner);
			String extendedRepoName = OwnerFixup.encode(tempOldLogin, repository.getName());
			LOG.info("Setting extended repo name: " + extendedRepoName + " for repo: " + repository.getName());
			repository.setName(extendedRepoName);
		}
	}
}
