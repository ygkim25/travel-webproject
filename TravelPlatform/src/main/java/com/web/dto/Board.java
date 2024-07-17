package com.web.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;



@Getter
@Setter
@ToString  
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="board")
public class Board {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)//primary key
	private Long seq;  //'id'로 변경
	private String title; 
	
	@Column(updatable = false)
	private String writer;
	@Column(length = 3000)
	private String content;
	
	private String nickname;
	//dynamic json data	
	@Column(columnDefinition = "TEXT")
	private String JsonContent;
	
	@Column(insertable = false, updatable = false, columnDefinition = "TIMESTAMP default CURRENT_TIMESTAMP")
	private Date createDate; // 테이블 필드명은 create_date
	@Column(insertable = false, updatable = false, columnDefinition = "bigint default 0")
	private Long cnt; 
	
	//@Column(columnDefinition = "TEXT")
	@ElementCollection
	@CollectionTable(
		name = "list_file_names",  // Name of the table to store the collection elements
		joinColumns = {
				@JoinColumn(name = "seq", referencedColumnName = "seq"), 
				}
			)
	@Column(name="processed_file_names")
	private List<String> ProcessedFileNames;
	
	//글 추천 기능
	private Long likes;	
	
	//댓글 수 
	@Column(name = "comment_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
	private Long commentCount = 0L;  
	

	@Transient //안하면 에러.
	private MultipartFile uploadFile;

    // Override toString() method to exclude parent and child
    @Override
    public String toString() {
        return "Board{" +
                "seq=" + seq +
                ", writer='" + writer + '\'' +
                ", content='" + content + '\'' +
                ", createDate='" + createDate + '\'' +
                ", nickname=" + nickname +
                '}';
    }		
   
    //@Override
    public Long getLikes() {
    	return likes != null? likes : 0L;
    }
  
    //댓글수 표시
    
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

}
