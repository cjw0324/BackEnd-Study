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