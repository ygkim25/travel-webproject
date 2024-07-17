package com.web.impl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.web.dto.Board;
//import com.web.dto.Member;
import com.web.service.FileService;
           
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



@Service
@SessionAttributes("member")
public class FileServiceImpl implements FileService {

	@Autowired
	private HttpServletRequest request;

	@Value("${path.upload}")
	private String uploadFolder;

	@Override
	public List<String> processUploadFiles(List<MultipartFile> uploadFiles, String memberId, String uploadFolder, Board board, int width, int height)
			throws IOException {

		List<String> processedFileNames = new ArrayList<>();

		List<String> fileNames = new ArrayList<>();

		for (MultipartFile uploadFile : uploadFiles) {
			if (!uploadFile.isEmpty()) {
						
				
				String originalFileName = uploadFile.getOriginalFilename();
				String uuid = UUID.randomUUID().toString();
				
				String fileName = uuid + "_" + originalFileName;
			
				fileName = new String(fileName.getBytes(StandardCharsets.UTF_8),StandardCharsets.UTF_8);
		
				File memberUploadFile = new File(uploadFolder, memberId);
				if (memberUploadFile.exists() == false) {
					memberUploadFile.mkdirs();
				}

				File uploadedFile = new File(memberUploadFile, fileName);
				System.out.println("uploadedFile : " + uploadedFile);
				uploadFile.transferTo(uploadedFile);
			
				
				try {
					//thumbnail
					File thumbnailFile = new File(memberUploadFile, "s_" + fileName);
					String imagePath = memberUploadFile.getAbsolutePath() + "\\" + fileName;
					File imageFile = new File(imagePath);
					BufferedImage bo_image = ImageIO.read(imageFile);
					BufferedImage bt_image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
					Graphics2D graphic = bt_image.createGraphics();
					graphic.drawImage(bo_image, 0, 0, width, height, null);
					ImageIO.write(bt_image, "jpg", thumbnailFile);
					String thumbnailName = thumbnailFile.getName();

					
					processedFileNames.add(fileName); 
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				break;
			}
		}
		//확인용
		System.out.println("processedFileNames" + processedFileNames);
		
		// Set the processed file names in the board object
		board.setProcessedFileNames(processedFileNames);
		System.out.println("processed Files to board : " + board);
		return processedFileNames; // Return the list of processed file names
	}

	@Override
	public List<String> listFiles(String directoryPath) throws IOException {
		
		System.out.println("Initiate file retrieve..............................");
		
		List<String> fileNames = new ArrayList<>();
		
		Path dir = Paths.get(directoryPath);
		System.out.println("directory to fetch files : " + dir);
		
		try {
			// Check if directory exists
			if (!Files.exists(dir) || !Files.isDirectory(dir)) {
				throw new IOException("Directory does not exist or is not a valid directory: " + directoryPath);
			}
			//if directory exists:
			//iterate files in directory
			Files.list(dir)
			.filter(Files::isRegularFile)
			.forEach(path -> fileNames.add(path.getFileName().toString()));			
			
		}catch(IOException e) {
			 System.err.println("Error accessing directory: " + e.getMessage());
			 //Return an empty list of file names
	         return fileNames;
			
		}
		
		return fileNames;
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
