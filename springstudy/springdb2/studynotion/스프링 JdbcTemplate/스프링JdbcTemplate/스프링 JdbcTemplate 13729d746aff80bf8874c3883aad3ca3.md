# 스프링 JdbcTemplate

- JdbcTemplate 소개와 설정
- JdbcTemplate 적용1 - 기본
- JdbcTemplate 적용2 - 동적 쿼리 문제
- JdbcTemplate 적용3 - 구성과 실행
- JdbcTemplate - 이름 지정 파라미터1
- JdbcTemplate - 이름 지정 파라미터2
- JdbcTemplate - 이름 지정 파라미터3
- JdbcTemplate - SimpleJdbcInsert
- JdbcTemplate 기능 정리

## JdbcTemplate 소개와 설정

SQL 을 직접 사용하는 경우에 스프링이 제공하는 JdbcTemplate은 아주 좋은 선택지이다.

JdbcTemplate 은 JDBC 를 매우 편리하게 사용할 수 있게 해준다.

**장점**

- 설정의 편리함
    - JdbcTemplate 은 spring-jdbc 라이브러리에 포함되어 있고, 이 라이브러리는 스프링으로 JDBC를 사용할 때 기본으로 사용되는 라이브러리이다.
- 반복 문제 해결
    - JdbcTemplate은 템플릿 콜백 패턴을 사용해서 JDBC 를 직접 사용할 때 발생하는 대부분의 반복 작업을 대신 처리해준다.
    - 개발자는 SQL을 작성하고, 전달할 파라미터를 정의하고, 응답 값을 패핑하기만 하면 된다.
    - 우리가 생각할 수 있는 대부분의 반복 작업을 대신 처리해준다
        - 커넥션 획득
        - statement 를 준비하고 실행
        - 결과를 반복하도록 루프를 실행
        - 커넥션 종료, statement, resultset 종료
        - 트랜잭션을 다루기 위한 커넥션 동기화
        - 예외 발생 시 스프링 예외 변환기 실행

**단점**

- 동적 SQL 을 해결하기 어렵다.

직접 JdbcTemplate을 설정하고 적용하면서 이해해 보자.

### JdbcTemplate 설정

**build.gradle**

```java
//JdbcTemplate 추가
implementation 'org.springframework.boot:spring-boot-starter-jdbc' //H2 데이터베이스 추가
//H2 데이터베이스 추가
runtimeOnly 'com.h2database:h2'

```

- `org.springframework.boot:spring-boot-starter-jdbc` 를 추가하면 JdbcTemplate이 들어있는

`spring-jdbc` 가 라이브러리에 포함된다.

- 여기서는 H2 데이터베이스에 접속해야 하기 때문에 H2 데이터베이스의 클라이언트 라이브러리(Jdbc Driver) 도 추가하자.

**JdbcTemplateItemRepositoryV1**

```java
package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "insert into item(item_name, price, quantity) values (?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connetion -> {
            //자동 증가 키
            PreparedStatement ps = connetion.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            return ps;
        }, keyHolder);

        long key = keyHolder.getKey().longValue();
        item.setId(key);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=?, price=?, quantity=?, where id=?";
        jdbcTemplate.update(sql,
                updateParam.getItemName(),
                updateParam.getPrice(),
                updateParam.getQuantity(),
                itemId
        );
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id=?";
        try {
            Item item = jdbcTemplate.queryForObject(sql, itemRowMapper(), id);
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();
        String sql = "select id, item_name, price, quantity from item";
        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }
        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',?,'%')";
            param.add(itemName);
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= ?";
            param.add(maxPrice);
        }
        log.info("sql={}", sql);
        return jdbcTemplate.query(sql, itemRowMapper(), param.toArray());
    }

    private static RowMapper<Item> itemRowMapper() {
        return ((rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        });
    }
}

```

→ ItemRepository 인터페이스를 구현함.

## JdbcTemplate 적용2 - 동적 쿼리 문제

결과를 검색하는 findAll() 에서 어려운 부분은 사용자가 검색하는 값에 따라서 실행하는 SQL 이 동적으로 달려져야 한다는 점이다.

- 검색 조건이 없음
- 상품명으로 검색
- 최대 가격으로 검색
- 상품명, 최대 가격 둘다 검색

결과적으로 4가지 상황에 따른 SQL을 동적으로 생성해야 한다.

참고로 이후에 설명할 MyBatis의 가장 큰 장점은 SQL을 직접 사용할 때 동적 쿼리를 쉽게 작성할 수 있다는 점이다.

## JdbcTemplate 적용3 - 구성과 실행

**JdbcTemplateV1Config**

```java
package hello.itemservice.config;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.jdbctemplate.JdbcTemplateItemRepositoryV1;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JdbcTemplateV1Config {
    private final DataSource dataSource;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        // JdbcTemplateItemRepositoryV1을 ItemRepository 빈으로 등록
        return new JdbcTemplateItemRepositoryV1(jdbcTemplate(dataSource));
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

```

→ JdbcTemplate 을 스프링 빈에 등록하여 자동 주입하도록 설정함.

**application.properties**

```java
spring.datasource.url=jdbc:h2:tcp://localhost/~/test
spring.datasource.username=sa
```

h2 DB 내용 추가

**ItemServiceApplication**

```java
//@Import(MemoryConfig.class)
@Import(JdbcTemplateV1Config.class)
```

설정 Config 파일 변경

**실행 결과**

![image.png](%E1%84%89%E1%85%B3%E1%84%91%E1%85%B3%E1%84%85%E1%85%B5%E1%86%BC%20JdbcTemplate%2013729d746aff80bf8874c3883aad3ca3/image.png)

![image.png](%E1%84%89%E1%85%B3%E1%84%91%E1%85%B3%E1%84%85%E1%85%B5%E1%86%BC%20JdbcTemplate%2013729d746aff80bf8874c3883aad3ca3/image%201.png)

- DB에 잘 반영됨을 알 수 있다.

## JdbcTemplate - 이름 지정 파라미터

**순서대로 바인딩**

JdbcTemplate을 기본으로 사용하면 파라미터를 순서대로 바인딩 한다.

```java
String sql = "update item set item_name=?, price=?, quantity=? where id=?";
 template.update(sql,
         itemName,
         price,
         quantity,
         itemId);
```

만약, price 와 quantity 순서를 변경했다면, 올바르지 않은 데이터 순서로 바인딩 되어 db에 저장된다.

**개발을 할 때는 코드를 몇줄 줄이는 편리함도 중요하지만, 모호함을 제거해서 코드를 명확하게 만드는 것이 유지보수 관
점에서 매우 중요하다.**

### 이름 지정 바인딩

JdbcTemplate은 이런 문제를 보완하기 위해 `NamedParameterJdbcTemplate` 라는 이름을 지정해서 파라미터를 바인딩 하는 기능을 제공한다.

**JdbcTemplateItemRepositoryV2**

```java
//save 변경 전
String sql = "insert into item(item_name, price, quantity) values (?,?,?)";

//save 변경 후
@Override
    public Item save(Item item) {
        String sql = "update item " +
                "set item_name=:itemName," +
                "price=:price," +
                "quantity=:quantity " +
                "where id=:id";

        SqlParameterSource param = new BeanPropertySqlParameterSource(item);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, param, keyHolder);
        
        long key = keyHolder.getKey().longValue();
        item.setId(key);
        return item;
    }
    
    //update 변경 전
    String sql = "update item set item_name=?, price=?, quantity=?, where id=?";
		
		//update 변경 후
		@Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item " +
                "set item_name=:itemName, price=:price, quantity=:quantity " +
                "where id=:id";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId); //이 부분이 별도로 필요하다. template.update(sql, param);
        jdbcTemplate.update(sql, param);
    }
    
    // findById 변경 전
    String sql = "select id, item_name, price, quantity from item where id=?";
		
		// findById 변경 후
		
		
		// itemRowMapper() 변경
		private RowMapper<Item> itemRowMapper() {
			return BeanPropertyRowMapper.newInstance(Item.class); //camel 변환 지원
		}
```

- `JdbcTemplateItemRepositoryV2` 는 `ItemRepository` 인터페이스를 구현했다.
- `this.template = new NamedParameterJdbcTemplate(dataSource)`
    - `NamedParameterJdbcTemplate`도 내부에`dataSource` 가 필요하다.
    - `JdbcTemplateItemRepositoryV2` 생성자를 보면 의존관계 주입은 `dataSource`를 받고 내부에서 `NamedParameterJdbcTemplate`을 생성해서 가지고 있다. 스프링에서는 `JdbcTemplate` 관련 기능을 사용할 때 관례상 이 방법을 많이 사용한다.
    - 물론 `NamedParameterJdbcTemplate` 을 스프링 빈으로 직접 등록하고 주입받아도 된다.

## JdbcTemplate - SimpleJdbcInsert

JdbcTemplate은 INSERT SQL를 직접 작성하지 않아도 되도록 `SimpleJdbcInsert` 라는 편리한 기능을 제공한다.

**JdbcTemplateItemRepositoryV3**

```java
package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SimpleJdbcInsert
 */
@Slf4j
@RequiredArgsConstructor
public class JdbcTemplateItemRepositoryV3 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;

    private final SimpleJdbcInsert jdbcInsert;

    @Override
    public Item save(Item item) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        Number key = jdbcInsert.executeAndReturnKey(param);
        item.setId(key.longValue());
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item " +
                "set item_name=:itemName, price=:price, quantity=:quantity " +
                "where id=:id";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId); //이 부분이 별도로 필요하다.

        template.update(sql, param);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id = :id";
        try {
            Map<String, Object> param = Map.of("id", id);
            Item item = template.queryForObject(sql, param, itemRowMapper());
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        SqlParameterSource param = new BeanPropertySqlParameterSource(cond);

        String sql = "select id, item_name, price, quantity from item";
        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',:itemName,'%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= :maxPrice";
        }

        log.info("sql={}", sql);
        return template.query(sql, param, itemRowMapper());
    }

    private RowMapper<Item> itemRowMapper() {
        return BeanPropertyRowMapper.newInstance(Item.class); //camel 변환 지원
    }
}

```

**JdbcTemplateV3Config**

```java
package hello.itemservice.config;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.jdbctemplate.JdbcTemplateItemRepositoryV2;
import hello.itemservice.repository.jdbctemplate.JdbcTemplateItemRepositoryV3;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JdbcTemplateV3Config {
    private final DataSource dataSource;
    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }
    @Bean
    public ItemRepository itemRepository() {
        return new JdbcTemplateItemRepositoryV3(namedParameterJdbcTemplate(), simpleJdbcInsert());
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public SimpleJdbcInsert simpleJdbcInsert() {
        return new SimpleJdbcInsert(dataSource).withTableName("item").usingGeneratedKeyColumns("id");
    }
}
```

- `JdbcTemplateItemRepositoryV3` 는 `ItemRepository` 인테페이스를 구현했다.
- `this.jdbcInsert = new SimpleJdbcInsert(dataSource)` : 생성자를 보면 의존관계 주입은 `dataSource` 를 받고 내부에서 `SimpleJdbcInsert` 을 생성해서 가지고 있다. 스프링에서는 `JdbcTemplate` 관련 기능을 사용할 때 관례상 이 방법을 많이 사용한다.

## JdbcTemplate 기능 정리

JdbcTemplate의 기능을 간단히 정리해보자.

### 주요 기능

JdbcTemplate 이 제공하는 주요 기능은 다음과 같다.

- JdbcTemplate
    - 순서 기반 파라미터 바인딩을 제공한다.
- NamedParameterJdbcTemplate
    - 이름 기반 파라미터 바인딩을 지원한다.
- SimpleJdbcInsert
    - INSERT SQL 을 편리하게 사용할 수 있다.
- SimpleJdbcCall
    - 스토어드 프로시저를 편리하게 호출할 수 있다.

### 조회

**단건 조회 (하나만 조회) - 숫자 조회**

```java
int rowCount = jdbcTemplate.queryForObject("select count(*) from t_actor",
Integer.class);
```

→ `queryForObject` 를 사용하면 된다.

**단건 조회 (하나만 조회) - 숫자 조회, 파라미터 바인딩**

```java
int countOfActorsNamedJoe = jdbcTemplate.queryForObject(
"select count(*) from t_actor where first_name = ?", Integer.class,
"Joe");
```

→ 숫자 하나와 파라미터 바인딩의 예시

**단건 조회 - 문자 조회**

```java
String lastName = jdbcTemplate.queryForObject(
"select last_name from t_actor where id = ?",
String.class, 1212L);
```

→ 문자 하나와 파라미터 바인딩 예시

**단건 조회 - 객체 조회**

```java
@Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id=?";
        try {
            Item item = jdbcTemplate.queryForObject(sql, itemRowMapper(), id);
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

private static RowMapper<Item> itemRowMapper() {
        return ((rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        });
    }
```

→ 객체 하나를 조회한다. 결과를 객체로 매핑해야 하므로 `RowMapper` 를 사용해야 한다. 여기서는 람다를 사용했다.

**목록 조회 - 객체**

여러 로우를 조회 할 때는 query() 를 사용하면 되고, 결과를 리스트로 반환한다.

```java
private final RowMapper<Actor> actorRowMapper = (resultSet, rowNum) -> {
		Actor actor = new Actor();
		actor.setFirstName(resultSet.getString("first_name"));
		actor.setLastName(resultSet.getString("last_name"));
		return actor;
};
public List<Actor> findAllActors() {
		return this.jdbcTemplate.query("select first_name, last_name from t_actor",
				actorRowMapper);
}
```

### 변경(INSERT, UPDATE, DELETE)

데이터를 변경할 때는 jdbcTemplate.update() 를 사용하면 된다.

int 를 반환하는데, SQL 실행 결과에 영향 받은 로우 수를 반환한다.

**등록**

```java
jdbcTemplate.update(
"insert into t_actor (first_name, last_name) values (?, ?)",
"Leonor", "Watling");
```

**수정**

```java
jdbcTemplate.update(
"update t_actor set last_name = ? where id = ?",
"Banjo", 5276L);
```

**삭제**

```java
jdbcTemplate.update(
"delete from t_actor where id = ?",
Long.valueOf(actorId));
```

### 기타 기능

임의의 SQL을 실행할 때는 exeecute() 를 사용하면 된다. 테이블을 생성하는 DDL에 사용할 수 있다.

**DDL**

```java
jdbcTemplate.execute("create table mytable (id integer, name varchar(100))");
```

**스토어드 프로시저 호출**

```java
jdbcTemplate.update(
"call SUPPORT.REFRESH_ACTORS_SUMMARY(?)",
Long.valueOf(unionId));
```

- 실무에서 가장 간단하고 실용적인 방법으로 SQL을 사용하려면 JdbcTemplate을 사용하면 된다.
JPA와 같은 ORM 기술을 사용하면서 동시에 SQL을 직접 작성해야 할 때가 있는데, 그때도 JdbcTemplate을 함께사용하면 된다.
- 그런데 JdbcTemplate의 최대 단점이 있는데, 바로 동적 쿼리 문제를 해결하지 못한다는 점이다. 그리고 SQL을 자바코드로 작성하기 때문에 SQL 라인이 코드를 넘어갈 때 마다 문자 더하기를 해주어야 하는 단점도 있다.
- **동적 쿼리 문제를 해결하면서 동시에 SQL도 편리하게 작성할 수 있게 도와주는 기술이 바로 MyBatis 이다.**