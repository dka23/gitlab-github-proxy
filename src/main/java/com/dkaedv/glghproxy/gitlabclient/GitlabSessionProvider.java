package com.dkaedv.glghproxy.gitlabclient;

import org.gitlab.api.GitlabAPI;
import org.gitlab.api.TokenType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GitlabSessionProvider {

	@Value("${com.dkaedv.glghproxy.gitlab_url}")
	private String gitlabUrl;
	
	public GitlabAPI connect(String authorizationHeader) {
		String token = authorizationHeader.replaceAll("token ", "");
		
		GitlabAPI api = GitlabAPI.connect(gitlabUrl, token, TokenType.ACCESS_TOKEN);
		api.ignoreCertificateErrors(true);
		return api;
	}
}
