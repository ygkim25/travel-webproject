package com.web.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.web.dto.DiaryDTO;
import com.web.model.DiaryEntity;
import com.web.model.User;
import com.web.repository.DiaryRepository;
import com.web.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryService {
    
    private final DiaryRepository diaryRepository;

    public void save(DiaryDTO diaryDTO) {
        DiaryEntity diaryEntity = DiaryEntity.toDiaryEntity(diaryDTO);
        diaryRepository.save(diaryEntity);
    }

    public List<DiaryDTO> findAll() {
        List<DiaryEntity> diaryEntityList = diaryRepository.findAll();
        List<DiaryDTO> diaryDTOList = new ArrayList<>();
        for (DiaryEntity diaryEntity : diaryEntityList) {
            diaryDTOList.add(DiaryDTO.toDiaryDTO(diaryEntity));
        }
        return diaryDTOList;
    }

    public DiaryDTO findById(Long listNumber) {
        Optional<DiaryEntity> optionalDiaryEntity = diaryRepository.findById(listNumber);
        if (optionalDiaryEntity.isPresent()) {
            DiaryEntity diaryEntity = optionalDiaryEntity.get();
            return DiaryDTO.toDiaryDTO(diaryEntity);
        } else {
            throw new RuntimeException("Diary not found with listNumber: " + listNumber);
        }
    }

    public DiaryDTO update(DiaryDTO diaryDTO) {
        DiaryEntity diaryEntity = DiaryEntity.toUpdateEntity(diaryDTO);
        diaryRepository.save(diaryEntity);
        return findById(diaryDTO.getListNumber());
    }

    public void delete(Long listNumber) {
        diaryRepository.deleteById(listNumber);
        diaryRepository.updateListNumbersAfterDelete(listNumber);
    }

    public Page<DiaryDTO> paging(Pageable pageable) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 3;
        Page<DiaryEntity> diaryEntities = diaryRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "listNumber")));

        return diaryEntities.map(diary -> new DiaryDTO(
                diary.getListNumber(),
                diary.getDiaryTitle(),
                diary.getDiaryContent(),
                diary.getDiaryLocation(),
                diary.getDiaryDate(),
                diary.getDiaryImages(),
                diary.getEmail(),
                diary.getNickname()
        ));
    }
 // 이메일을 기반으로 사용자가 작성한 여행 일지를 조회하는 메서드
    public Page<DiaryDTO> findByEmail(String email, Pageable pageable) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 3;
        Page<DiaryEntity> diaryEntities = diaryRepository.findByEmailOrderByListNumberDesc(email, PageRequest.of(page, pageLimit));

        return diaryEntities.map(diary -> new DiaryDTO(
                diary.getListNumber(),
                diary.getDiaryTitle(),
                diary.getDiaryContent(),
                diary.getDiaryLocation(),
                diary.getDiaryDate(),
                diary.getDiaryImages(),
                diary.getEmail(),
                diary.getNickname()
        ));
    }

    public List<DiaryDTO> findAllByEmail(String email) {
        List<DiaryEntity> diaryEntities = diaryRepository.findByEmailOrderByListNumberDesc(email);
        
        return diaryEntities.stream()
                .map(diary -> new DiaryDTO(
                    diary.getListNumber(),
                    diary.getDiaryTitle(),
                    diary.getDiaryContent(),
                    diary.getDiaryLocation(),
                    diary.getDiaryDate(),
                    diary.getDiaryImages(),
                    diary.getEmail(),
                    diary.getNickname()
                ))
                .collect(Collectors.toList());
    }


    public List<DiaryDTO> findTop4ByOrderByListNumberDesc() {
        Pageable topFour = PageRequest.of(0, 4); // 첫 페이지, 4개의 결과
        List<DiaryEntity> diaries = diaryRepository.findTop4ByOrderByListNumberDesc(topFour);
        return diaries.stream()
                      .map(DiaryDTO::toDiaryDTO)
                      .collect(Collectors.toList());
    }


}
