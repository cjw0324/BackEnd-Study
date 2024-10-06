# Spring MVC - 시작하기

# Spring MVC

![image.png](Spring%20MVC%20-%20%E1%84%89%E1%85%B5%E1%84%8C%E1%85%A1%E1%86%A8%E1%84%92%E1%85%A1%E1%84%80%E1%85%B5%2011729d746aff80d3a9b3f51d2ff91a53/image.png)

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

## Strart Spring MVC !

- 기존의 Spring MVC 를 직접 구현한 것을 실제 Spring MVC로 구현해보자.
- 애노테이션 기반으로 동작한다
    - 유연하고 실용적이다.
- `@RequestMapping` : 컨트롤러를 만든다.
    - 요청이 들어오면 해야할 것
        1. 핸들러 매핑으로 핸들러 조회
            
            → `@RequestMappingHandlerMapping`
            
        2. 핸들러 어댑터 조회 및 실행
            
            → `@RequestMappingHandlerAdapter`
            
    - @RequestMapping annotaion을 사용하면 두개의 핸들러 매핑, 어댑터 조회 가 사용 가능하다.
- **SpringMemberFormControllerV1 - 회원 등록 폼**

```java
@Controller
public class SpringMemberFormControllerV1 {
    @RequestMapping("springmvc/v1/members/new-form")
    public ModelAndView process() {
        return new ModelAndView("new-form");
    }
}
```

- `@Controller`  : controller 내에 “@Component” 가 있어서 “@Controller” 를 통해서도 스프링이 자동으로 스프링빈에 등록한다. = Component Scan의 대상이 됨.
- `@Controller`  : Spring MVC에서 애노테이션 기반 컨트롤러로 인식한다.

- `@RequestMapping` : 해당 url이 호출되면 이 메서드가 호출된다. 애노테이션 기반으로 동작하기 때문에, 메서드 이름은 임의로 지어도 된다.

→ 의문점. 왜? RequestMapping 이 메서드에 붙어있는데 핸들러 조회와 어댑터 조회가 가능한가?

→ RequestMappingHandlerMapping은 스프링 빈 중에서`@RequestMapping`또는`@Controller`가 클래스 레벨에 붙어 있는 경우에 매핑 정보로 인식한다.

→ 즉, RequestMappingHandlerMapping 이 해당 핸들러(=컨트롤러) 를 매핑 정보로 인식하기 위해 `“@Contorller”` 애노테이션을 붙인 것. 이것 대신 `“@Component” + “@RequestMapping”` 을 붙여도 된다. 

```java
//@Controller
@Component
@RequestMapping
public class SpringMemberFormControllerV1{
 ...
}
```

### Spring 3.0 이상

스프링 부트 3.0(스프링 프레임워크 6.0)부터는 클래스 레벨에 `@RequestMapping` 이 있어도 스프링 컨트롤러로 인식하지 않는다. 오직 `@Controller` 가 있어야 스프링 컨트롤러로 인식한다. 참고로 `@RestController` 는 해당 애노테이션 내부에 `@Controller` 를 포함하고 있으므로 인식 된다. 따라서 `@Controller` 가 없는 위의 두 코드는 스프링 컨트롤러로 인식되지 않는다

→ `RequestMappingHandlerMapping` 에서 `@RequestMapping` 는 이제 인식하
지 않고, `Controller` 만 인식한다.

## Spring MVC - 컨트롤러 통합

### Class 단위 RequestMapping 을 한 클래스 내에서 메서드 단위로 바꾸자!

- `springmvc.v2.SpringMemberControllerV2`

```java
@Controller
public class SpringMemberControllerV2 {
    private final MemberRepository memberRepository = MemberRepository.getInstance();

    @RequestMapping("/springmvc/v2/members/new-form")
    public ModelAndView newForm() {
        return new ModelAndView("new-form");
    }

    @RequestMapping("/springmvc/v2/members/save")
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        ModelAndView mv = new ModelAndView("save-result");
        mv.addObject("member", member);
        return mv;
    }

    @RequestMapping("/springmvc/v2/members")
    public ModelAndView members() {
        List<Member> members = memberRepository.findAll();
        ModelAndView mv = new ModelAndView("members");
        mv.addObject("members", members);
        return mv;
    }
}
```

- 메서드 명이 겹치면 안되니 메서드 명을 각 기능에 맞게 변경.
- 연관성 있는 Controller 끼리 묶어서 넣는 것이 좋다.
- 단, `@RequestMapping*(*"/springmvc/v2/members/save"*)`* 등 URL에서 겹치는 부분이 있다.
    - “/springmvc/v2/members → 이 부분 중복.
    - Class 단위의 @RequestMapping(”중복되는 부분”) 을 넣고, 메서드 단위의 @RequestMapping(”하위 URL”) 을 넣어주면 `중복되는 부분 + 하위 URL` 로 처리한다.
- 불편한 점!
    - 컨트롤러(핸들러) 에서 ModelAndView 를 만들어서 계속 전달해야 한다.
    - → V3 에서 개선하자!

## Spring MVC - 실용적인 방식

- `springmvc.v3.SpringMemberControllerV3`

```java
@Controller
@RequestMapping("/springmvc/v3/members")
public class SpringMemberControllerV3 {
    private final MemberRepository memberRepository = MemberRepository.getInstance();

    @RequestMapping("/new-form")
    public String newForm() {
        return "new-form";  //이제는 ModelAndView 반환 하는 것이 아닌 String 으로 반환해도 된다.
                            //애노테이션 기반 컨트롤러는 문자열를 반환해도 된다 -> 그러면 반환된 문자열을 View 이름으로 알고 프로세스가 진행된다.
    }

    @RequestMapping("/save")
    public String save(@RequestParam("username") String username, @RequestParam("age") int age, Model model) { //HttpServletRequest request, HttpServletResponse response -> 파라미터를 직접 받을 수 있다
        Member member = new Member(username, age);
        memberRepository.save(member);

        model.addAttribute("member", member);
        return "save-result";
    }

    @RequestMapping("")
    public String members(Model model) {
        List<Member> members = memberRepository.findAll();
        model.addAttribute("members", members);
        return "members";
    }
}
```

- ModelAndView 객체를 반환하지 않고 String 을 반환하자!
    - 애노테이션 기반 컨트롤러가 문자열을 반환하면 해당 문자열의 View 이름으로 진행된다.
- RequestMapping 의 컨트롤러(핸들러) 의 매개변수로
    
    `@RequestParam(”paramName”) Type parameter` 로 바로 파라미터들을 사용할 수 있다.
    

→ 여기서 더 발전 시켜보자 → Get, Post 등 정해서 받자. 그동안은 어떤 방식이든 가능했다.

→ 컨트롤러의 특성에 기능에 맞는 방식으로 통신을 할 수 있도록 정한다.

- new-form 은 GET 방식. (조회)
- save 는 POST 방식

방법 1

```java
@RequestMapping(value = "/new-form", method = RequestMethod.GET) //get 방식만 정해서 받는다
```

방법 2

```java
@GetMapping("/new-form")
@PostMapping("/save")
```