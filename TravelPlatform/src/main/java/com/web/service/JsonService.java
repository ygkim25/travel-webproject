package com.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface JsonService {
	public List<String> parseJson(String JsonData);

	public String preprocessJson(String jsonData);
}
