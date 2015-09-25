package com.dkaedv.glghproxy.converter;

import org.eclipse.egit.github.core.PullRequest;
import org.gitlab.api.models.GitlabMergeRequest;
import org.gitlab.api.models.GitlabUser;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitlabToGithubConverterTest {

	@Test
	public void shouldConvertPullRequest() {
		GitlabMergeRequest mergeRequest = new GitlabMergeRequest();
		GitlabUser user = new GitlabUser();
		user.setEmail("hanswurscht@test.com");
		user.setId(5);
		mergeRequest.setAssignee(user);
		mergeRequest.setAuthor(user);
		mergeRequest.setId(15);
		mergeRequest.setIid(3);
		mergeRequest.setState("merged");
		
		PullRequest pull = GitlabToGithubConverter.convertMergeRequest(mergeRequest, "http://gitlab", "testns", "test");
		
		assertEquals("hanswurscht@test.com", pull.getAssignee().getEmail());
		assertEquals("http://gitlab/testns/test/merge_requests/3", pull.getHtmlUrl());
		
	}

	@Test
	public void shouldConvertMergedPullRequestWithNullAssignee() {
		GitlabMergeRequest mergeRequest = new GitlabMergeRequest();
		GitlabUser user = new GitlabUser();
		user.setEmail("hanswurscht@test.com");
		user.setUsername("hanswurscht");
		user.setId(5);
		mergeRequest.setAuthor(user);
		mergeRequest.setId(15);
		mergeRequest.setIid(3);
		mergeRequest.setState("merged");
		
		PullRequest pull = GitlabToGithubConverter.convertMergeRequest(mergeRequest, "http://gitlab", "testns", "test");
		
		assertEquals("hanswurscht@test.com", pull.getMergedBy().getEmail());
		
	}
}
