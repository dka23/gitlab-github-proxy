package com.dkaedv.glghproxy.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.egit.github.core.Repository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/v3/orgs")
public class OrgsController {
	private final static Log LOG = LogFactory.getLog(OrgsController.class);
	
	@RequestMapping("/{orgname}/repos")
	@ResponseBody
	public List<Repository> getReposForOrg(
			@PathVariable String orgname,
			@RequestParam String per_page,
			@RequestParam String page,
			@RequestHeader("Authorization") String authorization) throws IOException {
		
		LOG.info("Received request: orgname=" + orgname + ", per_page=" + per_page + ", page=" + page + ", authorization=" + authorization);

		return Collections.emptyList();
	}
}
