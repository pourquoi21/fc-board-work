##  2025-08-14


### application.properties에서
- spring.application.name이 자동으로 들어있었는데, 이것은 spring application의 이름을 지정하는 설정임
  - 로그나 모니터링 툴에서 애플리케이션 식별자 역할
  - Spring Cloud, Eureka 같은 분산 시스템에서 서비스 이름으로 사용
  - 이 설정을 없애도 기본적으로 앱이 동작하는 데 큰 문제는 없다.
  - 다만 특정 라이브러리나 인프라가 이 값을 활용하는 경우에는 이 값이 없으면 식별이 어려울 수 있음
---
### test코드 돌릴 때
#### main/resources폴더 안에 만든 data.sql파일에 아무 내용이 없다면
- boot 2.5정도의 버전에서는 test failed가 뜨는 것으로 보임
- 3.5.4에서는 해당 에러 발생하지 않았음
#### 테스트 돌리는 데 시간이 너무 오래 걸림
- intelliJ의 settings / build, execution, ... / build tools를 Gradle에서 intelliJ IDEA로 바꾸어 해결
  - Gradle빌드로 테스트를 돌리면 매번 JVM을 새로 띄우기 때문에 캐시가 되지 않아 오래 걸림
- 개별 테스트를 Run으로 반복 실행하는 경우에도 매번 새 실행이 되어 느릴 수 있음
    - 한 번 실행 후 '전체 테스트'를 돌리면 빨라질 수 있음
  #### 그래서 언제 뭘 써야 하는가?
    - CI/CD환경과 100% 동일하게 검증해야함 -> gradle
    - 로컬 개발 중 반복 테스트 빠르게 해야함 -> intelliJ
---
### yaml설정과 로그
####  hibernate관련 alter table 등의 로그가 나오지 않는 문제가 있었음
>   #### Spring Boot 3.x Hibernate 로깅 설정<br>
>   org.hibernate.SQL: debug<br>
>   org.hibernate.type.descriptor.sql.BasicBinder: trace<br>
>   org.hibernate.orm.jdbc.bind: trace<br>
>   org.hibernate.orm.jdbc.extract: debug<br>
>   org.springframework.orm.jpa: debug<br>
>   org.springframework.transaction: debug<br>
-   이것들을 추가하고, 잘못된 indent 수정함.
- boot 2.5.x에서 이것이 없어도 나오지 않았던 이유는 spring boot 버전 차이로 인해 hibernate로깅 설정이 달랐기 때문으로 생각됨.

<br>

#### hibernate dialect 로그가 뜨지 않음
- hibernate 6.x부터 내부적으로 dialect 자동 감지해도 로그출력 하지 않는 방향으로 바뀐 것으로 보임

<br>

#### 테스트 코드를 돌릴 때 발생한 에러
>Caused by: java.lang.IllegalArgumentException: 'script' must not be null or empty
at org.springframework.util.Assert.hasText(Assert.java:253) ~[spring-core-6.2.9.jar:6.2.9]
at org.springframework.jdbc.datasource.init.ScriptUtils.splitSqlScript(ScriptUtils.java:475) ~[spring-jdbc-6.2.9.jar:6.2.9]
at org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(ScriptUtils.java:252) ~[spring-jdbc-6.2.9.jar:6.2.9]
... 35 common frames omitted
- script 라는게 뭔지 궁금했음
- 결론적으로 이건 데이터베이스 컬럼과는 전혀 관련이 없고, Spring Boot의 SQL 초기화 스크립트 관련 오류임
- `ScriptUtils.executeSqlScript`에서 발생, Spring Boot가 SQL 스크립트 파일을 실행하려고 하는데 빈 파일이나 null 스크립트를 만났을 때 발생
- yaml 파일에서 spring: sql: init: mode: 가 always로 되어 있으면, 이건 `항상 SQL 초기화 스크립트를 실행하라`는 것임
    - mode를 never로 하거나 data.sql에 주석이더라도 한 줄을 넣어두면 오류 발생하지 않음
---
### @Table에 대하여
#### java 파일을 entity로 인식하게 하기 위해 @Table annotation을 썼는데,<br>강의영상에서는 javax.persistence로 import되나 나에게는 jakarta.persistence로 import됨
- spring boot 3.x부터 jakarta EE 9+을 기반으로 하여 변경됨
---
### @Transient?
####  `따로 transient를 넣지 않은 이상 나머지들은 column으로 본다`는 말이 있었다.
>  @Setter @Column(nullable = false) private String title; // 제목: not null<br>
> @Setter @Column(nullable = false) private String content; // 본문<br>
> @Setter private String hashtag; // 해시태그: optional field로, null허용<br>
> @CreatedDate private LocalDateTime createdAt; // 생성일시<br>
- 이런 식으로 entity에서 필드를 column화하고 있을 때,
- `Transient` 키워드를 붙이면 해당 필드는 DB테이블 컬럼에 포함하지 않고 매핑에서 제외됨
---
### JPA entity는 hibernate 구현체를 사용, 기본생성자를 가지고 있어야 한다
````java
protected Article() { }

private Article(String title, String content, String hashtag) {
    this.title = title; 
    this.content = content; 
    this.hashtag = hashtag; 
}

public static Article of(String title, String content, String hashtag) {
    return new Article(title, content, hashtag);
}
````
#### 평소에 오픈할 일이 없기에 protected로 선언해 코드 밖에서 new로 생성할 수 없게 함<br>위의 코드에서 Article of이 뭔지 궁금했다.
- 이는 factory method 패턴 중 하나로, 클래스 내부에서 객체생성을 담당하는 정적 메서드
- 외부에서는 이 메서드를 통해야만 객체를 만들 수 있음
- Article.of 하면 내부적으로 private 생성자를 호출하여 객체를 생성함



<br>

#### 이렇게 쓰는 이유
> #### 생성과정 제어
> - 생성자 대신 메서드를 통해 객체 생성 시 더 복잡한 로직을 넣거나
> - 필요한 검증 또는 추가 작업을 넣기 좋음
> #### 가독성
> - Article.of이 '이 데이터로 Article객체 만든다'는 의미를 명확하게함
> #### 불변 객체 패턴 등
> - 생성자 숨기고 팩토리 메서드로 관리하여 객체 생성 통제

<br>

#### hibernate구현체와 빈 constructor
- hibernate(=JPA 구현체)는 reflection API를 사용해 엔티티 객체를 생성하는데 이때 `no-args constructor`가 반드시 필요함
- hibernate는 DB에서 데이터를 조회할때 `new Article()`처럼 인자 없이 객체를 만들고 그 후에 필드값을 채워넣는 방식이기에
- 기본 생성자가 없으면 오류 발생함

<br>

#### hibernate는?
- 자바 ORM(Object-Relational Mapping) 프레임워크
- 관계형 데이터베이스(RDBMS) 등과 연결할 때 쓰는 도구
  - 나는 이전에 JDBC랑 Mybatis를 사용했었음
    - JDBC: 가장 기본적인 자바 API로 DB에 직접 SQL 쿼리를 보내고 결과를 받는 통신 방법<br>
      모든 DB작업을 직접 SQL로 작성하고 실행 결과도 수동으로 처리
    - MyBatis: JDBC우에 만들어진 SQL 매핑 프레임워크 <br>
      SQL쿼리는 직접 작성하지만 쿼리결과를 자바 객체에 자동으로 매핑해 줌<br>
      XML이나 어노테이션으로 SQL을 관리할 수 있고, 쿼리 재사용성을 높임
- #### Hibernate 가 하는 역할
  - 자바 객체를 DB 테이블과 자동으로 연결해 줌(ORM)
  - SQL 쿼리를 자동 생성, 실행
  - DB 데이터 변경 감지 및 자동 업데이트
  - 트랜잭션 관리 지원
  - 객체 간 관계 관리 (1:N, N:M 등)
  - 개발자가 SQL, JDBC 코드 직접 작성하는 수고를 크게 줄임

<br>

#### 그럼 왜 JPA가 혼자 뭘 할 수는 없고 hibernate가 필요한가
- JPA = "규격(spec)"
  - "엔티티를 이렇게 매핑해라", "이런 메서드를 제공해라" 같은 *인터페이스*만 정의함
  - JPA자체로는 아무것도 못함. 실행되는 코드가 아님
- Hibernate = "구현체"
  - JPA 규격을 **실제로 동작**하게 만들어주는 라이브러리
  - 예: `@Entity`, `@ManyToOne` 같은 annotation을 보고 실제 SQL쿼리를 생성하고 DB와 주고받는 역할
  
---
### equals and hashcode
#### nonnull?
- intelliJ 이전 설정에서는 nonnull 필드를 체크하라는 옵션이 있었던 것 같음
- equals and hashcode를 만들 필드가 확실히 nonnull일 경우,<br>
return 부분이 `return id.equals(article.id)`처럼 아예 해당 필드로 비교하게 만들어졌음
- 그러나 현재 내가 사용중인 버전에서는 nonnull을 체크하는 부분이 없고, `return Objects.equals(id, article.id)`로 나타남

<br>

#### equals and hashcode를 만드는 두가지 형태<br>`instanceof` vs `getClass()`
- instanceof
  - 하위 타입도 인정하는 넓은 비교 방식
  - 상속관계 비교시 좀 더 유연함
  - java16이상부터는 `if (!(o instanceof Article article))`처럼 패턴 매칭도 가능해짐
    - 그래서 `if (!(o instanceof Article)) return false; Article article = (Article) o;`을 `if (!(o instanceof Article article)) return false;`로 줄일 수 있음
- getClass()
  - 엄격한 타입비교 방식
  - `this.getClass() != o.getClass()` 처럼 정확히 **같은 클래스 타입**인지를 비교함
  - 최근 intelliJ 기본값
  
<br>

#### 필드가 null일 경우 equalsAndHashcode처리?
> id가 영속화되지 않은 상황,<br>DB와 연결되지 않은 상황에 만들어진 entity는 id가 없기 때문에<br>
> 이 부분 체크를 위해 `id != null && id.equals(article.id)`로 `id != null`을 추가함
- 새로 생성된(영속화 안 된) 엔티티는 보통 id == null
- 두 객체가 모두 새 객체라면, id == null이지만 id.equals()는 호출 불가
- 그래서 id != null 체크 후 비교하고, null이면 동등하지 않다고 판단하는 엄격한 방식을 택한 것
- 만약 새 객체들끼리 동등 비교를 별도로 처리하지 않는다면, 이 방식이 더 안전<br>
**... 이라고 했으나, `insert하기 전에 만든 entity`가 어떻게 있을 수 있는지가 궁금했다.**
    #### insert하기 전에 만든 entity
  - entity는 JPA에서 DB테이블과 매핑되는 자바객체이므로
  - **insert하기 전**이란 그 객체가 아직 DB에 저장(영속화)되지 않은 상태를 말함
    - ex) 개발자가 new Article()로 메모리상에만 존재하는 객체를 만든 상태
    
  #### 그래서 나도 Object.equals앞에 `id != null` 붙임
<br>

#### id로 비교하게 만든 것이 1:N, M:1에서 빛을 발할 것이라는 말
- 게시글과 댓글 연관관계에서 댓글은 보통 게시글을 참조하는 필드를 가짐
- 댓글이 여러 개 모인 컬렉션에서 댓글의 동등성 판단 시, 게시글과 연결된 id가 중요해짐
- 게시글의 equals/hashCode가 id 기준으로 잘 구현돼 있으면,
  - 댓글이 어떤 게시글에 속하는지 명확히 구분 가능
  - 컬렉션 내 중복 제거, 변경 추적이 정확해짐
---
### JPA 에서 @ManyToOne

> #### 1. @ManyToOne 어노테이션과 JPA 역할
> -   @ManyToOne 같은 어노테이션은 JPA가 런타임 시점에 리플렉션으로 읽어서 
> - 해당 필드가 다른 Entity와 어떤 관계인지, 어떻게 DB에 매핑할지 처리해 줌
> - 즉, JPA가 객체와 DB 테이블을 연결해주는 ORM(Object-Relational Mapping) 역할
> #### 2. 만약 JPA를 사용하지 않는다면?
> -   순수 자바 클래스에서 @ManyToOne 같은 건 필요도 없고 쓸 수도 없음.
> - 대신 모든 연관관계 관리나 데이터베이스 작업을 직접 코드로 작성해야 한다.
> -   예를 들어, Article과 ArticleComment가 서로 참조하는 객체를 수동으로 연결하고,
> - DB 쿼리도 직접 작성하고 결과를 자바 객체에 매핑하는 등 훨씬 번거롭고, 코드도 길고 복잡해질 수밖에 없음

<br>

#### JPA 없는 구현 예시
````java
class Article {

// 댓글 목록 직접 관리
    private List<ArticleComment> comments = new ArrayList<>();
    
// 댓글 추가 메서드 (양방향 연관관계 수동 관리)
    public void addComment(ArticleComment comment) {
        comments.add(comment);
        comment.setArticle(this);  // 댓글 객체에 article 참조 연결
    }

}

class ArticleComment {

// Article setter (연관관계 수동 설정)
    public void setArticle(Article article) {
        this.article = article;
    }

}

public class Main {
    public static void main(String[] args) {

        // 수동으로 양방향 연관관계 설정
        article.addComment(comment1);
        article.addComment(comment2);

        // 댓글에서 게시글 접근 가능
        System.out.println(comment1.getArticle().getTitle()); // "제목"

        // 게시글에서 댓글들 접근 가능
        for (ArticleComment comment : article.getComments()) {
            System.out.println(comment.getContent());
        }
    }
}
````
- 이걸 보고 좀 당황함
- 왜냐면 이건 항상 내가 하던 짓이기 때문..

#### 그럼 JPA를 쓰면?
````java
public class Article {
    @OrderBy("id")
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    private final Set<ArticleComment> articleComments = new LinkedHashSet<>();
}

public class ArticleComment {
   @Setter @ManyToOne(optional = false) private Article article; // 게시글 ID
}
````
- 그냥 이게 다냐고.. 대박사건임
#### 또 놀라운 것들
- 업데이트 자동화(?)
  - `tableA`와 `tableB`, `tableC`가 모두  entity로 선언되어 있고
  - 연관관계 (`@ManyToOne`, `@OneToMany`...)가 잘 매핑되어 있고
  - 영속성 컨텍스트(`EntityManager` 또는 `Spring Data JPA`의 `Repository`)가 그 객체들을 관리하는 상태라면
  - `tableA`를 update할 때 `EntityManager`가 변경 사항을 감지하여 `tableB, C`의 update쿼리도 실행해준다.
  - **그런데 이건 실무에서는 문제가 될수 있어서 안쓰는 경우가 있다고 함. comment-notes의 `@onetomany`참고**

- 레거시프로젝트에서는 nullable하지 않은 컬럼인데 값이 들어오지 않거나 length가 맞지 않아 오류가 나면, DB차원의 문제인지를 알아채기 위해 로그나 다른 방법을 쓸수밖에 없었다.
  - hibernate/JPA를 통해 Java 엔티티 클래스에 `@Column(nullable = false, length = 50)` 같은 제약 조건을 직접 설정
  - DB구조를 보지 않아도 java코드만으로 데이터 제약 조건 파악이 가능
- JPA쓰면 복잡한 쿼리가 어렵다?
  - JPQL(HQL) 문법은 ANSI SQL의 모든 기능을 제공하지는 않기 때문에 이런 오해가 있을 수 있음
  - JPA는 엔티티 중심이기에 DB테이블구조와 완전히 다른형태로 데이터를 가져오기에는 DTO매핑이 번거로움
  - 단순히 연관관계 fetch join으로 해결할 수 없는 N + 1문제나 특정 DB의 고유 기능을 쓰려면 네이티브 SQL로 돌아가야 함
  - #### 대안이 있다?
    #### 복잡한 쿼리 → QueryDSL 사용
      - 객체지향 스타일로 타입 안정성을 유지하면서 동적 쿼리 작성 가능
    #### 정말 복잡한 DB 전용 기능 → Native Query
      - JPA에서도 @Query(nativeQuery = true)로 순수 SQL 작성 가능
    #### 하이브리드 접근
      - CRUD, 단순 조회 → JPA/Hibernate
      - 리포트성 쿼리, 복잡한 집계 → MyBatis나 Native SQL 같이 사용

---
### @Data혹은 @Value써서 DTO만드는 대신 record를 쓴다는 것
#### `@Data` & `@Value`
- 이전 lombok에서는 전자를 붙였으나 이후에는 후자가 붙는 것으로 추정됨
- @Data에는 setter가 있어 불변이 아니지만, @Value는 모든 필드가 private final이고 getter만 생성되어 불변 객체임
#### 예전 DTO구조와 record
##### 예전 DTO 구조
```java
@Data // 롬복
public class ArticleDto {
    private final String title;
    private final String content;
}
```

- 필드, 생성자, getter, equals/hashCode, toString 등을 롬복이 자동 생성.<br>
하지만 이 방식은 롬복에 의존해야 하고, 불변성 보장을 위해 final 붙이는 것도 신경써야 함.
<br>
##### record
```java
public record ArticleDto(String title, String content) {}
```

- title, content는 자동으로 final이고, setter 없음 → 불변 객체.<br>
equals, hashCode, toString 자동 생성.<br>
롬복 없이 순수 자바 기능만으로 간결하게 불변 DTO 생성 가능.

<br>

#### 불변 객체인데 set이 필요하면?
- 불변 객체는 필드 값을 바꾸는 게 아니라, 값이 바뀐 새로운 객체를 만드는 방식으로 처리

```java
public record ArticleDto(String title, String content) {}

// 값 바꾸기
ArticleDto dto1 = new ArticleDto("제목1", "내용1");
ArticleDto dto2 = new ArticleDto("제목2", dto1.content()); // 새로운 객체 생성
```
- 즉, **"수정"이 아니라 "새로 생성"**이 핵심.
- 장점: 데이터 변경 추적이 쉽고, 멀티스레드 환경에서도 안전.
- @Value 써도 괜찮음<br>
단, record는 Java 표준 기능이라 Lombok 의존성이 줄고 문법이 더 간결.

<br>

#### 내가 레거시에서 썼던 구조는 Java Bean패턴임
- 과거 자바에서 표준처럼 쓰였던 방식으로,
- 모든 필드는 private
- 기본 생성자(빈 생성자) 존재
- getter/setter 메서드 존재
- 2014년 출시된 java8에서는 record가 아직 없어 javaBean을 쓰는 것이 최선이었음

##### `javaBean` -> `Lombok @Value` -> `record`
```java
// 1. JavaBean (DTO 스타일)
public class ArticleDto {
    private String title;
    private String content;

    public ArticleDto() {} // 기본 생성자

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

// 2. Lombok @Value (불변 DTO)
import lombok.Value;

@Value
public class ArticleDto {
    String title;
    String content;
}
// 모든 필드 final, getter 자동, equals/hashCode/toString 자동
// 생성 시점 이후 값 변경 불가

// 3. record (Java 16+ 불변 DTO)
public record ArticleDto(String title, String content) {}
// 필드 final, getter 역할의 메서드 자동, equals/hashCode/toString 자동
// 가장 간결, Lombok 불필요
```

  ##### `javaBean` -> `Lombok @Value` -> `record`
  ```java
  // 1. JavaBean (DTO 스타일)
  public class ArticleDto {
      private String title;
      private String content;

      public ArticleDto() {} // 기본 생성자

      public String getTitle() { return title; }
      public void setTitle(String title) { this.title = title; }

      public String getContent() { return content; }
      public void setContent(String content) { this.content = content; }
  }

  // 2. Lombok @Value (불변 DTO)
  import lombok.Value;

  @Value
  public class ArticleDto {
      String title;
      String content;
  }
  // 모든 필드 final, getter 자동, equals/hashCode/toString 자동
  // 생성 시점 이후 값 변경 불가

  // 3. record (Java 16+ 불변 DTO)
  public record ArticleDto(String title, String content) {}
  // 필드 final, getter 역할의 메서드 자동, equals/hashCode/toString 자동
  // 가장 간결, Lombok 불필요
  ```

---
### 의존성 주입이 없던 때의 mvc패턴?
- 의존성 주입이 그렇게도 중요하다면 그 전에는 mvc패턴을 어떻게 구현했는지 궁금했다.
- 그 전에는 DI 없이 `new`로 연결을 했는데, 이것은 결합도가 높다는 문제가 있음.

#### `@Resource` vs `@Autowired`
- 내가 하던 프로젝트 중 `@Resource(name = ...)` 을 사용한 경우가 있는데 이건 뭔가 또 궁금해졌다.
- Java표준, 이름 기준(`@Autowired`는 Spring전용, 타입 기준)
- 따라서 name에 적어준 이름을 우선으로 본다.

#### `@Autowired`의 경우 DB연결 등을 Spring이 Bean으로 생성 후 주입한다는데 이건 또 무슨 말인지 궁금했음
- 나는 이전 프로젝트들에서 xml파일을 이용해 DB 설정을 했다.
- spring이전의 MVC방식을 보면...
  ```java
  // Controller
  public class MemberController {
    private MemberService service;

    public MemberController() {
        SqlSessionFactory factory = MyBatisUtil.getSqlSessionFactory();
        MemberDAO dao = new MemberDAO(factory);
        this.service = new MemberService(dao);
    }
  }
  ```
- 이렇게 service가 이용할 것까지 controller에서 관리를 해야한다는 것이다.
- **DI의 핵심:** DAO가 어떤 방식으로 SQL을 실행하는지 Service는 몰라도 된다

  #### 근데 나는 sqlSessionFactory 따로 부르지 않았는데?
  - sqlSessionFactory는 Mybatis에서 SQL을 실행하는 통로임
  - 이게 없으면 Mybatis가 xml에 선언한 SQL을 실행할 방법이 없음.
  - (어쨌든 spring + myBatis가 이부분을 처리해주고 있었다는 뜻임)
    - `context-datasource.xml` + `mybatis-config.xml`(myBatis 설정) + DAO의 `@Resource`를 통해서

#### Spring없이 java EE환경에서 @Resource 쓰려면?
- 톰캣같은 서버에서 JNDI 리소스 주입
  - 1. tomcat 설정 (context.xml)
  ```xml
  <Resource name="jdbc/mydb" 
          auth="Container"
          type="javax.sql.DataSource"
          maxTotal="20" 
          maxIdle="10" 
          username="dbuser"
          password="dbpass"
          driverClassName="com.mysql.cj.jdbc.Driver"
          url="jdbc:mysql://localhost:3306/testdb"/>
  ```
  - 2. java에서 주입받기(@Resource)
  ```java
  @Repository
  public class MyDao {
      @Resource(name="jdbc/mydb")
      private DataSource dataSource;
  }
  ```
- 1번이 뭔가 익숙하다.. 기존 프로젝트에서 context-datasource를 썼기 때문이다.
- spring boot이후로는 같은 설정을 모두 java config + `application.properties` 에서 처리한다고 함.
- 그래도 왜 spring이 개발자들의 봄이라 불렸는지는 이해할 수 있었다
<br>
<br>





---

## 2025-08-15

### 테스트하는 방법(?)
#### select
- 그냥 List로 가져와서 그 개수를 셌음
#### insert
- previousCount를 구하고, 새로 하나를 Article.of으로 save한 다음 previousCount+1의 개수인지를 셌음
#### update
- findById해서 update될 record(?)를 하나 고르고
- update할부분은 set...로(`@Setter`붙였기에 가능) 취해와서 set한다음
- repository에 다시 save한다.
- 그런 다음 해당 record가 `hasFieldOrPropertyWithValue`했을때 update된 값을 가지고 있는가 확인한다.
> #### 근데 여기서 특이한 부분이 있었음
> `Tests passed`는 떴으나 update쿼리가 로그에 나오지 않은것.<br>
> slice 테스트를 돌릴 때 Test안에 있는 모든 test 메서드들은 해당 Test 클래스 위에 걸린 `@DataJpaTest`로 인해 transactional이 걸려 있다.<br>
> transaction은 기본 동작이 rollback인데, 이에 따라 변경점이 중요하지 않다고 판단되면 동작이 생략되기도 함.
> 그냥 save한 다음 끝나버리기 때문에 update가 로그에 안찍힘.<br>
> 이때는 save한 다음 flush를 하거나 `saveAndFlush`를 이용.
#### delete
- delete의 경우 조금 복잡할수 있음. update때처럼 findById에서 record를 하나 고르지만,
- 지우는 것이기 때문에 해당 entity의 count도 구해놔야 함
- 그런데 양방향 바인딩으로 cascade 되어있으므로 해당 entity와 매핑된 entity의 count도 구해야함
- 이경우, 해당 하위 entity는 @manytoOne이기 때문에 '없어질'개수가 하나가 아니다.
- 그러므로 '없어질'개수도 따로 구하는데 이게 내기준엔 좀 신기했다.
-`article.getArticleComments().size()`로 구해진다(왜냐면 article entity안에서 set만들어서 해당 articleComment entity랑 연관관계 형성했기 때문에..)
- delete한 다음에 article은 개수가 하나 줄었나 보고, articleComment는 구한 size만큼 줄었나 보면 된다.
> 이때 분명 cascade로 이어진 article_comment에서도 삭제가 발생해야하는데 로그에 뜨지 않은 issue가 있어서 알아보니 강의 github에 올라온 data.sql에 적용된 컬럼이 지금 내가 실습하는 시점과 달라서 데이터를 일부만 가져왔는데, 그때 comments 데이터는 안가져왔기 때문이었다. 그래서 가공해서 가져옴.<br>
해당 data.sql의 history를 보면 되는 것이었는데 감안을 못했다..

---

### yaml에서 datasource설정
```yaml
spring:
  config:
    activate:
      on-profile: testdb

  datasource:
    url: jdbc:h2:mem:board;mode=mysql
    driver-class-name: org.h2.Driver
  sql.init.mode: always
  # test.database.replace: none
  ```
  이전에 datasource 부분을 주석처리 했었는데 오늘에야 설명을 들음.
  - H2에서 제공하는 호환성 옵션 중의 하나라고 함. mode를 통해 다양한 DB를 사용할 수 있다고..
    > #### jdbc:h2:mem:board → 메모리 H2 DB를 생성 (DB 이름: board)
    > mode=mysql → H2 DB를 MySQL 호환 모드로 동작시키겠다는 뜻<br>
    > 예: MySQL의 문법과 최대한 비슷하게 동작하도록 SQL 파서를 변경<br>
    > MySQL처럼 AUTO_INCREMENT 처리, 문자열 비교 방식, 함수 호환 등 일부 차이를 맞춰줌<br>
    > 이유: 실제 운영 DB가 MySQL이면, H2에서 테스트할 때 SQL 호환성 문제를 줄이기 위해
  - 그런데 이런 설정을 해도 Test 클래스에서 `@ActiveProfiles("testdb")`이렇게 해주어야 한다.
  - 근데 이렇게 해도 test용 DB가 자기가 지정한 DB를 띄워버리기 때문에 자동으로 testDB를 띄우지 못하게 막아줘야함.
  - 이럴 때는 `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`(기본값은 ANY)를 테스트클래스 위에 올려주면 됨.
    - 근데 이 테스트에만 적용할 거면 이걸 붙이는게 맞지만 다른 모든 테스트에 설정하고 싶으면 위 yaml에서 `test.database.replace: none` 이부분을 살려주면 되는 것이다.

---

### Repository에 `@Repository`를 붙이지 마라?
#### 스테레오 annotation은 구현 클래스에 붙이지 인터페이스에 붙이지 않는다
- @Component, @Service, @Repository, @Controller 등을 스테레오타입 애노테이션이라 부른다.
  - 1. Spring이 Bean으로 등록하도록 표시
  - 2. 역할에 맞게 의미부여(@Service는 서비스계층, @Repository는 DB계층...)
- 예를 들어 @Service는 구현 클래스인 ServiceImpl에 붙는 것이지 Service에 붙는게 아니다.

  -  **1. Spring이 Bean을 생성하는 대상은 클래스이기 때문**<br>인터페이스에는 실제 인스턴스가 존재하지 않음 → Bean 등록 불가

  - **2. DI(Dependency Injection) 시 Spring은 구현체를 찾아서 주입**<br>예: @Autowired UserService userService; → Spring이 UserServiceImpl을 찾아 주입
  - **3. 인터페이스에 붙이면 아무 효과 없음**<br>
인터페이스는 Bean 등록이 안 되니까, 스캔해도 Spring이 무시함

- 그럼 Repository에 안 붙인 annotation은 어디로 갔냐?
  - `extends JpaRepository` 했기때문에 runTime시에 Spring Data JPA가 구현체를 자동생성함. 그래서 안붙여도 된다.
- 그럼 어떨때 붙여야 하냐
  - 직접 구현한 클래스에 붙일 때
  ```java
  @Repository
  public class CustomArticleRepositoryImpl implements CustomArticleRepository {
      // 구현
  }
  ```
  - 직접 Bean으로 등록하려는 경우에 붙임

---

### H2 Console보기
- yaml에서 기본적으로 `spring: h2: console: enabled:`을 `false`로 해 두는데 이걸 `true`로 하고,
- `spring: datasource`에 `url: jdbc:h2:mem:testdb`, `username: sa`, `driver-class-name: org.h2.Driver`를 넣어준 다음 기존에 mySql로 연결하기 위한 설정을 주석처리한 다음 spring boot 프로젝트를 Run한다.
 - run console에서 `H2 console available at '/h2-console'. Database available at 'jdbc:h2:mem:testdb'` 이걸 확인한다.
 - localhost:8080/h2-console로 들어간다.
 - 그럼 H2 console을 볼 수 있다!
 - 중요한 건 여기서 JDBC URL에 아까 설정해 준 URL을 넣어야 한다는 것이다. 기본으로 `~/test`같은 게 들어있을 수 있는데 그러면 파일시스템에서 DB를 찾으려하기 때문에 connect가 안될 수 있다.
 - password설정은 따로 안 해줬으니 그냥 바로 connect누르면 H2 DB확인이 가능하다!
 #### 근데 이걸 왜 봐야 하지? 궁금했다
어차피 test돌리면 실 DB에 영향도 안 가고(rollback되니까) 그렇게 하면 될 거 같은데 굳이 H2로 확인할 일이 있나 싶었음
- **테스트 데이터 검증**<br>data.sql이나 JPA @Entity 설정이 잘 먹혔는지 확인
- **문제 디버깅**<br>테스트코드 돌렸는데 원하는 데이터 안나올 때, DB에 실제로 뭐가 있는지 확인

이라고 하는데 이 외에도 여러 이유가 있었으나 역시 테스트데이터 검증시에 쓴다는게 가장 실무와 맞는 설명 같았음..


## 2025-08-16
### 엔티티에서 공통되는 필드를 빼내기
- `mappedSuperClass` & `embedded`
- embedded같은 경우 class AAA와 같이 새로운 class를 해당 엔티티 안에(?) 만든 다음 `@Embedded AAA aa;` 이런 식으로 써서 사용한다(정확한 확인 필요)
- mappedSuperclass로 아예 새로운 클래스 만들어서 분리를 했다면, 내 실습 프로젝트의 경우 중복되는 필드들은 메타데이터이고, jpa auditing으로 생성되는 것이기 때문에 jpa관련된 annotation인 `@EntityListeners(AuditingEntityListener.class)`도 새로 만든 클래스로 빼낼 수 있다.
- 이때 @ToString은 넣어주지만 `@Setter`는 당연히 안 넣는다.
- 중복되는 필드 빼내고 `@Column`에 `updatable=false`같은거 넣어주고.. `@Table`로 index 만드는 건 애석하지만(?) 새로 만든 클래스로 옮기기만 한다고 되는 것은 아니라고 함.

  #### 그럼 이렇게 옮긴 field들은 어떻게 원래 entity랑 연결하나?
  - extends 로 연결한다.(상속)
  - 부모 클래스 자체는 엔티티가 아니지만, 부모클래스의 필드가 자식엔티티 테이블의 컬럼으로 그대로 매핑됨.

  #### 이때 @embedded 는?
  - embedded로 공통필드를 뺐을 때는 이게 필드처럼 취급되기 때문에 그 클래스명이 중요해짐.
  - 엔티티 안에 해당 값타입이 내장되기때문에 이것 또한 영속성 컨텍스트의 관리하에 있음
  - 이렇게 한번 더 거치느니 그냥 상속을 이용하는 mappedsuperclass를 쓴 듯

    #### 영속성 컨텍스트
    - JPA가 엔티티 객체(ex. `new Member()`)를 데이터베이스 테이블의 레코드와 1:1로 매핑해서 관리하는 공간
    - 영속성 컨텍스트는 ID기준으로 엔티티를 캐싱함

- 근데 무조건 이렇게 하는게 맞는 건 아니고, 팀 내에서 혹은 개인 판단 하에서 하는 것임
- 무조건 필드를 빼내면 바로 눈에 안들어올 수 있기때문이다.
- 어쨌든 나는 이 필드들을 `auditingfields`라는 클래스로 따로 빼냄.
  
---

### Spring data rest
- api를 만들기 위해 이용할 기능
- spring data안의 구독 기능임
- 엔티티클래스와 레포지토리로 restful api를 만들수 있다.
- start.spring.io에서 dependency로 검색하면 `rest repositories`가 나오는데 이것임
- 일단은 `hal explorer`도 같이 볼것임

  #### yaml에도 추가할 것이 생겼다
  - spring하에 `data.rest`추가하고 `base-path: /api`, `detection-strategy: annotated`(기본은 default)로 함
  - annotated로 한다는 건 annotation 지정한 것들만 rest api로 노출한다는 것
  
  #### 그런 다음에 각 repository에 annotation붙여줌
  - **@Repository 붙이는 거 아니다**
  - `@RepositoryRestResource` 붙여줌

  #### Hal explorer는 뭔데?
  - data rest관련해서 시각적으로 편하게 볼 수 있다고 한다
  - 검색을 해보니까 postman같은게 없어도 된다고 하는데?(대박!)
  - 아까 `data.rest`의 endpoint를 `/api`로 잡았으니 localhost:8080/api로 들어가면 hal explorer로 화면을 볼 수가 있다
  - 여기서 실행가능한 data rest로 구성된 화면을 볼 수 있고 get으로 조회해보면 정보가 나옴. 근데 특이한게 이때 content type이 json아니고 `hal+json`임
  - `_links`라는 링크도 body에 붙어있는데 이건 restful규약 중 레벨2 `hateoas`라고한다.
    - hateoas: REST Api를 사용하는 클라이언트가 전적으로 서버와 동적인 상호작용이 가능하도록 하는 것
  - 여튼 각 조회된 원소를 클릭해 거기에 연관된 정보도 볼 수 있다!(ex. 게시글 - 게시글에 달린 댓글)

- 이제 api형태를 다 봤으니까 프젝으로 돌아와 test에 필요한 클래스를 만들고 `@WebMvcTest`를 붙인 다음에 mockMvc로 mvc만들어서 `mvc.perform(get("/api/..."))` 이런 식으로 적어준 다음 `andExpect(...)`를 적어줬는데 404에러가 나옴
- 왜냐하면 @WebMvcTest는 슬라이스테스트라 컨트롤러외의 bean은 로드하지 않기에 data rest의 auto configuration을 로드하지 않은것임
- 이것 때문에 테스트를 `integration test`로 작성을 해야함

  #### integration test
  - 이걸 위해서 `@webmvcTest`대신 `@SpringBootTest`를 달아줬는데 `webenvirionment=none`뭐 이런걸 써서 경량화하고 싶지만 mockMvc를 써야 해서 그럴 수 없다고 했다.
    - Spring Boot 2.x 이상에서 @SpringBootTest는 전체 애플리케이션 컨텍스트를 띄움 → 테스트 속도가 느림<br>일부 옵션으로 WebEnvironment.NONE을 지정하면, 서버를 띄우지 않고 컨텍스트만 로드 가능
  - 그래서 mockMvc 인식하라고 `@AutoConfigureMockMvc`도 추가해 줌
  - test잘 통과했으면 이후에 `.andDo(print())`해서 나온 값도 콘솔에서 볼 수 있다
  - 근데 이거는 integration 테스트라 hibernate로그까지 같이나온다. DB에까지 영향을 준다는거. 그래서 `@transactional`을 붙였으면 좋겠다고함(import 할때 `spring`걸로 해야함). 이렇게 해야 rollback이 되니까요
- 해당 클래스는 spring data rest에서 제공하는 것이고, 통합 테스트로 너무 무겁기 때문에 @Disabled처리함

---

### Query DSL
- 우리 API에는 검색기능이 없어서 queryDSL을 사용하려함
- spring initializr 에서 지원하고 있지 않음
- gradle의 경우 plugin 등을 통해 적은 줄수로 사용 가능하나 비추천<br>
 `이유: 활발하게 쓰이는 plugin도 업데이트가 몇년 전임..(ewerk)`
- build.gradle에 넣을때 `dependencyManagement.importedProperties`라는 변수를 넣었는데 `dependencyManagement`에서 관리가 안되어 수동으로 버전을 가져온것이라고함. 이렇게하면 `JPA core collections dependency`와 동일하게 들어감 `TODO: 찾아보기`
- 버전이 안나오면 Noclassdeffounderror가 떠서 실용적으로 더 넣은게 있다고하나 나는 claude의 도움을 받아 spring 3.5.4에 맞게 함..
- generated라는 변수를 만들어서 이 이름으로 src/main안에 폴더를 만들게하는 부분도 있다.
  - QueryDSL이 만드는 클래스들을 Qclass라고함
  - qclass가 기본적으로는 build 디렉토리에 들어가는데 이렇게 되면 gradle빌드시 gradle빌드가 스캔하는 영역과 intelliJ IDEA가 스캔하는 영역이랑 달라서, intelliJ 빌드를 하면 gradle빌드 영역과 intelliJ빌드 영역 두번 불러서 중복 충돌 문제가 나므로 강제로 위치를 옮긴거라고함.
  - 그래서 위치를 옮기면 gradle빌드시와 intelliJ빌드시 동작이같아진다고함
  - `TODO: 이거 전에 내장tomcat만들때도 겪은문제라 그때도 해결할수 있는 문제였는지 확인해보기`
  - 근데 생각해보니까 어제 나한테도 main안에 `generated`라는 폴더가 생겨서 알아본 기억이 있는데.. 그때는 lombok때문인줄 알았다. 딱히 설정을 해둔것도 없었고. 희한하네 `TODO`
  #### `queryDSL predicate Executor` & `queryDSL binder customizer`
  - 인자로 그냥 T vs `entityPath`
  - `predicate...`만 넣어도 검색기능은 끝남
  - 근데 대소문자 처리는 안하지만, 부분검색이 안됨.
  - `binder customizer`가 바로 디테일한 처리를 할수있게 도와주는 녀석임
  - `customize`메서드를 override한다음, bindings를 이용하고, 모든 필드에 대한 검색을 원하는건 아니므로 `excludeUnlistedProperties` 설정을 `true`로 해줌(기본값은 false, 모든 필드 다 검색하겠다는것)
  - `include`로 필요한 필드 넣는데, 원래 api설계시에는 id검색도 가능하게하기로 했었으나 이건 인증기능 구현한후에 넣기
  - 또 기본값은 `exact match`이기 때문에 `bindings.bind`에 제목 검색을 `like`로 할 수있게 `root.title`넣어주고 인자는 하나 받는것으로 `first`로 하는데 이때 `SimpleExpression::eq`라는게 있지만 이걸로는 안한다고함 `TODO: 뭐지?`
  - 우리는 결국 `StringExpression::containsIgnoreCase`이걸로 하는데, 이거랑 `StringExpression::likeIgnoreCase`의 차이는, 후자는 쿼리문을 `like ''`로, 전자는 쿼리문을 `like '%%'`로 생성한다는 것이다. 당연히 전자가 우리가 원하는 방식임. 그리고 `ignorecase`덕에 대소문자 구분 안함.
  - 생성일자인 createdAt은 시간이기때문에 `StringExpression`대신 `DateTimeExpression`으로 하는데 이때 `::eq`로 한다. 근데 이러면 시분초를 동일하게 넣어야하기때문에 이 검색방법은 아주 편한방법은 아님, `TODO: 이후에`
  - Qclass파일들은 자동생성되는 것들이라 커밋하지 않을것임. 그래서 generated 디렉토리자체를 .gitignore에 추가하기로. 이때 루트패스 `/` 넣어서 `/src/main/generated` 이렇게 추가해줌


## 2025-08-17

### 드디어 view 만들기 - controller와 test
#### test만들때 `private final Mockmvc mvc`를 가져와서 생성자 방식으로 주입하는데..
- 이때 autowired생략할수 없다고 하는데 왜지?
- 테스트패키지에 있는것은 autowired가 하나만 있을때 생략할수 없다고 한다. 그래서 생성자의 argument에 `autowired`넣음(일반 클래스에서는 안붙여줘도 붙여준걸로 상정)
  - 일반 클래스에서는 생성자가 하나뿐이면 자동으로 `@Autowired`로 인식함.
  - 그래서 아래의 코드에서는 `@Autowired`가 없는 것임
    #### 1. 일반 클래스에서의 경우
    ```java
    @Service
    public class ArticleService {

        private final ArticleRepository articleRepository;

        // 생성자가 하나뿐이라면 @Autowired 생략 가능
        public ArticleService(ArticleRepository articleRepository) {
            this.articleRepository = articleRepository;
        }
    }
    ```
    - 그러나 테스트에서는 다르다.
    #### 2. 테스트 클래스에서의 경우
    ```java
    @WebMvcTest(ArticleController.class)
    class ArticleControllerTest {

        private final MockMvc mvc;

        // ❌ @Autowired 없으면 에러
        public ArticleControllerTest(MockMvc mvc) {
            this.mvc = mvc;
        }
    }
    ```
- 테스트 미리 예상해서 넣기: 게시글이 나온다는 것은 modelAttribute로 서버에서 게시글을 내려줬다는 건데 그게 있는지? `model().attributeExists("articles")` 로 해당 attribute가 있는지 검사가능
- 이 테스트에 `@WebMvcTest`를 붙이긴했지만 이대로는 모든컨트롤러를 다 읽어들이기때문에 `@WebMvcTest(해당클래스.class)`넣으면 좋음
- 여튼 현재는 껍데기만 만들었기때문에 당연히 테스트통과를 못한다. 근데 이상태에서 gradle build 하면 test도 자동으로 같이 실행하므로 나중에 build조차 안되는 상황이 있을수있음(나도 이런적이 있다). 이래서 커밋의 최소조건으로 test는 꼭 통과하게 만드는 정책도 있다는것
  - 이를 위해 미리 x테스트를 제외시키는 옵션을 넣어 테스트없이 빌드를 하게하거나,
  - 실패하는 테스트 미리 ignore처리하는 방법 등이 있다.
- 테스트시에 해당 핸들러가 보여줄 뷰이름으로 검사하게 할수도 있다. `.andExpect(view().name("..."))`
- 그리고 생각할게 많군.. 게시글 페이지면, 댓글도 보여야 하니까 model.attributeExists할때 댓글도 model에 보내졌는지 확인할수도 있겠군
  #### 근데 매우 크나큰 문제가 발생
  -`@WebMvcTest`에 class 지정을 했는데도, 강의에서는 `content type expected test/html but was text/html;charset=utf-8`라는 에러가 뜨는 반면 나는 아예 404 떠버림
  - 확인해보니 `spring 3.x`부터는 `@webmvcTest`사용할때 `Thymeleaf auto-config`가 제한됨
  - 대신 **`@ImportAutoConfiguration(ThymeleafAutoConfiguration.class)`를 명시** 라고함..
  - 이렇게 해도 안돼서 `@springboottest`로 바꾸고 뭐 아주 쌩난리를 쳤는데 알고보니까 내가 `controller`패키지를 바로 `java`패키지아래 만들어버렸던거였음(바보.. gpt 쥐잡듯이 잡았는데..)
- 마크업을 테스트하는 테스트솔루션(ex. 셀레니움)도 있다고 함( `TODO`)
- mediatype을 철저히 검사할수있기 때문에 그걸로 인한 오류를 막기 위해 `contentTypeCompatibleWith`를 넣음

#### 처음에 추가했던 dependency.. devtools
- 라이브리로드와 같이 쓰면 바로바로 변경점이 저장
- 캐시를 자동으로 삭제
- 등.. (`TODO`..)

### intelliJ 단축키들..
- import 정리하기 (ctrl+alt+o)
- 옵션 두번 추천받기 (ctrl+space)
  - 여기에서 static 으로 받으려면 바로 alt+enter
- ctrl+shift+f10하면 테스트실행
  - 근데 이걸로 view확인하다가 삽질 엄청함.. 일단 여기까지만.
- ctrl+shift+f9하면 recompile

## 2025-08-18
### thymeleaf파일을 html로도 `열 수는` 있다
- decoupled templated logic
- 근데 문제는 이걸 다루는 로직이 spring boot properties에 아직 없다고.
- 이거 설정을 위해 `thymeleafconfig`파일을 만들었는데, SB2와 SB3에서의 설정이 좀 다른것 같다. `TODO: 사용자 커스텀 프로퍼티와 그 세팅에 관해서`
- `build.gradle`에 `spring configuration processor`를 추가했다.
- decoupled logic 적용하다가 `th:block`을 없애고.. 경로 다시 잡고.. 여러 시행착오가 있었다
- 목적은 thymeleaf문법과 HTML 분리.. 인 듯하다.

  #### 근데 이걸 하고나서 서비스가 안돌아가는 문제가 있었다
  - spring boot 3.2부터
      - Java 컴파일러가 기본적으로 매개변수 이름 정보를 바이트코드에 포함하지 않음
      - @ConfigurationProperties의 생성자 바인딩 시 매개변수 이름을 알 수 없어서 실패
      - 특히 record나 @ConstructorBinding 사용 시 빈번히 발생
  - 이런 문제가 있었다고하는데 우리의 thymeleafconfiguration안에
    ```java
    // Spring Boot 3에서는 생성자가 하나면 자동으로 @ConstructorBinding 적용
    public Thymeleaf3Properties(boolean decoupledLogic) {
        this.decoupledLogic = decoupledLogic;
    }
    ```
  - 이런 부분이 있었음.
  - 이걸 해결하려면 settings/java compiler/additional command line parameters들어가서 `-parameters`를 추가해준다.
  - 이 외에 gradle로 build하면 된다는 말도 있었지만 그렇게 했더니 queryDSL의 qClass 관련 에러가 나서 나는 이 방식이 더 나았다고 생각함.

### 새로운 handler method 추가
- map에 addAttribute할때 도메인코드인 Article자체를 넘기진 않을것이고(현재는 테스트목적) null만 넘김
- 근데 이렇게 했더니 테스트 돌릴때 `article`이 없다고 에러남. `article/`로 접속할때 whitelabel 에러 뜨는 것도 같은 이유인 것 같다.

### 따로 merge한 부분이 있었는데
- auditingfields 클래스를 추상클래스로 변경한 부분
  - 엔티티에서 상속하여 사용해야하는 목적에 더 잘 맞도록 `abstract` 키워드를 추가했다라..
- 근데 이거 구현하는 부분은 같이 안 하냐고..? `TODO`

### spring security 적용
- 주의할 점: `spring web`, `spring security`, `thymeleaf` 세 가지 모두를 `start.spring.io`에서 import해서 봐야 실수가 없다고 함
- thymeleaf빼고 두가지만 import해보니까 `  implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'` 이게 빠져있다.
- 문제는 이걸 적용하면 화면에 들어가기 어려워지므로(예전의 나도 겪었던 일..)security Config를 따로 해주기로 한다.
- 근데 이 security config이 `SB2.7`부터 변경이 되었다고함. [참조 spring blog](https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)
- `auto-configuration` 정책 차이도 있고, `WebSecurityConfigurerAdapter`를 이용하던것이 `securityfilterChain`을 이용하는 등 변화가 있었던거 같다.
- 또, `formLogin()`을 인자없이 쓰는게 deprecated되어서 안에 `Customizer.withDefaults()`를 넣어주었다.
- 앗차차 `@Configuration` 을 빼먹음. `@EnableWebSecurity`는 안넣어도 된다는데? (`TODO: 이유 알아보기`)
- 여튼 이걸 적용하면 기존 test들도 실패가 되기때문에 `@Import(SecurityConfig.class)`요걸 넣어주면 된다.

  #### AuthControllerTest를 만들었는데
  - 예전에는 여러 모듈이 합쳐진 상태에서 슬라이스테스트를 하는게 어려운 점이 많았는데 지금은 spring security 적용된 상태에서도 `@WebMvcTest`로 얼마든지 테스트할 수있다고 한다.
    #### 예전에는 어땠길래?
    - spring security는 구조적으로 servlet filter 기반으로 사용자가 url요청하면 dispatcherservlet 가기 전에 securityfilterchain이 먼저 가로챔
      #### 옛날(Spring Boot 1.x~2.3 전후)**
        - @WebMvcTest로 테스트하면 Security까지 같이 로드돼버림
        - MockMvc 요청은 전부 인증 필요 → 401 Unauthorized 나옴<br>
        **해결하려면**
          - @Import(SecurityConfig.class)로 수동 등록
          - @WithMockUser로 직접 인증 사용자 지정
          - 아예 excludeAutoConfiguration = SecurityAutoConfiguration.class로 Security
      #### 지금(Spring Boot 2.7 ~ 3.x, Security 5.7 ~ 6.x)**
        - spring-security-test 라이브러리를 포함하면,
        - MockMvc가 알아서 Security 필터를 인식하고, @WithMockUser 같은 어노테이션을 지원함
        - 그래서 Controller 슬라이스만 가져와도, Security 필터체인이 자동으로 MockMvc에 연결됨
        - 덕분에 인증/인가 테스트도 자연스럽게 같이 가능해짐
  - `/login`으로 갈때 해당 페이지 정상 호출되는지의 테스트인데 viewName은 검사 안함. 왜냐면 얘는 자동생성되는 애라서 (article 부분 검사할때는 ` .andExpect(view().name("articles/index"))` 이렇게 했었음)
  - attribute도 따로 검사 안함

## 2025-08-21
### @ConfigurationPropertiesScan
- 늦게서야 발견했는데 강의(SB2.5.x)의 main class에는 `@ConfigurationPropertiesScan`이게 붙어있었다.
- 확인해보니 thymeleaf config하던 class에 `@ConfigurationProperties`가 있는데, 이게 SB3.x부터 자동으로 스캔이 된다고 했다.
- TODO: `@Configuration` vs `@ConfigurationProperties`

## 2025-08-22
### Spring boot의 슬라이스테스트 기능을 쓰지 않는다는 의미?
- 저번에는 `@WebMvcTest`로 슬라이스테스트를 했는데 이번에는 스프링 부트 애플리케이션이 뜨는 시간을 없애겠다고 한다.
- 대신 디펜던시같은게 필요하면 mocking을 할거고 이를 위해 mockito를 쓰겠다고 했다. 이건 이미 spring test패키지에 포함되어 있어서 따로 설치할 필요는 없다고 한다.
- 테스트 클래스 위에 `@ExtendWith(MockitoExtension.class)`이걸 붙여준다.
- 테스트클래스 말고 원래클래스에는 `@RequiredArgsConstructor`를 붙여서 필수생성자를 자동으로 만들어준다. (`TODO`)
- articleService를 articleServiceTest에서 불러올때 `sut`라는 이름으로 불러왔는데 이는 `System Under Test`라는 의미로 테스트 대상을 말한다.
- 테스트에서 mock을 주입하는 대상은 mockito에서 지원하는 것 중 `@InjectMocks`을 붙이고 나머지는 `@Mock`

## 2025-08-23
### sut테스트시 파라미터가 여러개라면
- DTO로 만들어 관리하는건 어떨지?
- JPA buddy를 이용해 record형태로 DTO를 만들었는데 이때 생성자 만들기를 했더니 compact/canonical 등의 옵션이 있다. (`TODO`)
- 테스트 중 페이지네이션에 대한 테스트가 있는데 이것을 spring framework의 Page를 이용해서 구현하네.. `TODO`
- 페이지네이션 뿐 아니라 정렬 기능도 page 안에 들어있다고 한다.

## serializable
- 우리는 jackson을 직렬화도구로 사용하고 있어서 java에서 제공하는 serializable이 필요없다.
- 그러니 jpa buddy를 쓰고있다면 option들어가서 non-serializable을 체크해주자

## 2025-08-24
### OSIV
- open session in view
