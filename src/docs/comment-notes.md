## 2025-08-14

### JpaConfig.java
- @EnableJpaAuditing: jpa auditing 기능을 사용하기 위함. JPA auditing기능을 통해서 id 혹은 metadata들을 JPA가 자동으로  부여하게 할 수 있다.
- @Configuration: configuration bean으로 만들어줌

### Article.java
- @Getter: 모든 필드는 접근이 가능하도록
- @EqualsAndHashCode: list 로 가져오거나 할 때 동일성 동등성 검사를 위한 구현. 그러나 그러면 모든 필드를 비교하게 되는데, 다른 방법을 쓰겠다고함(intelliJ에서 제공하는 equalsAndHashcode 사용함)
- @ToString: 쉽게 출력하기 위해
- @Table
    ```java
#### @EnableJpaAuditing
- jpa auditing 기능을 사용하기 위함. JPA auditing기능을 통해서 id 혹은 metadata들을 JPA가 자동으로  부여하게 할 수 있다.
#### @Configuration
- configuration bean으로 만들어줌
---
### Article.java
#### @Getter
- 모든 필드는 접근이 가능하도록
#### @EqualsAndHashCode
- list 로 가져오거나 할 때 동일성 동등성 검사를 위한 구현. 그러나 그러면 모든 필드를 비교하게 되는데, 다른 방법을 쓰겠다고함(intelliJ에서 제공하는 equalsAndHashcode 사용함)
#### @ToString
- 쉽게 출력하기 위해
#### @Table
```java
    @Table(indexes = {
            @Index(columnList = "title"),
            @Index(columnList = "hashtag"),
            @Index(columnList = "createdAt"),
            @Index(columnList = "createdBy")
    })
```

- @EntityListeners(AuditingEntityListener.class): test코드에 enableJpaAuditing했어도 각 entity에 entityListeners 붙여주지 않으면
jpaAuditing 사용이 안된다.

```
#### @EntityListeners(AuditingEntityListener.class)
- test코드에 enableJpaAuditing했어도 각 entity에 entityListeners 붙여주지 않으면
jpaAuditing 사용이 안된다.
#### @Entity
- entity 명시 하면 primary key도 만들어줘야함
#### @Id
- Jpa에서 엔티티의 기본키를 나타내는 어노테이션, 모든 엔티티에는 최소 하나의 @Id가 있어야 함
#### @GeneratedValue(strategy = GenerationType.IDENTITY)
- 자동으로 auto-increment해줌, identity방식으로 해야 auto-increment임, 기본키값을 자동생성하기 위한 것이므로 @Id랑 같이 씀
#### @Setter
- 수정 가능하게 하기위해 넣은 annotation.
```java
    // 수정 가능하게 @Setter. 만약 이 entity 자체에 @Setter 걸어버리면 사용자가 id같은 필드도 수정가능하게 되어버림
    // id나 아래의 metadata들은 jpa가 자동으로 부여하게 할 것이라 수정되면 안됨(JPA의 auditing 기능)
    @Setter @Column(nullable = false) private String title; // 제목: not null
    @Setter @Column(nullable = false, length = 10000) private String content; // 본문

    @Setter private String hashtag; // 해시태그: optional field로, null허용

    // 얘네가 JPA가 생성해주는 metadata를 말한다.
    @CreatedDate @Column(nullable = false) private LocalDateTime createdAt; // 생성일시
    @CreatedBy @Column(nullable = false, length = 100) private String createdBy; // 생성자
    @LastModifiedDate @Column(nullable = false) private LocalDateTime modifiedAt; // 수정일시
    @LastModifiedBy @Column(nullable = false, length = 100) private String modifiedBy; // 수정자
```
#### @ToString.Exclude, @OneToMany
```java
    @ToString.Exclude
    @OrderBy("id")
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private final Set<ArticleComment> articleComments = new LinkedHashSet<>();
```
##### @ToString.Exclude
- 여기 toString.Exclude가 붙은 이유는 lazy loaded fields 관련 경고때문에 제외해준것
- 그리고 circular referencing문제도 있다고 함
    - 얘가 articleComment안에 들어가서 toString을 찍을때 articleComment에도 Article구조가 있어서 순환참조가 됨
    - 보통 이렇게 끊을 때는 부모(?)쪽에서 끊는다(그래서 article entity에서 끊은 것)
##### @OneToMany
- 양방향 바인딩 OnetoMany
- onetomany에 이름을 정해주지 않으면 매핑한 두가지를 합쳐서 테이블을 만들어버림
- ~~이름을 정했기 때문에, 이건 article이라는 테이블로부터 온 것임을 명시하는 것~~
> @onetomany(mappedby = "article")은 양방향 연관관계에서 연관관계의 주인이 아닌 쪽임을 나타낸다.<br>
> Article엔티티가 아닌 articleComment 엔티티가 연관관계의 주인임.<br>
> 주인이란 DB FK값을 실제 관리하는 쪽<br>
> ArticleComment에서 이미 @ManyToOne으로 FK를 관리하고 있기 때문에 Article에서 다시 FK만들면 중복관리가 되어 충돌할 수 있어서 `mappedby`필드로  연관관계를 관리하겠다는 것임<br>
> mappedby에 적어준 필드는 Set을 어떤 타입으로 만들었느냐에 따른다.
- 실무에서는 양방향 바인딩을 일부러 푸는 경우도 많다
    - 예를 들어 게시글을 삭제시 연관된 댓글이 다 사라지는 등 운영에서의 데이터 편집시 문제가 생길수 있기에.
    - 그래서 fk를 안 걸기도 한다
- #### JPA를 통해서 update등이 자동으로 이루어진다는게 새로웠는데 실무에선 이것을 일부러 푼다니 궁금해져서 수동 처리 예시를 보았다.
    ```java
    @Service
    @Transactional
    public class ArticleService {

        private final ArticleRepository articleRepository;
        private final ArticleCommentRepository commentRepository;

        public ArticleService(ArticleRepository ar, ArticleCommentRepository cr) {
            this.articleRepository = ar;
            this.commentRepository = cr;
        }

        public void deleteArticle(Long articleId) {
            // 1. 댓글 먼저 삭제 (양방향 매핑이 없으므로 수동)
            List<ArticleComment> comments = commentRepository.findByArticleId(articleId);
            for (ArticleComment c : comments) {
                commentRepository.delete(c);
            }

            // 2. 게시글 삭제
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new RuntimeException("Article not found"));
            articleRepository.delete(article);
        }
    }
    ```
    - 물론 이렇게 해서 삭제를 한다면 그것도 문제가 될것.. 그래서 삭제에 대한 패턴도 찾아보았다.
        - ##### 논리 삭제
            - DB row 자체는 남겨둠
            - 게시판 UI에서만 안 보이게 처리
            - 장점: 데이터 복구 가능, 감사(audit) 기록 유지
            ```java
            @Entity
            public class Article {
                @Id @GeneratedValue
                private Long id;

                private String title;
                private String content;

                private boolean deleted = false; // 논리 삭제 여부

                public void delete() {
                    this.deleted = true;
                }
            }
            ```
            - 서비스 레벨에서 조회할 때는 `deleted = false` 조건 추가
---
### ArticleComment.java
#### 연관관계
- 위에 article.java의 @OneToMany에도 일부 있는 내용
```java
    // 연관관계 없음
    @Setter private Long articleId;

    // 연관관계 있음
    @Setter @ManyToOne(optional = false) private Article article; // 게시글 ID
```

#### hibernate구현체를 위한 빈 생성자
`protected ArticleComment() {}`는 `@noArgsConstructor(access = AccessLevel.PROTECTED)`로도 가능

---

### jpaRepositoryTest.java
#### @Import(JpaConfig.class)
- jpaConfig에 auditing 기능을 넣었기 때문에 import해주어야 한다
#### @DataJpaTest
- datajpatest가 포함한 annotation들 중 springExtends를 통해 autowired를 가지고 있는 녀석이 있어서 생성자 주입 패턴으로 필드 만드는 것이 가능해진다
- 예시
```java
@DataJpaTest
class JpaRepositoryTest {
    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;

    JpaRepositoryTest(
                @Autowired ArticleRepository articleRepository,
                @Autowired ArticleCommentRepository articleCommentRepository
        ) {
            this.articleRepository = articleRepository;
            this.articleCommentRepository = articleCommentRepository;
        }

}
```

- 어차피  @Autowired쓰는데 생성자 주입 패턴으로 필드 만드는게 가능한점이 왜 좋은 건지 이해가 안갔다.
- `@Autowired private SomeRepository someRepository`하는 거랑 무슨 차이인가?
- @Autowired일때는 `final`필드를 못 넣는데 생성자 주입시 `final`이 되어서 코드 안전성이 증가한다고 함.
- `final`이기 때문에 `someRepository = 다른값` 이런식으로 재할당이 불가
- 그러나 someRepository 내부상태는 바뀔 수가 있음
- 의존성이 한번 주입된 후에 다른 Repository로 바뀌는 실수를 방지한다고.

