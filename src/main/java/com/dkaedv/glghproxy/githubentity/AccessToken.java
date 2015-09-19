package com.dkaedv.glghproxy.githubentity;

public class AccessToken {
	private String access_token;
	private String scope = "repo";
	private String token_type = "bearer";
	
	public AccessToken() {}
	
	public AccessToken(String access_token) {
		this.access_token = access_token;
	}
	public String getAccess_token() {
		return access_token;
	}
	public String getScope() {
		return scope;
	}
	public String getToken_type() {
		return token_type;
	}
	
	@Override
	public String toString() {
		return "access_token=" + access_token + "&scope=" + scope + "&token_type=" + token_type;
	}
}
