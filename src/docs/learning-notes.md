##  2025-08-14

#### application.properties에서
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
-   ###### 그래서 언제 뭘 써야 하는가?
    - CI/CD환경과 100% 동일하게 검증해야함 -> gradle
    - 로컬 개발 중 반복 테스트 빠르게 해야함 -> intelliJ
---
### yaml설정과 로그
####  hibernate관련 alter table 등의 로그가 나오지 않는 문제가 있었음
>   ###### Spring Boot 3.x Hibernate 로깅 설정<br>
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
<br>
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
- 레거시프로젝트에서는 nullable하지 않은 컬럼인데 값이 들어오지 않거나 length가 맞지 않아 오류가 나면, DB차원의 문제인지를 알아채기 위해 로그나 다른 방법을 쓸수밖에 없었다.
  - hibernate/JPA를 통해 Java 엔티티 클래스에 `@Column(nullable = false, length = 50)` 같은 제약 조건을 직접 설정
  - DB구조를 보지 않아도 java코드만으로 데이터 제약 조건 파악이 가능
- JPA쓰면 복잡한 쿼리가 어렵다?
  - JPQL(HQL) 문법은 ANSI SQL의 모든 기능을 제공하지느 ㅇ낳기 때문에 이런 오해가 있을 수 있음
  - JPA는 엔티티 중심이기에 DB테이블구조와 완전히 다른형태로 데이터를 가져오기에는 DTO매핑이 번거로움
  - 단순히 연관관계 fetch join으로 해결할 수 없는 N + 1문제나 특정 DB의 고유 기능을 쓰려면 네이티브 SQL로 돌아가야 함
  - ##### 대안이 있다?
    ###### 복잡한 쿼리 → QueryDSL 사용
      - 객체지향 스타일로 타입 안정성을 유지하면서 동적 쿼리 작성 가능
    ###### 정말 복잡한 DB 전용 기능 → Native Query
      - JPA에서도 @Query(nativeQuery = true)로 순수 SQL 작성 가능
    ###### 하이브리드 접근
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
