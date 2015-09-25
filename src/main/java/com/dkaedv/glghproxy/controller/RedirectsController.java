package com.dkaedv.glghproxy.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RedirectsController {

	@Value("${gitlabUrl}")
	private String gitlabUrl;

	@RequestMapping("/{namespace}")
	public String namespace(
			@PathVariable String namespace
			) {
		
		return "redirect:" + gitlabUrl + "/" + namespace;
	}

	@RequestMapping("/{namespace}/{repo}")
	public String repoHomepage(
			@PathVariable String namespace,
			@PathVariable String repo
			) {
		
		return "redirect:" + gitlabUrl + "/" + namespace + "/" + repo;
	}
	
	@RequestMapping("/{namespace}/{repo}/commit/{sha}")
	public String commit(
			@PathVariable String namespace,
			@PathVariable String repo,
			@PathVariable String sha
			) {
		
		return "redirect:" + gitlabUrl + "/" + namespace + "/" + repo + "/commit/" + sha;
	}

	@RequestMapping("/{namespace}/{repo}/tree/{branch}")
	public String repoTree(
			@PathVariable String namespace,
			@PathVariable String repo,
			@PathVariable String branch
			) {
		
		return "redirect:" + gitlabUrl + "/" + namespace + "/" + repo + "/tree/" + branch;
	}

}
