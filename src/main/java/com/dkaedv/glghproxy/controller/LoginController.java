package com.dkaedv.glghproxy.controller;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dkaedv.glghproxy.gitlabclient.OAuthClient;

@Controller
@RequestMapping("/login/oauth")
public class LoginController {
	@Value("${gitlabUrl}")
	private String gitlabUrl;
	
	@Autowired
	private OAuthClient oauthClient;
	
	private String redirectUri;
	
	/**
	 * Step 1 - OAuth request from client application (e.g. JIRA)
	 * @throws MalformedURLException 
	 */
	@RequestMapping("/authorize")
	public String authorize(
			@RequestParam String scope,
			@RequestParam String client_id,
			@RequestParam String redirect_uri,
			HttpServletRequest request) throws UnsupportedEncodingException, MalformedURLException {
		
		// Save redirect uri
		this.redirectUri = redirect_uri;
		
		String callbackUrl = buildCallbackUrl(request);
		
		return "redirect:" + gitlabUrl + "/oauth/authorize?client_id=" + client_id + "&response_type=code&redirect_uri=" + callbackUrl;
	}

	private String buildCallbackUrl(HttpServletRequest request) throws MalformedURLException {
		return new URL(new URL(request.getRequestURL().toString()), "./authorize_callback").toString();
	}

	@RequestMapping("/authorize_callback")
	public String gitlabCallback(
			@RequestParam String code
			) {

		String answer = "redirect:" + this.redirectUri + "&code=" + code;
		
		// Clear redirect uri
		this.redirectUri = null;
		
		return answer;
	}
	
	
	/**
	 * Step 3 - Client application exchanges code for an access token.
	 */
	@RequestMapping(value = "/access_token", method = RequestMethod.POST)
	@ResponseBody
	public String accessToken(
			@RequestParam String client_id,
			@RequestParam String client_secret,
			@RequestParam String code,
			HttpServletRequest request
			) throws MalformedURLException {

		return oauthClient.requestAccessToken(client_id, client_secret, code, buildCallbackUrl(request)).toString();
	}
}
