package com.dkaedv.glghproxy.githubentity;

public class Repository {
	private int id;
	private String name;
	private String full_name;
	private User owner = new User("test");
	
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	public Repository(int id, String name, String full_name) {
		this.id = id;
		this.name = name;
		this.full_name = full_name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFull_name() {
		return full_name;
	}
	public void setFull_name(String full_name) {
		this.full_name = full_name;
	}
}
