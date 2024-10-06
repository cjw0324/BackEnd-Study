# MVC Framework 만들기

## MVC 패턴의 한계

- 포워드 중복 : View로 이동하는 코드가 항상 중복 호출된다.
- ViewPath 중복 : jsp의 경로나, jsp 파일형식이 변경되면 모든 코드를 수정해야 함
- 사용하지 않는 코드가 발생할 수 있다.
- 공통 처리가 어렵다
    - 메서드화 하더라도 항상 해당 메서드를 호출해야 한다. 그것 또한 중복이다.
    - 수문장 역할을 하는 컨트롤러들이 호출되도록 하는 프론트 컨트롤러 가 필요함.
        
        → 컨트롤러 앞에서 프론트 컨트롤러가 먼저 요청 받고 이것이 먼저 공통 기능을 처리한다.
        
        → 스프링 MVC의 핵심이 Front Controller에 있다.
        
- **FrontController 패턴 특징**
    - 프론트 컨트롤러 서블릿 하나로 클라이언트의 요청을 받음
    - 프론트 컨트롤러가 요청에 맞는 컨트롤러를 찾아서 호출한다
    - 입구를 하나로!
    - 공통 처리 가능
    - 프론트 컨트롤러를 제외한 나머지 컨트롤러는 서블릿을 사용하지 않아도 됨.

## 프론트 컨트롤러 도입 - Version 1

![image.png](MVC%20Framework%20%E1%84%86%E1%85%A1%E1%86%AB%E1%84%83%E1%85%B3%E1%86%AF%E1%84%80%E1%85%B5%2011529d746aff805c88a6fc67dc0c7e83/image.png)

```java
/frontcontroller/v1 //directory
```

ControllerV1 interface 생성 → 왜 interface 일까?

- v1 의 핵심 목표 :
    - Controller의 @WebServlet(name=””, urlPatterns=””) 중복 제거.
    - FrontControllerServlet 에서 먼저 요청을 받고, Mapping 정보에서 해당하는 Controller를 찾고 해당 Controller를 호출한다.
- FrontControllerServletV1 code

```java
@WebServlet(name = "frontControllerServletV1", urlPatterns = "/front-controller/v1/*")
public class FrontControllerServletV1 extends HttpServlet {
    private Map<String, ControllerV1> controllerMap = new HashMap<>();

    public FrontControllerServletV1() {
        controllerMap.put("/front-controller/v1/members/new-form", new MemberFormControllerV1());
        controllerMap.put("/front-controller/v1/members/save", new MemberSaveControllerV1());
        controllerMap.put("/front-controller/v1/members", new MemberListControllerV1());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("FrontControllerServletV1.service");

        String requestURI = request.getRequestURI();
        ControllerV1 controller = controllerMap.get(requestURI);
        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        controller.process(request, response);
    }
}
```

## View 분리 - v2

- v1 에서의 문제 보완점 :
    - 중복되는 코드들이 있다! → 중복 제거해 보자
    
    ```java
    String viewPath = "/WEB-INF/views/new-form.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request,response);
    ```
    
    - 중복되는 내용은 View를 위한 부분.
- 따라서 별도로 뷰를 처리하는 객체를 만들자.

![image.png](MVC%20Framework%20%E1%84%86%E1%85%A1%E1%86%AB%E1%84%83%E1%85%B3%E1%86%AF%E1%84%80%E1%85%B5%2011529d746aff805c88a6fc67dc0c7e83/image%201.png)

- Controller 가 이제는 직접 JSP로 포워드 하지 않고, MyView를 반환하고, MyView가 JSP를 포워딩 한다.
- MyView는 viewPath를 받고, 해당 viewPath로 JSP 포워딩 시켜 랜더링한다 → 이를 render method로 분리.

```java
public class MyView {
    private String viewPath;

    public MyView(String viewPath) {
        this.viewPath = viewPath;
    }

    public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }
}
```

### V2 의 실행 과정.

- FrontController는 HTTP요청을 받고, 인터페이스로 생성된 ControllerV2 를 해당 Controller 객체를 생성하고 해당 Controller를 실행한다.
- Controller 의 process method 가 실행되면, 각 process는 비즈니스 로직을 실행하고, 이를 띄워줄 jsp path가 있는 MyView 객체를 반환한다.
- 반환된 MyView 객체를 받은 FrontController는 반환된 MyView의 view.render(request, response) 메서드를 실행하여 최종적으로 아래의 코드에 맞는 View가 실행된다.

```java
public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }
```

- V2의 한계
    - 서블릿 종속성 제거
        
        → 컨트롤러 입장에서 HttpServletRequest, HttpServletResponse 이 필요한가?
        
        → 요청 파라미터 정보는 다른 방식으로 넘기면 된다.
        
        → `request.setAttribute*(*"members", members*)*;` : 별도의 Model 을 사용하지 않고, request 를 모델로 사용하고 있기 때문에 이를 바꿔줘야 함.
        
        → Controller는 서블릿 기술을 전혀 사용하지 않도록 변경하자.
        
    - 뷰 이름 중복 제거
        
        → 컨트롤러에서 view을 물리 위치(디렉토리 구조 및 확장자 명까지) 반환하지 않고, 단순히 뷰의 논리 이름을 반환하고, 물리 위치 는 프론트 컨트롤러에서 처리하자!
        
        → 즉, 컨트롤러는 
        `String viewPath = "/WEB-INF/views/members.jsp";`
        
        이런 파일의 물리 위치를 반환하지 않고,
        
        `members` 라는 논리 이름을 반환하자!
        

## V3

![image.png](MVC%20Framework%20%E1%84%86%E1%85%A1%E1%86%AB%E1%84%83%E1%85%B3%E1%86%AF%E1%84%80%E1%85%B5%2011529d746aff805c88a6fc67dc0c7e83/image%202.png)

- 서블릿 종속성 제거
- 뷰 이름 중복 제거

- ModelView
    - model은 단순히 map으로 되어 있으므로 컨트롤러
    에서 뷰에 필요한 데이터를 key, value로 넣어주면 된다.
- ControllerV3 interface
    - 컨트롤러는 서블릿 기술을 전혀 사용하지 않는다. 따라서 구현이 매우 단순해지고, 테스트 코드 작성시 테스트 하기
    쉽다.
    - HttpServletRequest가 제공하는 파라미터는 프론트 컨트롤러가 paramMap에 담아서 호출해주면 된다.
    응답 결과로 뷰 이름과 뷰에 전달할 Model 데이터를 포함하는 ModelView 객체를 반환하면 된다.
- **MemberFormControllerV3 - 회원 등록 폼**
    - ModelView`를 생성할 때`new-form 이라는 view의 논리적인 이름을 지정한다. 실제 물리적인 이름은 프론트 컨트 롤러에서 처리한다.
- viewResolver()
    - 논리 뷰 이름: members
    - 물리 뷰 경로: /WEB-INF/views/members.jsp
- view.render(mv.getModel(), request, response)
    - 뷰 객체를 통해서 HTML 화면을 렌더링 한다.
    - 뷰 객체의 render()는 모델 정보도 함께 받는다.
    - JSP는 request.getAttribute() 로 데이터를 조회하기 때문에, 모델의 데이터를 꺼내 request.setAttribute() 로 담아둔다.
    - JSP로 포워드 해서 JSP를 렌더링 한다.
- MyView
    
    ```java
    public void render(Map<String, Object> model, HttpServletRequest request,
     HttpServletResponse response) throws ServletException, IOException {
             modelToRequestAttribute(model, request);
             RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
             dispatcher.forward(request, response);
    }
    ```
    

### 실행 순서대로 주절주절…

http 요청 → FrontControllerServletV3 

이제는 controller들이 HttpServletRequest, Response를 사용하지 않을 것이다. 그렇다면, 무엇을 어떻게 controller들에 넘길것인가?

→ request에서 requestURI를 따로 땐다

그리고 controllerMap에서 get(requestURI)로 요청에 맞는 원하는 controller를 생성한다. 이때 ControllerV3 인터페이스로 생성.

그러면 ControllerV3는 ModelView를 반환하는 process 메서드가 있고, 입력은 Map<String, String> 이라는 paramMap 을 받는다.

→ 그렇다면 ModelView는 무엇이길래 controller에서 반환하는가?

→ ModelView에는 viewName과 Map<String, Object> 라는 model 이 있다.

→ 다시 요청을 받았던 FrontControllerServletV3에서 paramMap에 createParamMap() 메서드를 사용하여 request로 받은 모든 paramName과 parameter들을 Map<String, String> 으로 넘긴다.

→ 처음에 
`/front-controller/v3/members/new-form`

요청 이라면, body에 뭐가 없다~ 그리고 controller.process를 실행한다.

→ 해당 controller의 비즈니스 로직이 실행된다. 이후  “new-form”이 담긴 ModelView를 반환하여 FrontControllerServletV3에서 이를 반환 받는다.

→ viewResoolver를 통해 jsp파일의 절대경로를 알아내고, myView.render를 통해 렌더링한다.

→ 이때 modelView.getModel()을 함께 넘기는데, hashmap 으로 된 model 을 넘기는데, save가 실행되었다면, member를. list가 실행된다면 member list 인 members를 render에 보낸다.

→ MyView 의 `render*(*Map*<*String, Object*>* model, HttpServletRequest request, HttpServletResponse response*)`* 는 model 을 풀어해치기 위해 modelToRequestAttribute 메서드를 사용한다. 이는 model의 key, value 값들을 request 저장소에 각각 저장한다.

→ 그리고 `RequestDispatcher dispatcher = request.getRequestDispatcher*(*viewPath*)*;`  와
`dispatcher.forward*(*request, response*)*;` 를 사용하여 viewPath 에 맞는 jsp 로 포워딩 시킨다.

→ 그러면 해당 JSP가 렌더링 된다.

이후 다른 요청으로,

→ 요청 url 이 `/front-controller/v3/members/save` 라면

→ MemberSaveControllerV3 가 실행되고, new-form.jsp로 받은 username=””, age=”” 가  paramMap에 매핑되고

→ 이 값들을 controller.process(paramMap)에 넘겨 controller를 실행한다.

→ save controller 가 저장 비즈니스 로직을 실행 한 후, 

ModelView 객체를 생성하고, 이때 생성자를 통해 jsp의 논리 이름으로 생성한다. + ModelView 객체의 model에 방금 저장 한 member 데이터를 담아 반환한다.

→ FrontControllerV3는 논리이름을 물리 저장 위치로 바꾸고, JSP의 물리 이름 이 저장된 객체의 render 를 실행한다.

이때 controller 실행 후 반환 받은 modelView 객체의 model이 Controller가 전달하고자 했던 데이터이기에 이를 함께 포함하여 render 메서드가 실행된다.

→ MyView 의 render() 에서 request 저장소에 model 데이터를 저장하고, 이를 해당 viewPath의 JSP에 포워딩 한다.

## V4 - 단순하고 실용적인 컨트롤러

- V3 구조와 거의 동일하다.
- 하지만 컨트롤러는 ModelView를 반환하지 않고 ViewName만 반환한다.
    
    ControllerV4
    `String process(Map<String, String> paramMap, Map<String, Object> model);`
    v4에서는 controller들은 process를 실행하고 return을 string type, 그리고 매개변수로 paramMap, model 을 받는다.
    

![image.png](MVC%20Framework%20%E1%84%86%E1%85%A1%E1%86%AB%E1%84%83%E1%85%B3%E1%86%AF%E1%84%80%E1%85%B5%2011529d746aff805c88a6fc67dc0c7e83/image%203.png)

## V5 - 어댑터 개념 추가

어댑터? 인터페이스가 구조적으로 strict하다.

여러 controller 들을 사용할 수 있도록 한다.

어떤 controller 들을 선택할 수 있도록 한다.

![image.png](MVC%20Framework%20%E1%84%86%E1%85%A1%E1%86%AB%E1%84%83%E1%85%B3%E1%86%AF%E1%84%80%E1%85%B5%2011529d746aff805c88a6fc67dc0c7e83/image%204.png)

프론트 컨트롤러가 그동안은 바로 컨트롤러를 호출했다.

하지만 이제는 핸들러 어댑터를 통해 핸들러 = 컨트롤러를 호출한다.

### 실행 순서

- 프론트 컨트롤러가 요청을 받으면, 핸들러 매핑 정보를 통해 핸들러를 조회하고, 해당 핸들러를 처리할 수 있는지 핸들러 어댑터 목록을 통해 어댑터를 조회 한다
    - → MyHandlerAdaper - boolean supports(Object handler)
    - `ModelView handle*(*HttpServletRequest request, HttpServletResponse response, Object handler*)* throws IOException, ServletException;` → 어댑터는 실제 컨트롤러를 호출하고, 그 결과로 ModelView를 반환해야 한다.

→ 정리

1. [http://localhost:8080/front-controller/v5/v3/members/new-form](http://localhost:8080/front-controller/v5/v3/members/new-form)

요청이 온다 → FrontControllerServletV5 요청 받음.

1. getHandler 에서 Map의 키가 “/front-controller/v5/v3/members/new-form” 인 새로운 객체를 반환받는다 → **handler = MemberFormControllerV3**
2. getHandlerAdapter(handler) 를 통해 handler가 handlerAdapters 에 등록된 Adapter 인지 확인하여 처리 할 수 있다면, 처리할 수 있는 MyHandlerAdapter 를 구현한 구현 객체를 return 한다. 즉 V3 를 처리할 수 있는 HandlerAdapter이다.
3. **MyHandlerAdapter adapter =  ControllerV3HandlerAdapter**  가 된다.
4. ControllerV3HandlerAdapter 에서 ControllerV3 객체를 생성할 때, handler는 MemberFormControllerV3 이기 때문에 해당 컨트롤러 객체를 생성하고,
5. ControllerV3HandlerAdapter 의 handle(request, response, handler) 에서는 받은 Object type의 handler를 ControllerV3로 타입 캐스팅 해주고.
6. createParamMap 을 통해 request의 parameter들을 paramMap에 등록한다.
7. 그리고 해당 controller (MemberFormControllerV3) 의 process 를 실행하고 이때 paramMap을 넘기고 반환으로 ModelView 객체를 받는다. 그리고 이를 return 한다.
8. 다시 FrontControllerServletV5 에 돌아와서 ModelView 를 반환 받은 
`ModelView mv = adapter.handle*(*request, response, handler*)*;`   mv를 통해 viewName을 알고, 해당 viewName을 viewResolver() 를 통해 JSP 의 물리 위치를 만들어 낸다.
9. 그리고 MyView view 객체를 생성하고 이 view 객체를 render한다.
10. 그렇다면 해당 jsp로 MyView - render 는 포워딩 한다.

## 역할과 구현이 분리가 되어야 한다.

- 인터페이스 기반으로 구현하고 필요한 구현체를 꽂아 넣으면 된다.
- OCP 를 지킬 수 있게 된다.
- Spring MVC의 핵심 구조를 파악하는데 필요한 부분들을 직접 구현 해 봄.
- Spring MVC의 핵심 : HandlerAdapter

# Spring MVC

![image.png](MVC%20Framework%20%E1%84%86%E1%85%A1%E1%86%AB%E1%84%83%E1%85%B3%E1%86%AF%E1%84%80%E1%85%B5%2011529d746aff805c88a6fc67dc0c7e83/image%205.png)

**직접 만든 프레임워크 스프링 MVC 비교**

- FrontController → DispatcherServlet
- handlerMappingMap → HandlerMapping
- MyHandlerAdapter → HandlerAdapter
- ModelView → ModelAndView
- viewResolver → ViewResolver
- MyView → View

- ***HandlerMapping***

```
 0 순위 : RequestMappingHandlerMapping
  -> 애노테이션 기반의 컨트롤러인 @RequestMapping에서 사용

 1 순위 : BeanNameUrlHandlerMapping 
	-> 스프링 빈의 이름으로 핸들러를 찾는다.

```

- ***HandlerAdapter***

```
0 순위 : RequestMappingHandlerAdapter
 -> 애노테이션 기반의 컨트롤러인 @RequestMapping에서 사용
1 순위 : HttpRequestHandlerAdapter
 -> HttpRequestHandler 처리
2 순위 : SimpleControllerHandlerAdapter
 -> Controller 인터페이스(애노테이션X, 과거에 사용) 처리
```

## 핸들러 매핑과 핸들러 어댑터

### 실제 스프링이 동작하는 과정 예시.

- MyHttpRequestHandler

```java
@Component("/springmvc/request-handler")
public class MyHttpRequestHandler implements HttpRequestHandler {

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("MyHttpRequestHandler.handleRequest");
    }
}
```

- OldController

```java
@Component("/springmvc/old-controller") //spring bean 이름이 "/springmvc/old-controller" 라고 등록 함.
public class OldController implements Controller {
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("OldController.handleRequest");
        return null;
    }
}
```

1. MyHttpRequestHandler
    - url = /springmvc/request-handler
    
    **실행순서**
    
    - 핸들러 매핑으로 핸들러 조회
        - HandlerMapping 을 해야하고, 2순위인 빈이름으로 핸들러를 찾는 BeanNameUrlHandlerMapping 을 통해 → 핸들러인 MyHttpRequestHandler를 반환한다.
    - 핸들러 어댑터 조회
        - HandlerAdapter의 supports()를 순서대로 조회한다.
        - 1순위인 HttpRequestHandlerAdapter 가 선택 된다.
    - 핸들러 어댑터 실행
        - DispatcherServlet이 조회한 HttpRequestHandlerAdapter를 실행하며 핸들러도 함께 넘겨준다.
        - 즉, HttpRequestHandlerAdapter인 핸들러 어댑터는 핸들러인 MyHttpRequestHandler를 냉부에서 실행하고 그 결과를 반환한다.
2. OldController
    - url = "/springmvc/old-controller"
    
    **실행순서**
    
    - 핸들러 매핑으로 핸들러 조회
        - HandlerMapping 을 순서대로 실행한다.
        - BeanNameUrlHanderMapping사용하여 스프링 빈 이름으로 핸들러를 찾고, 이 결과로 OldController가 반환된다.
    - 핸들러 어댑터 조회
        - HandlerAdapter 의 supports()를 순서대로 호출한다.
        - 2순위. Controller 인터페이스를 처리할 수 있는 SimpleControllerHandlerAdapter를 찾아낸다.
    - 핸들러 어댑터 실행
        - 찾아낸 handler adapter인 SimpleControllerHandlerAdapter를 실행하며, 핸들러인 OldController 정보를 함께 넘겨 OldController를 내부에서 실행하고 그 결과를 반환한다.