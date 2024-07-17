package com.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//DTO

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BoardInfoDTO {
	
	//wishlist로 일부 정보 제공
	
	private Long boardId;
	private String title;
	private String writer;
	private String thumbnail;
	
	
}
