# Servlet - 요청 처리 부분

- 내 패키지를 포함한 하위 패키지의 서블릿을 다 찾아서 자동으로 등록해줌. (main class)

```java
@ServletComponentScan
```

```java
@WebServlet //WebServlet 어노테이션으로 무엇이 어떤 url에 동작할 지 설정해 줌. 
```

- 서블릿 구현 클래스는 HttpServlet 이라는 클래스를 상속받아야 함.

```java
@WebServlet(name = "원하는 이름", urlPatterns = "/원하는 url"
public class HelloServlet extends HttpServlet{
	
}
```

- HelloServlet 내에서 macOS : Control + O

: select methods to override/implement 가능 → protected service 사용.

```java
@Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }
```

- 서블랫으로 쿼리 파라미터 읽기

[http://localhost:8080/hello?username=kim](http://localhost:8080/hello?username=kim)

```java
String username = request.getParameter("username");
```

- 서블랫으로 텍스트 보내기

```java
response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(username);
```

- `main/webapp/index.html`

### HttpServletRequest 객체

- start line
    - http method
    - url
    - query string
    - schema, protocol
- header
    - header 조회
- body
    - form parameter 형식 조회
    - message body 데이터 직접 조회

+

- **임시 저장소 기능**
해당 HTTP 요청이 시작부터 끝날 때 까지 유지되는 임시 저장소 기능

저장: request.setAttribute(name, value)
조회: request.getAttribute(name)

- 람다식으로 헤더 전체 조회하여 출력하기

```java
request.getHeaderNames().asIterator().forEachRemaining(headerName -> System.out.println(headerName + ": " + headerName));
```

### Http 요청 데이터 3가지

- **GET - 쿼리 파라미터 요청**
    - /url**?username=hello&age=20**
- **POST - HTML Form 요청**
    - content-type: application/x-www-form-urlencoded
    - 메시지 바디에 쿼리 파리미터 형식으로 전달 username=hello&age=20
    - 예) 회원 가입, 상품 주문, HTML Form 사용
- **HTTP message body에 데이터를 직접 담아 요청**
    - HTTP API에서 주로 사용
    - 예) JSON, XML, TEXT
    - POST, PUT, PATCH

## GET 쿼리 파라미터로 HTTP 요청 보내기.

[http://localhost:8080/request-param?username=hello&age=20](http://localhost:8080/request-param?username=hello&age=20)
서버에서는 `HttpServletRequest` 가 제공하는 다음 메서드를 통해 쿼리 파라미터를 편리하게 조회할 수 있다.

- 전체 파라미터 조회

```java
request.getParameterNames().asIterator().forEachRemaining(paramName -> System.out.println(paramName + "=" + request.getParameter(paramName)));
```

- 단일 파라미터 조회

```java
System.out.println("username = " + request.getParameter("username"));
System.out.println("age = " + request.getParameter("age"));
```

- 이름이 같은 복수 파라미터 조회

```java
String[] usernames = request.getParameterValues("username");
for (String name : usernames) {
    System.out.println("username = " + name);
}
```

## POST - HTML Form 으로 HTTP 요청 보내기

![image.png](Servlet%20-%20%E1%84%8B%E1%85%AD%E1%84%8E%E1%85%A5%E1%86%BC%20%E1%84%8E%E1%85%A5%E1%84%85%E1%85%B5%20%E1%84%87%E1%85%AE%E1%84%87%E1%85%AE%E1%86%AB%2011429d746aff8055ab69d003d6cdf34f/image.png)

- application/x-www-form-urlencoded` 형식은 앞서 GET에서 살펴본 쿼리 파라미터 형식과 같다.
- 따라서 **쿼리 파라미터 조회 메서드를 그대로 사용**하면 된다.
- 클라이언트(웹 브라우저) 입장에서는 두 방식에 차이가 있지만, 서버 입장에서는 둘의 형식이 동일하므로,
`request.getParameter()` 로 편리하게 구분없이 조회할 수 있다.

## HTTP API 메시지 바디에 데이터 보내기

### TEXT 보내기

http://localhost:8080/request-body-string

```java
@WebServlet(name = "requestBodyStringServlet", urlPatterns = "/request-body-string")
public class RequestBodyStringServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletInputStream inputStream = req.getInputStream();
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        System.out.println("messageBody = " + messageBody);
        resp.getWriter().write("OK");
        
    }
}
```

→ inputStream은 byte 코드를 반환한다. 이를 String으로 변환하기 위해 StreamUtils (spring 제공됨)

사용하여 변환이 필요함.

```java
StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
//UTF-8 로 변환하는 파라미터가 필요.
```

### JSON 보내기

- 파싱을 위해 객체를 생성해야 함. (HelloData)
- HelloData 안에 있는 것.

`private String username;`

`private int age;`

- getter, setter 생성이 필요하나, Lombok을 사용할 시,
- 어노테이션 @Getter, @Setter 사용이 가능.

```java
@WebServlet(name = "requestBodyJsonServlet", urlPatterns = "/request-body-json")
public class RequestBodyJsonServlet extends HttpServlet {

    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletInputStream inputStream = request.getInputStream();
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        System.out.println(messageBody); //before json parsing

        HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
        //after json parsing
        System.out.println("helloData.username = " + helloData.getUsername());
        System.out.println("helloData.age = " + helloData.getAge());

        response.getWriter().write("OK");
    }
}
```

- 기본 spring boot 제공 json 라이브러리 : jackson 사용.
- ObjectMapper 객체 생성하여, 

`HelloData helloData = objectMapper.readValue*(*messageBody, HelloData.class*)*;`
    
    사용.
    

# Servlet - 응답 부분

- 데이터를 보내기
- HTTP 응답 메시지를 생성하는 것.
    - HTTP 응답 코드 지정
    - 헤더 생성
    - 바디 생성
    - Content type, cookie, Redirect

### Header 에 보내기

```java
response.setStatus(HttpServletResponse.SC_OK); //200 code

//response-header
response.setHeader("Content-Type", "text/plain;charset=utf-8");
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); //cache를 완전히 부여 하겠다는 뜻.
response.setHeader("Pragma", "no-cache");
response.setHeader("my-header", "hello");
```

### Body에 Html 보내기

- ContentType, Encoding 방법을 반드시 알려주어야 함.

```java
response.setContentType("text/html");
response.setCharacterEncoding("utf-8");

PrintWriter writer = response.getWriter();
writer.println("<html>");
writer.println("<body>");
writer.println(" <div>안녕?</div>");
writer.println("</body>");
writer.println("</html>");
```

### Body에 JSON 보내기 - HTTP API 방식

- Content Type : application/json
- Encoding : utf-8

```java
@WebServlet(name = "responseJsonServlet" , urlPatterns = "/response-json")
public class ResponseJsonServlet extends HttpServlet {
    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        HelloData helloData = new HelloData();
        helloData.setUsername("choi jae woo");
        helloData.setAge(26);

        //{"username" : "choi jae woo", "age" : 26} parsing with jackson library
        String result = objectMapper.writeValueAsString(helloData);

        response.getWriter().write(result);

    }
}
```

# 기본 회원 관리 웹 어플리케이션

### Member 저장, 조회 기능 구현.

- Member 객체 생성 → 저장
- MemberRepository → 싱글톤으로 관리

```java
private static final MemberRepository instance = new MemberRepository(); //싱글톤으로 만든다.
    //singleton으로 할 때는 생성자를 private으로 해야 함.
```

- getInstance() 로 MemberRepository 사용

```java
public static MemberRepository getInstance() {
        return instance;
    }
```

- MemberFormServlet : 멤버 이름, 나이 입력받아서 post, body에 담아 보냄(x-www-form-urlencoded 방식)
urI : /servlet/members/save
- MemberSaveServlet : /servlet/members/save 임.
    
    → username, age를 받아 memberRepository.save() 함.
    
    ```java
    package hello.servlet.web.servlet;
    
    import hello.servlet.domain.member.Member;
    import hello.servlet.domain.member.MemberRepository;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.annotation.WebServlet;
    import jakarta.servlet.http.HttpServlet;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    
    import java.io.IOException;
    import java.io.PrintWriter;
    
    @WebServlet(name = "memberSaveServlet", urlPatterns = "/servlet/members/save")
    public class MemberSaveServlet extends HttpServlet {
        private MemberRepository memberRepository = MemberRepository.getInstance();
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            System.out.println("MemberSaveServlet.service");
            String username = req.getParameter("username");
            int age = Integer.parseInt(req.getParameter("age"));
    
            Member member = new Member(username, age);
            memberRepository.save(member);
    
            resp.setContentType("text/html");
            resp.setCharacterEncoding("utf-8");
    
            PrintWriter w = resp.getWriter();
            w.write("<html>\n" +
                    "<head>\n" +
                    " <meta charset=\"UTF-8\">\n" + "</head>\n" +
                    "<body>\n" +
                    "성공\n" +
                    "<ul>\n" +
                    "    <li>id="+member.getId()+"</li>\n" +
                    "    <li>username="+member.getUsername()+"</li>\n" +
                    " <li>age="+member.getAge()+"</li>\n" + "</ul>\n" +
                    "<a href=\"/index.html\">메인</a>\n" + "</body>\n" +
                    "</html>"
            );
        }
    }
    
    ```
    
- MemberListServlet : 저장된 member를 table 형식으로 보여줌.