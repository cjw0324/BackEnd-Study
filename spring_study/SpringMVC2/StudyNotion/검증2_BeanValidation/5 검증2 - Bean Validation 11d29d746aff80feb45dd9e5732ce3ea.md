# 5. 검증2 - Bean Validation

## Bean Validation - 소개

**대체로 검증은 특정 필드 값이 빈 값인지?, 특정 크기를 넘는지, 안넘는지? 와 같은 매우 일반적인 로직이다.**

```java
@Data
public class Item {

    private Long id;
    
    @NotBlank
    private String itemName;
    
    @NotNull
    @Range(min = 1000, max == 1000000)
    private Integer price;
    
    @NotNull
    @Max(9999)
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
```

→ 검증 로직을 **모든 프로젝트에 적용할 수 있게 공통화하고, 표준화 한 것이 Bean Validation** 이다!

- **하이버네이트 Validator 관련 링크**
    - 공식 사이트: http://hibernate.org/validator/
    - 공식 메뉴얼: https://docs.jboss.org/hibernate/validator/6.2/reference/en-US/html_single/
    - 검증 애노테이션 모음: https://docs.jboss.org/hibernate/validator/6.2/reference/en-US/
    html_single/#validator-defineconstraints-spec

→ 애노테이션을 넣었으면, 이걸 사용할 수 있는 `Validator` 가 필요하다!

## Bean Validation - 시작

**build.gradle - dependency 추가**

```java
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

## Bean Validation - Spring 적용

**Item**

```java
@Data
public class Item {
    private Long id;
    @NotBlank
    private String itemName;
    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;
    @NotNull
    @Max(9999)
    private Integer quantity;
    public Item() {
    }
    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
```

**ValidationItemControllerV3**

```java
@Slf4j
@Controller
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor
public class ValidationItemControllerV3 {

    private final ItemRepository itemRepository;
    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v3/addForm";
    }

    @PostMapping("/add")  //실제 저장 로직 실행
    public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        //검증에 싪패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v3/addForm";
        }

        //검증에 성공한 이후 저장 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, @Validated Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId,@Validated @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

}
```

→ 기존의 ItemValidator 는 삭제 하고 실행 시켜보자.

**실행 결과**

![image.png](5%20%E1%84%80%E1%85%A5%E1%86%B7%E1%84%8C%E1%85%B3%E1%86%BC2%20-%20Bean%20Validation%2011d29d746aff80feb45dd9e5732ce3ea/image.png)

***잘 된다! 왜??***

어떻게 스프링 MVC 는 Bean Validator 를 사용할까?

- 스프링 부트가 spring-boot-strater-validation  라이브러리를 넣으면 자동으로 Bean Validator를 인지하고, 스프링에 통합한다.
- 스프링 부트는 자동으로 `LocalValidatorFactoryBean`을  글로벌 Validator로 등록한다.

**검증 순서**

1. @ModelAttribute 각각의 필드에 타입 변환 시도
    
    → 성공 시 다음으로
    
    → 실패 시 typeMissmatch 로 FieldError 추가.
    
2. Validator 적용 (`LocalValidatorFactoryBean`)

## Bean Validation - Error code

오류 코드가 자동으로 등록된다

어떻게? → 애노테이션 이름으로 등록된다.

`@NotBlank`

오류 발생시키고 로그를 찍어보면,

```java
codes [NotBlank.item.itemName,NotBlank.itemName,NotBlank.java.lang.String,NotBlank];
```

- CODES
    - NotBlank.item.itemName
    - NotBlank.itemName
    - NotBlank.java.lang.String
    - NotBlank

`@Range`

오류 발생시키고 로그를 찍어보면,

```java
codes [Range.item.price,Range.price,Range.java.lang.Integer,Range]; 
```

- CODES
    - Range.item.price
    - Range.price
    - Range.java.lang.Integer
    - Range

**BeanValidation 메시지 찾는 순서**

1. 생성된 메시지 코드 순서대로 messageSource에서 메시지 찾기
2. 애노테이션의 message 속성 사용 → `@NotBlank*(*message = "공백은 불가합니다"*)*private String itemName;`
3. 라이브러리가 제공하는 기본 값 사용

## Bean Validation - Object 오류

**그동안은 필드에 대한 에러를 검증하였다.**

Object 오류는 어떻게 처리할 수 있을까?

`@ScriptAssert()` 를 사용하면 된다!

검증 희망 Object class 에 애노테이션 `@ScriptAssert()` 를 붙이고, 원하는 언어, 조건을 작성할 수 있다.

```java
@ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >= 10000")
```

- 하지만, 실 사용에 제약도 많고, 더 복잡해질 수 있다.
- 따라서, java code 로 처리하는 것이 좋을 수 있다.
    
    ```java
    @PostMapping("/add")  //실제 저장 로직 실행
        public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
    
            if (item.getPrice() != null && item.getQuantity() != null) {
                int resultPrice = item.getPrice() * item.getQuantity();
                if (resultPrice < 10000) {
                    bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
                }
            }
            //검증에 싪패하면 다시 입력 폼으로
            if (bindingResult.hasErrors()) {
                log.info("errors = {}", bindingResult);
                return "validation/v3/addForm";
            }
    
            //검증에 성공한 이후 저장 로직
            Item savedItem = itemRepository.save(item);
            redirectAttributes.addAttribute("itemId", savedItem.getId());
            redirectAttributes.addAttribute("status", true);
            return "redirect:/validation/v3/items/{itemId}";
        }
    ```
    

## Bean Validation - edit 에도 적용

**상품 수정에도 Bean Validation을 적용하자.**

```java
@PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId,@Validated @ModelAttribute Item item, BindingResult bindingResult) {
        //검증에 싪패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v3/editForm";
        }
        
        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }
```

## Bean Validation - 한계

### 수정 시 검증 요구 사항

: 데이터를 등록할 때와, 수정할 때는 요구사항이 다를 수 있다!

**등록 요구사항과 수정 요구사항이 다르다면?** 

- 등록 quantity 최대 : 9,999 → 수정 할 때는 수량을 99,999 까지 가능하도록 하자!
- 수정시에는 id 값이 필수이다.

→ 만약, 아래와 같이 고친다면?

```java
@Data
public class Item {
@NotNull //수정 요구사항 추가
     private Long id;
     @NotBlank
     private String itemName;
     @NotNull
     @Range(min = 1000, max = 1000000)
     private Integer price;
@NotNull
//@Max(9999) //수정 요구사항 추가 private Integer quantity;
//...
}
```

**수정시에는 문제가 없다. 하지만, 등록때는 문제가 생긴다!!!**

**다음의 groups 로 그룹별로 검증방법을 다르게 하여 해결이 가능하다.**

## Bean Validation - groups

- **방법 2가지**
    - BeanValidation의 groups 기능을 사용한다.
    - Item을 직접 사용하지 않고, ItemSaveForm, ItemUpdateForm 같은 폼 전송을 위한 별도의 모델 객체를 만들어서 사용한다.

### Groups 적용 개발

interface 2개를 만들고, SaveCheck, UpdateCheck 두개의 인터페이스를 만든다.

**Interface**

```java
public interface SaveCheck {
}

public interface UpdateCheck {
}
```

**Item.class**

```java
@Data
public class Item {
    @NotNull(groups = UpdateCheck.class)
    private Long id;
    
    @NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
    private String itemName;
    
    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    @Range(min = 1000, max = 1000000, groups = {SaveCheck.class, UpdateCheck.class})
    private Integer price;
    
    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
    @Max(value = 9999, groups = {SaveCheck.class})
    @Max(value = 99999, groups = {UpdateCheck.class})
    private Integer quantity;
    
    public Item() {
    }
    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
```

**ValidationItemControllerV3**

```java
@PostMapping("/add")  //실제 저장 로직 실행
public String addItem2(@Validated(SaveCheck.class) @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

  objectOverThanMinValidation(item, bindingResult);
  //검증에 싪패하면 다시 입력 폼으로
  if (bindingResult.hasErrors()) {
      log.info("errors = {}", bindingResult);
      return "validation/v3/addForm";
  }

  //검증에 성공한 이후 저장 로직
  Item savedItem = itemRepository.save(item);
  redirectAttributes.addAttribute("itemId", savedItem.getId());
  redirectAttributes.addAttribute("status", true);
  return "redirect:/validation/v3/items/{itemId}";
}

@PostMapping("/{itemId}/edit")
public String edit2(@PathVariable Long itemId, @Validated(UpdateCheck.class) @ModelAttribute Item item, BindingResult bindingResult) {
  //검증에 싪패하면 다시 입력 폼으로
  if (bindingResult.hasErrors()) {
      log.info("errors = {}", bindingResult);
      return "validation/v3/editForm";
  }

  itemRepository.update(itemId, item);
  return "redirect:/validation/v3/items/{itemId}";
}
```

- 사실 groups 기능은 실제 잘 사용되지 않는다고 한다.
- 그 이유는 실무에서는 주로 다음에 등장하는 등록용 폼 객체와 수정용 폼 객체를 분리해서 사용하기 때문이다.

## Form 전송 객체 분리 - project V4

V3 → V4 copy&paste 하여 준비.

## Form 전송 객체 분리 - 소개

실무에서는 add 와 edit 에서 받는 Item은 다른 경우가 많다 (거의 대부분)

따라서 별도의 객체를 만들어서 전달하고, 예를 들면 ItemSaveForm 이라는 폼을 전달받는 전용 객체를 만들어서 @ModelAttribute로 사용한다. 이를 통해 컨트롤러에서 폼 데이터를 전달받고, 이후 컨트롤러에서 필요한 데이터를 사용하여 Item을 생성한다.

- Form 데이터 전달에 Item 도메인 객체를 사용하는 경우
    
    `HTML Form -> Item -> Controller -> Item -> Repository`
    
- Form 데이터 전달을 위한 별도의 객체를 사용하는 경우
    
    `HTML Form -> ItemSaveForm -> Controller -> Item 생성 -> Repository`
    

## Form 전송 객체 분리 - 개발

Item 에서의 검증을 모두 제거하자! 이제 Item에서의 검증은 사용하지 않는다.

**전송별 객체를 분리한다.**

/src/main/java/hello/itemservice/web/validation/form/ItemSaveForm.java

/src/main/java/hello/itemservice/web/validation/form/ItemUpdateForm.java

**추가하기**

```java
@Data
public class ItemSaveForm {
    @NotBlank
    private String itemName;
    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;
    @NotNull
    @Max(value = 9999)
    private Integer quantity;
}

@Data
public class ItemUpdateForm {
    @NotNull
    private Long id;
    @NotBlank
    private String itemName;
    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;
    @NotNull
    @Max(value = 99999)
    private Integer quantity;

}
```

**ValidationItemControllerV4 수정**

```java
@PostMapping("/add")  //실제 저장 로직 실행
public String addItem(@Validated @ModelAttribute("item") ItemSaveForm itemSave, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

    if (itemSave.getPrice() != null && itemSave.getQuantity() != null) {
        int resultPrice = itemSave.getPrice() * itemSave.getQuantity();
        if (resultPrice < 10000) {
            bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
        }
    }
    //검증에 싪패하면 다시 입력 폼으로
    if (bindingResult.hasErrors()) {
        log.info("errors = {}", bindingResult);
        return "validation/v4/addForm";
    }

    //검증에 성공한 이후 저장 로직
    Item item = new Item();
    item.setItemName(itemSave.getItemName());
    item.setPrice(itemSave.getPrice());
    item.setQuantity(itemSave.getQuantity());

    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);

    System.out.println(savedItem.getItemName());
    System.out.println(savedItem.getPrice());
    System.out.println(savedItem.getQuantity());
    return "redirect:/validation/v4/items/{itemId}";
}

@PostMapping("/{itemId}/edit")
public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm itemUpdate, BindingResult bindingResult) {
    //검증에 싪패하면 다시 입력 폼으로
    if (bindingResult.hasErrors()) {
        log.info("errors = {}", bindingResult);
        return "validation/v4/editForm";
    }

    Item item = new Item();
    item.setItemName(itemUpdate.getItemName());
    item.setPrice(itemUpdate.getPrice());
    item.setQuantity(itemUpdate.getQuantity());

    itemRepository.update(itemId, item);
    return "redirect:/validation/v4/items/{itemId}";
}
```

## Bean Validation - HTTP 메시지 컨버터

`@Valid, @Validated` 는 HttpMessageConverter `(@RequestBody)`에도 적용할 수 있다.

→ `@ModelAttribute` 는 HTTP 요청 파라미터 - URL 쿼리 스트링, POST form 에서 사용한다.

→ ***HTTP API 에서는 어떻게 사용해야 할까?***

### API Controller 에 적용해보기 (message body - JSON)

**ValidationItemApiController 생성**

```java
@Slf4j
@RestController
@RequestMapping("/validation/api/items")
public class ValidationItemApiController {
    @PostMapping("/add")
    public Object addItem(@Validated @RequestBody ItemSaveForm saveItem, BindingResult bindingResult) {
        
        log.info("API 컨트롤러 호출");

        if (bindingResult.hasErrors()) {
            log.info("검증 오류 발생 errors = {}", bindingResult);
            return bindingResult.getAllErrors();
        }

        log.info("성공 로직 실행 ");
        return saveItem;
    }
}
```

- **성공 요청 보낼 경우**

![image.png](5%20%E1%84%80%E1%85%A5%E1%86%B7%E1%84%8C%E1%85%B3%E1%86%BC2%20-%20Bean%20Validation%2011d29d746aff80feb45dd9e5732ce3ea/image%201.png)

![image.png](5%20%E1%84%80%E1%85%A5%E1%86%B7%E1%84%8C%E1%85%B3%E1%86%BC2%20-%20Bean%20Validation%2011d29d746aff80feb45dd9e5732ce3ea/image%202.png)

- “API 컨트롤러 호출” 이라는 로그도 잘 찍힌 것을 알 수 있다.

만약, 실패하는 요청을 보낸다면 어떻게 될까?

**실패의 2가지**

1. 검증 오류 요청 : JSON을 객체로 생성하는데에 성공 후 검증에서 실패하는 경우.
    
    ![image.png](5%20%E1%84%80%E1%85%A5%E1%86%B7%E1%84%8C%E1%85%B3%E1%86%BC2%20-%20Bean%20Validation%2011d29d746aff80feb45dd9e5732ce3ea/image%203.png)
    
    ![image.png](5%20%E1%84%80%E1%85%A5%E1%86%B7%E1%84%8C%E1%85%B3%E1%86%BC2%20-%20Bean%20Validation%2011d29d746aff80feb45dd9e5732ce3ea/image%204.png)
    
    →“API 컨트롤러 호출” 로그가 찍혔다. 컨트롤러가 실행 되고 검증에서 실패한 Case 이다.
    
2. 실패 요청 : JSON을 객체로 생성하는 것 자체가 실패하는 경우.
    
    ![image.png](5%20%E1%84%80%E1%85%A5%E1%86%B7%E1%84%8C%E1%85%B3%E1%86%BC2%20-%20Bean%20Validation%2011d29d746aff80feb45dd9e5732ce3ea/image%205.png)
    
    ![image.png](5%20%E1%84%80%E1%85%A5%E1%86%B7%E1%84%8C%E1%85%B3%E1%86%BC2%20-%20Bean%20Validation%2011d29d746aff80feb45dd9e5732ce3ea/image%206.png)
    
    → “API 컨트롤러 호출” 로그가 없다.
    
    → 컨트롤러의 호출도 못 한 Case 이다.
    

**`@ModelAttribute` vs `@RequestBody`**
`@ModelAttribute` 는 특정 필드에 타입 에러가 발생해도 나머지 필드는 정상 처리할 수 있었다.

하지만 `@RequestBody` 는 HttpMessageConverter 단계에서 JSON 데이터를 객체로 변경하지 모하면, 이후 단계 자체가 진행되지 않고, 예외가 발생한다. 컨트롤러도 호출되지 않고, Validator도 적용할 수 없다.

→ 예외 발생 시 원하는 모양으로 예외를 처리하는 방법은 **예외 처리 부분에서 다룰 예정.**