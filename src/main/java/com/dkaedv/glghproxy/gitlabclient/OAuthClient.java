package com.dkaedv.glghproxy.gitlabclient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.dkaedv.glghproxy.Constants;
import com.dkaedv.glghproxy.githubentity.AccessToken;

@Component
public class OAuthClient {
	private final static Log LOG = LogFactory.getLog(OAuthClient.class);

	@Value("${gitlabUrl}")
	private String gitlabUrl;

	public AccessToken requestAccessToken(String client_id, String client_secret, String code, String callbackUrl) {
		if (!Constants.STRICT_SSL) {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
		}
		
		RestTemplate rest = new RestTemplate();
		
		MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<String, String>();
		requestParams.add("client_id", client_id);
		requestParams.add("client_secret", client_secret);
		requestParams.add("code", code);
		requestParams.add("grant_type", "authorization_code");
		requestParams.add("redirect_uri", callbackUrl);
		
		LOG.info("Requesting access token with params" + requestParams);
		
		AccessToken token = rest.postForObject(gitlabUrl + "/oauth/token", requestParams, AccessToken.class);
		
		LOG.info("Received token");
		
		return token;
	}
	
}
