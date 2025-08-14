package org.fc.fcboardwork.domain;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter // 모든 필드는 접근이 가능하도록
// @EqualsAndHashCode // list 로 가져오거나 할 때 동일성 동등성 검사를 위한 구현. 그러나 그러면 모든 필드를 비교하게 되는데, 다른 방법을 쓰겠다고함
@ToString // 쉽게 출력
@Table(indexes = {
        @Index(columnList = "title"),
        @Index(columnList = "hashtag"),
        @Index(columnList = "createdAt"),
        @Index(columnList = "createdBy")
})
// test코드에 enableJpaAuditing했어도 각 entity에 entityListeners 붙여주지 않으면
// jpaAuditing 사용이 안된다.
@EntityListeners(AuditingEntityListener.class)
@Entity // entity 명시 하면 primary key도 만들어줘야함
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동으로 auto-increment, identity방식으로 해야 auto-increment임
    private Long id;

    // 수정 가능하게 @Setter. 만약 이 entity 자체에 @Setter 걸어버리면 사용자가 id같은 필드도 수정가능하게 되어버림
    // id나 아래의 metadata들은 jpa가 자동으로 부여하게 할 것이라 수정되면 안됨(JPA의 auditing 기능)
    @Setter @Column(nullable = false) private String title; // 제목: not null
    @Setter @Column(nullable = false, length = 10000) private String content; // 본문

    @Setter private String hashtag; // 해시태그: optional field로, null허용

    // 여기 toString.Exclude가 붙은 이유는 lazy loaded fields 때문에 제외해준것
    // 그리고 circular referencing문제도 있다고 함
    // 얘가 articleComment안에 들어가서 toString을 찍을때 articleComment에도 Article구조가 있어서 순환참조가 됨
    // 보통 이렇게 끊을 때는 부모(?)쪽에서 끊는다
    @ToString.Exclude
    // 양방향 바인딩 OnetoMany
    // onetomany에 이름을 정해주지 않으면 매핑한 두가지를 합쳐서 테이블을 만들어버림
    // 이름을 정했기 때문에, 이건 article이라는 테이블로부터 온 것임을 명시하는 것
    // 실무에서는 양방향 바인딩을 일부러 푸는 경우도 많다
    // 예를 들어 게시글을 삭제시 연관된 댓글이 다 사라지는 등 운영에서의 데이터 편집시 문제가 생길수 있기에.
    // 그래서 fk를 안 걸기도 한다
    @OrderBy("id")
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private final Set<ArticleComment> articleComments = new LinkedHashSet<>();

    @CreatedDate @Column(nullable = false) private LocalDateTime createdAt; // 생성일시
    @CreatedBy @Column(nullable = false, length = 100) private String createdBy; // 생성자
    @LastModifiedDate @Column(nullable = false) private LocalDateTime modifiedAt; // 수정일시
    @LastModifiedBy @Column(nullable = false, length = 100) private String modifiedBy; // 수정자

    // JPA entity는 hibernate구현체를 사용, 기본생성자를 가지고 있어야 한다
    // 평소에 오픈할 일이 없기에 protected로 해서 코드 밖에서 new로 생성은 못하게 함
    protected Article() {
    }

    private Article(String title, String content, String hashtag) {
        this.title = title;
        this.content = content;
        this.hashtag = hashtag;
    }

    public static Article of(String title, String content, String hashtag) {
        return new Article(title, content, hashtag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article article)) return false;
        // 원래 id != null 부분은 없었으나,
        // id에 대한 영속성이 부여되기 전에(insert가 일어나기 전에) 생성된 entity에서 id가 null인 경우를 막기위해
        // id != null을 추가함
        // 결과: 영속화되지 않은 entity는 모두 동등성검사를 탈락한다.
        return id != null && Objects.equals(id, article.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
