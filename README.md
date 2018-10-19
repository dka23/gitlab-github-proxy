# GitLab-GitHub Proxy (glghproxy) [![Build Status](https://travis-ci.org/dka23/gitlab-github-proxy.svg?branch=master)](https://travis-ci.org/dka23/gitlab-github-proxy)

Proxy to provide GitHub-like API on top of Gitlab. Especially designed to use the **JIRA DVCS connector** with Gitlab.

**VERY IMPORTANT CONFIGURATION REQUIREMENTS:**

1. The address of glghproxy on your network MUST be DNS resolvable AND routable from BOTH a) JIRA server, and b) end-user browsers.

2. If you want to access not only the repositories of the configured user but all repositories the user has access to, 
   you need to set the property -DtreatOrgaAsOwner=true, either in Tomcat's CATALINA_OPTS or on the commandline. This will  
   encode the full path to the repository into the repository name so that JIRA can display it. This even supports Gitlab's 
   subgroups, that are not supported via the GitHub API.

## Setup

### Step 1. Launch glghproxy using Maven and Spring boot from CLI

**NOTICE:** The default Spring listen port is tcp/8080,
so specifying it again as a java argument is redundant,
but makes the problem explicit.  
You can choose to setup a port forward in front of it,
or (not recommended) run the service as root so you can listen on tcp/80.

```bash
mvn spring-boot:run -DgitlabUrl="http://yourgitlabserver.yourcompany.com" -Dserver.port=8080 -DtreatOrgaAsOwner=true
```

Alternatively, you can launch using Docker, and its resident service will proxy tcp/80 -> tcp/8080 for you:
```bash
docker build -t glghproxy .
docker run -p 80:8080 glghproxy
```

For the hostname (ie. `glghproxy`) you'll need to add a DNS entry or a `/etc/hosts` override.
However the latter will only work if glghproxy, JIRA, and the browser are operating on the same machine.
That can be nice for testing, (e.g., with a locally installed [containerized] trial version of JIRA) but
be aware that if you are using docker containers you'll need to ensure the hostname resolves to an IP that
is resolvable from all sides--in that case, the `docker0` interface ip is recommended.

**WARNING:** By default, JIRA and glghproxy want to operate on the same tcp/8080 port. If you are not
using containers, you'll have to resolve this conflict yourself.

### Step 2. Generate new Application in GitLab

- Browse to your Gitlab Applications tab (`/profile/applications`)
- On the `Add New Application` tab, fill in the `Name` field with your choice (ie. `glghproxy`)
- Fill in the `Redirect URI` with the address you've chosen for this proxy service.

	```
	http://glghproxy/login/oauth/authorize_callback
	```
- Click the `Save application` button.
- Make note of the `Application Id` and `Secret` on the following page.


### Step 3. Add a new DVCS account in JIRA

- Browse to your JIRA Administration > DVCS accounts page (`/secure/admin/ConfigureDvcsOrganizations.jspa`)
- Click the `Link Bitbucket Cloud or Github account` button.
- On the `Add New Account` popup, select `Github Enterprise` from the `Host` dropdown menu.
- Fill in `Team or User Account` field with the name of the Gitlab group or username containing the repositories you want JIRA to integrate.  
  **NOTICE:** You will need to add multiple DVCS accounts if your repositories are spread across more than one group or username.
- Fill in `Host  URL` with the address you've chosen for this proxy service. (ie. `http://glghproxy`)	
- Fill in `Client ID` with the *Application Id* generated for you by Gitlab earlier.
- Fill in `Client Secret` with the *Secret* generated for you by Gitlab earlier.
- The `Auto Link New Repositories` and `Enable Smart Commits` checkboxes are compatible and are safe to configure to your liking.
- Click the `Add` button and then the `Continue` button when prompted.
- It should say `Connecting to Github Enterprise to configure your account...`.
- Then it should say `Linking new account ...`.
- Then you should see your newly added account listed and the JIRA DVCS feature should operate normally from here.
