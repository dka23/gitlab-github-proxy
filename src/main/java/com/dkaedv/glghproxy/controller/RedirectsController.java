package com.dkaedv.glghproxy.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RedirectsController {

	@Value("${gitlabUrl}")
	private String gitlabUrl;
	
	@RequestMapping("/{namespace}/{repo}")
	public String repoHomepage(
			@PathVariable String namespace,
			@PathVariable String repo
			) {
		
		return "redirect:" + gitlabUrl + "/" + namespace + "/" + repo;
	}
}
