package com.web.model;

import java.time.LocalDate;

import com.web.dto.DiaryDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "mytraveldiary")
public class DiaryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long listNumber;
	
	@Column(unique = true)
	private String diaryTitle;
	
	
	@Column(columnDefinition = "TEXT")
	private String diaryContent;
	
	@Column
	private String diaryLocation;
	
	@Column
	private LocalDate diaryDate;
	
	@Column
	private String email;
	@Column
	private String nickname;
	
	@Lob
	/* @Column(nullable = false) */
    private String diaryImages;
	
	public static DiaryEntity toDiaryEntity(DiaryDTO diaryDTO) {
		DiaryEntity diaryEntity = new DiaryEntity();
		diaryEntity.setListNumber(diaryDTO.getListNumber());
		diaryEntity.setDiaryDate(diaryDTO.getDiaryDate());
		diaryEntity.setDiaryTitle(diaryDTO.getDiaryTitle());
		diaryEntity.setDiaryContent(diaryDTO.getDiaryContent());
		diaryEntity.setDiaryLocation(diaryDTO.getDiaryLocation());
		diaryEntity.setDiaryImages(diaryDTO.getDiaryImages());
		diaryEntity.setEmail(diaryDTO.getEmail());
		diaryEntity.setNickname(diaryDTO.getNickname());
		return diaryEntity;
	}

	public static DiaryEntity toUpdateEntity(DiaryDTO diaryDTO) {
		DiaryEntity diaryEntity = new DiaryEntity();
        diaryEntity.setListNumber(diaryDTO.getListNumber());
        diaryEntity.setDiaryDate(diaryDTO.getDiaryDate());
        diaryEntity.setDiaryTitle(diaryDTO.getDiaryTitle());
        diaryEntity.setDiaryContent(diaryDTO.getDiaryContent());
        diaryEntity.setDiaryLocation(diaryDTO.getDiaryLocation());
        diaryEntity.setDiaryImages(diaryDTO.getDiaryImages());
        diaryEntity.setEmail(diaryDTO.getEmail());
		diaryEntity.setNickname(diaryDTO.getNickname());
        return diaryEntity;
		
	}
}
