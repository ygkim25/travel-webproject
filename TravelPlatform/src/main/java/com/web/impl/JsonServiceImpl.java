package com.web.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.web.service.JsonService;

@Service
public class JsonServiceImpl implements JsonService {

	@Override
	public List<String> parseJson(String jsonData) {

		List<String> parsedStrings = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();

		try {

			List<Map<String, String>> dataList = objectMapper.readValue(jsonData,
					new TypeReference<List<Map<String, String>>>() {
					});

			for (Map<String, String> data : dataList) {
				parsedStrings.add(data.get("text"));

			}
			System.out.println("parsedStrings : " + parsedStrings);
			System.out.println("parsedStrings.getClass() : " + parsedStrings.getClass());
			// *******************************************************************************************

		} catch (Exception e) {
			e.printStackTrace();
		}

		return parsedStrings;
	}

	// json 체크
	@Override
	public String preprocessJson(String jsonData) {
		// Check if jsonData is an array or an object
		// If it's an object, wrap it in square brackets to make it an array
		if (!jsonData.trim().startsWith("[")) {
			return "[" + jsonData + "]";
		} else {
			return jsonData;
		}
	}
}
