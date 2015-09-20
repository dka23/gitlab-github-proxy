package com.dkaedv.glghproxy;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class DateObjectMapper extends ObjectMapper {
	private static final long serialVersionUID = 1L;

	public DateObjectMapper() {
        this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}
	
}
