package com.jmj.board.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter // 모든 필드는 접근이 가능해야한다.
// 세터는 전체레벨로 잡지 않을 것이다.
@ToString
// 인덱스 잡을 수 있다.
// 검색 조건에 인덱스를 걸자.
// 엔덱스에도 용량 제한이 있다?
@Table(indexes = {
        @Index(columnList = "title"),
        @Index(columnList = "hashtag"),
        @Index(columnList = "createdAt"),
        @Index(columnList = "createdBy"),
})
@EntityListeners(AuditingEntityListener.class) // 테스트코드를 위해(?)
@Entity
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String title; // 제목

    @Setter
    @Column(nullable = false, length = 10000)
    private String content; // 본문

    @Setter
    private String hashtag; // 해시태그

    // 실무에서는 양방향 매핑은 잘 사용하지 않는다.
    // cascade 에 의해서 서로 너무 연관되어있어서 불편함이 있을 수 있다.
    @OrderBy("id")
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<ArticleComment> articleCommentSet = new LinkedHashSet<>();

    // 메타데이터들
    // 시간 자동 : JPA audting (JpaConfig 에 만듦)
    @CreatedDate // JPA audting
    @Column(nullable = false)
    private LocalDateTime createdAt; // 생성일시

    @CreatedBy // JPA audting
    @Column(nullable = false, length = 100)
    private String createdBy; // 생성자

    @LastModifiedDate // JPA audting
    @Column(nullable = false)
    private LocalDateTime modifiedAt; // 수정일시

    @LastModifiedBy // JPA audting
    @Column(nullable = false, length = 100)
    private String modifiedBy; // 수정자

    public void setArticleCommentSet(Set<ArticleComment> articleCommentSet) {
        this.articleCommentSet = articleCommentSet;
    }

    // 여기 밖에서는 new로 생성하지 못하도록 막아둠
    protected Article() {}

    private Article(String title, String content, String hashtag) {
        this.title = title;
        this.content = content;
        this.hashtag = hashtag;
    }

    // 팩토리 메서드
    // 아티클을 생성하고자 할 때는 해당 메서드를 사용하세요.
    public static Article of(String title, String content, String hashtag) {
        return new Article(title, content, hashtag);
    }

    // 데이터베이스 접근 로직 테스트 정의2 강의 참고
    // 동등성 검사를 하기위해서 모든 필드가 맞는지 검사할 필요가 없다.
    // id 만 검사하면 된다.
    // 나중에 연관관계 매핑할 때 빛을 발휘하게되는 코드?
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // java 14 pattern matching
        if (!(o instanceof Article article)) return false;
        return id != null && id.equals(article.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
