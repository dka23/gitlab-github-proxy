package com.dkaedv.glghproxy.githubentity;

public class User {
	private String login;
	private String type = "User";
	
	public User(String login) {
		this.login = login;
	}
	
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
