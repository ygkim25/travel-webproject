package com.web.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.web.dto.Board;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface FileService {
	
	public List<String> processUploadFiles(List<MultipartFile> uploadFiles
			, String memberId
			, String uploadFolder
			, Board board
			, int width
			, int height ) throws IOException;
	
	public List<String> listFiles(String directoryPath) throws IOException;
	
}
