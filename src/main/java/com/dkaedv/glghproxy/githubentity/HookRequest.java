package com.dkaedv.glghproxy.githubentity;

import java.util.List;
import java.util.Map;

public class HookRequest {
	private Integer id;
	private String name;
	private boolean active;
	private List<String> events;
	private Map<String, String> config;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public List<String> getEvents() {
		return events;
	}
	public void setEvents(List<String> events) {
		this.events = events;
	}
	public Map<String, String> getConfig() {
		return config;
	}
	public void setConfig(Map<String, String> config) {
		this.config = config;
	}
}
