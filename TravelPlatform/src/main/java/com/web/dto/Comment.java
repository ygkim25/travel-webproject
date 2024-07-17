package com.web.dto;



import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;  //javax로 쓰면 not a managed type 에러
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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
@Table(name="comment")
@Entity
public class Comment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String writer;
	private String nickname;
	
	
	@Column(columnDefinition = "TEXT", nullable = false) 
	private String content;
	
	@Column(insertable = false, updatable = false, columnDefinition = "TIMESTAMP default CURRENT_TIMESTAMP")
	private Date createDate; // 테이블 필드명은 create_date
	
	//board와 연결
	//글 한 개에 여러 댓글 
	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)//글이 지워지면 해당 글의 댓글도 모두 삭제
	@JoinColumn(name="seq", referencedColumnName = "seq")
	private Board board;
	
	// 부모 자식 간 many to one, one to many 양방향 매핑 안하면 에러
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id") //자동으로 Long(bigint)로 db에 지정?
	@ToString.Exclude
	private Comment parent;
	
	@OneToMany(mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL)
	@ToString.Exclude
	private List<Comment> child; 

	
}
