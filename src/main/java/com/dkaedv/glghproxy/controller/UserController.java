package com.dkaedv.glghproxy.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/v3/user")
public class UserController {

	@RequestMapping("/repos")
	@ResponseBody
	public List<Repository> getReposForCurrentUser(@RequestParam String per_page, @RequestParam String page,
			@RequestHeader("Authorization") String authorization) throws IOException {

		return Collections.emptyList();
	}

}
