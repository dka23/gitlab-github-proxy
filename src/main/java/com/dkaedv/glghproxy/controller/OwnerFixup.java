package com.dkaedv.glghproxy.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;

public class OwnerFixup {

	private static final Log LOG = LogFactory.getLog(OwnerFixup.class);

	private static final boolean FIXUP_ENABLED = true;

	public static final String REPO_NAMESPACE_SEPARATOR = ":";

	private OwnerFixup() {}

	public static final String fixupNamespace(String namespaceAndRepo, Boolean treatOrgaAsOwner) {
		if (FIXUP_ENABLED && Boolean.TRUE.equals(treatOrgaAsOwner)) {
			String[] tempSplit = namespaceAndRepo.split(REPO_NAMESPACE_SEPARATOR);
			if (tempSplit.length >= 2) {
				return tempSplit[0]; // return the actual namespace
			}
			LOG.warn("Unable to fix up namespace, does not contain expected separator: " + namespaceAndRepo);
		}
		return namespaceAndRepo;
	}

	public static final String fixupRepo(String namespaceAndRepo, Boolean treatOrgaAsOwner) {
		if (FIXUP_ENABLED && Boolean.TRUE.equals(treatOrgaAsOwner)) {
			String[] tempSplit = namespaceAndRepo.split(REPO_NAMESPACE_SEPARATOR);
			if (tempSplit.length >= 2) {
				return tempSplit[1]; // return the actual repo
			}
			LOG.warn("Unable to fix up namespace, does not contain expected separator: " + namespaceAndRepo);
		}
		return namespaceAndRepo;
	}

	public static final List<GitlabProject> getRepositories(GitlabAPI api) throws IOException {
//		return api.getAllProjects(); // only works with elevated permissions
		return api.getProjects();
	}
}
