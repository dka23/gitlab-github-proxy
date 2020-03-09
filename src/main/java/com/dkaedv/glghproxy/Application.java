package com.dkaedv.glghproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	/**
	 * The date format used by GitHub. No millis! JIRA DVCS Connector fails parsing
	 * dates when we have millis, which results in the timezone not being applied.
	 */
	public static final String GITHUB_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	public static void main(String[] args) {
		System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
		SpringApplication.run(Application.class, args);
	}
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
	
	@Bean
	public Jackson2ObjectMapperBuilder jacksonBuilder() {
		return createObjectMapperBuilder();
	}

	private static Jackson2ObjectMapperBuilder createObjectMapperBuilder() {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder
			.indentOutput(true)
			.simpleDateFormat(GITHUB_DATE_FORMAT)
			.serializationInclusion(Include.NON_NULL) // include empty collections; JIRA does not like commtis without files
			.propertyNamingStrategy(new PropertyNamingStrategy.SnakeCaseStrategy());
		
		return builder;
	}

	public static ObjectMapper createObjectMapper() {
		return createObjectMapperBuilder().build();
	}
}
