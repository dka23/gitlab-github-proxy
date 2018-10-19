FROM anapsix/alpine-java:8_jdk_unlimited

WORKDIR /home/app

# install dumb-init; helps dockerized java handle signals properly
# using ADD avoids installing openssl dependency
ADD https://github.com/Yelp/dumb-init/releases/download/v1.1.3/dumb-init_1.1.3_amd64 /usr/bin/dumb-init
RUN chmod +x /usr/bin/dumb-init
ENTRYPOINT ["dumb-init", "--"]

COPY . ./

# process will run as non-root `app` user
RUN addgroup app \
		&& adduser -s /bin/bash -D app -G app \
		&& chown -R app:app .

# install maven
RUN wget http://mirrors.ocf.berkeley.edu/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz \
		&& tar zxvf apache-maven-3.3.9-bin.tar.gz \
		&& rm -f apache-maven-3.3.9-bin.tar.gz 

USER app

EXPOSE 8080

# start with spring boot
CMD apache-maven-3.3.9/bin/mvn spring-boot:run -DgitlabUrl="$GITLAB_URL" -DtreatOrgaAsOwner="$TREAT_ORGA_AS_OWNER"
