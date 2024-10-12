# 6. Spring MVC -  Web Page 만들기

### 요구사항 분석

- 상품 도메인 모델
    - 상품 ID
    - 상품명
    - 가격
    - 수량
- 상품 관리 기능
    - 상품 목록
    - 상품 상세
    - 상품 등록
    - 상품 수정

### 서비스 제공 흐름

![image.png](https://github.com/cjw0324/Spring_Study/blob/main/spring_study/StudyNotion/Spring_MVC_WebPage_%EB%A7%8C%EB%93%A4%EA%B8%B0/6%20Spring%20MVC%20-%20Web%20Page%20%EB%A7%8C%EB%93%A4%EA%B8%B0%2011829d746aff80709bdfebf92d5a1efd/image.png)

---

## 상품 도메인 개발

- Itme class
    
    ```java
    @Data
    public class Item {
        private Long id;
        private String itemName;
        private Integer price;
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
    
- ItemRepository
    
    ```java
    @Repository
    public class ItemRepository {
        private static final ConcurrentHashMap<Long, Item> store = new ConcurrentHashMap<>();
        private static final AtomicLong sequence = new AtomicLong(0);
    
        public Item save(Item item) {
            item.setId(sequence.incrementAndGet());
            store.put(item.getId(), item);
            return item;
        }
    
        public Item findById(Long id) {
            return store.get(id);
        }
        public List<Item> findAll() {
            return new ArrayList<>(store.values());
        }
    
        public void update(Long itemId, Item updateParam) {
            Item findItem = findById(itemId);
            findItem.setItemName(updateParam.getItemName());
            findItem.setQuantity(updateParam.getQuantity());
            findItem.setPrice(updateParam.getPrice());
        }
        public void clearStore(){
            store.clear();
        }
    }
    ```
    

## 상품 목록 폼

- BasicItemController
    - **`*@Autowired** //생성자가 하나만 있으면 @Autowired 생략 가능.*`
    - `*public BasicItemController(ItemRepository itemRepository) {       this.itemRepository = itemRepository; }*`
        
        → 생성자
        
    - 생성자는 `@RequiredArgsConstructor` 사용하여 생략 가능.
        
        → final 이 붙은 멤버변수만 사용해서 생성자를 자동으로 만들어 준다.
        
    - `@PostConstruct` : 해당 빈의 의존관계가 모두 주입되고 나면 초기화 용도로 호출된다.
        
        → 테스트 용 데이터를 넣기위해 사용함.
        
    
    ```java
    @Controller
    @RequestMapping("/basic/items")
    @RequiredArgsConstructor
    public class BasicItemController {
        private final ItemRepository itemRepository;
    
        /**
         * @Autowired //생성자가 하나만 있으면 @Autowired 생략 가능.
         * public BasicItemController(ItemRepository itemRepository) {
         * this.itemRepository = itemRepository;
         * }
         * 생성자는 @RequiredArgsConstructor 사용하여 생략 가능.
         */
        @GetMapping
        public String items(Model model) {
            List<Item> items = itemRepository.findAll();
            model.addAttribute("items", items);
            return "basic/item";
        }
    
        /**
         * 테스트 용 데이터 추가
         */
    
        @PostConstruct
        public void init(){
            itemRepository.save(new Item("testA", 10000, 10));
            itemRepository.save(new Item("testB", 20000, 20));
        }
    }
    
    ```
    

## 상품 상세 폼

**URL : “/basic/items/{itemId}”**

```java
    @GetMapping("/{itemId}")
    public String item(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "basic/item";
    }
```

- itemId 로 itemRepository 조회하여 해당하는 item 객체를 생성하여 Model에 담아 반환.
- Thymeleaf - “basic/item” 으로 가도록 return 한다.

## 상품 등록 폼

**v1 → v4**

- v1 :
    
    ```java
    public String addItemV1(@RequestParam String itemName,
                                @RequestParam int price,
                                @RequestParam Integer quantity,
                                Model model
                                )
    ```
    
    - @RequestParam String itemName : itemName 요청 파라미터를 해당 변수에 받는다. 나머지 price, quantity도 동일하게 받는다.
    - Item 객체를 생성하고 저장한다.
    - 저장된 Item 객체를 모델에 담아 뷰에 전달한다.
- v2 :
    
    ```java
    public String addItemV2(@ModelAttribute("item") Item item, Model model)
    ```
    
    - @ModelAttribute 는 Item 객체를 생성하고, 요청 파라미터 값을 프로퍼티 접근법으로 입력해준다.
    - 추가로! @ModelAttribute는 모델에 @ModelAttribute로 생성하고 지정한 객체를 자동으로 넣어준다. 즉 `model.addAttribute*(*"item", item*)*;` 코드를 주석처리 해도 동작한다.
- v3 :
    
    ```java
    public String addItemV3(@ModelAttribute Item item)
    ```
    
    - @ModelAttribute(”item”) → 모델 어트리뷰트의 이름을 생략할 수 있다.
    - 생략되면 모델에 저장될 때, 클래스 명을 사용하고 Item → item으로 모델에 자동 추가된다.
- v4 :
    
    ```java
    public String addItemV4(Item item)
    ```
    
    - @ModelAttribute 자체도 생략 가능하고, 대상 객체인 Item item은 모델에 자동 등록된다.

## 상품 수정

→ 두가지가 필요하다.

1. GET - 상품 수정 폼 뷰
2. POST - 상품 수정 처리 → 해당 상품 페이지로 리다이렉트 해야 함.

- 상품 수정 폼 뷰
    
    ```java
        @GetMapping("/{itemId}/edit")
        public String editForm(@PathVariable Long itemId, Model model) {
            Item item = itemRepository.findById(itemId);
            model.addAttribute(item);
            return "basic/editForm";
        }
    ```
    
    - 수정에 필요한 정보를 조회하고 수정용 폼 뷰 (”basic/editForm”) 을 호출한다.

- 상품 수정 처리
    
    ```java
    @PostMapping("/{itemId}/edit")
        public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
            itemRepository.update(itemId, item);
            return "redirect:/basic/items/{itemId}";
        }
    ```
    
    - 요청 파라미터 변수로 itemId와, body에 수정 된 item 을 @ModelAttribute를 받는다.
    - 해당하는 itemId로 item의 내용으로 update한다.
    - /basic/items/{itemId} → 여기로 리다이렉트 하고, 컨트롤러에 매핑된 @PathVariable 값은 redirect 에도 사용될 수 있음을 확인할 수 있다.

## PRG Post/Redirect/Get

- 상품 등록을 완료하고, 새로고침을 하면 상품이 계속해서 중복 등록된다. (id값이 증가하고 목록에서 새로고침 한 횟수만큼 동일한 물건이 중복해서 저장되었다.)
- Post로 새로운 상품 등록 후 새로고침을 할 때 순서
    1. 상품 등록 버튼 눌러서 상품 등록 폼 접근
    2. `@GetMapping*(*"/add"*)`* : “basic/addForm” 뷰 호출
    3. `@PostMapping*(*"/add"*)*` : item 객체 생성하여 데이터를 item 객체에 담고, repository에 save 한다.
    4. 상품 등록 폼에서 데이터를 입력하고 저장을 누르면, Post /add + 상품 데이터를 서버로 전송한다.
    5. 새로 고침을 누른다.
    6. 웹 브라우저의 새로고침은 마지막에 서버에 전송한 데이터를 다시 전송한다.
    7. 가장 마지막에 전송 했던 과정 (4번 과정) 을 반복한다. 즉 Post /add + 상품 데이터를 서버로 다시 전송하게 된다. 
    8. 다시 해당 요청에 대한 메서드가 실행된다. `@PostMapping*(*"/add"*)*` : item 객체 생성하여 데이터를 item 객체에 담고, repository에 save 한다.
    
    **→ Post 하고 , Redirect 해서, 다시 Get 으로 보내 : PRG 방식.**
    

## Redirect Attributes

- 상품을 add / edit 하고 해당 상품의 상세 페이지로 redirect 하는것은 알겠다.
- 그런데 client의 입장에서, 이 요청이 잘 된 것인지 알지 못한다.
- 그래서 저장이 잘 되었으면, 상품 상세 화면에 “저장되었습니다” 라는 메시지를 보여달라는 요구사항을 구현해야 한다.

```java
@PostMapping("/add")
    public String addItemV6(Item item, RedirectAttributes redirectAttributes) {
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/basic/items/{itemId}";
    }
```

→ V5 에서는 item을 저장하고, redirect 할때, 단순히 상세 페이지로 리다이렉트 했다면,

→ V6 에서는 item을 저장하고, RedirectAttributes redirectAttributes 에 itemId, status를 추가하여 리다이렉트 하였다.

- 이렇게 하면, return `"redirect:/basic/items/{itemId}";` 여기서 itemId 값은 그대로 URL로 붙고, status는 URL에 없으니, 쿼리 파라미터 값으로 추가가 된다.
- 즉, 다음의 URL로 리다이렉트 됨을 알 수 있다. → [http://localhost:8080/basic/items/3?status=true](http://localhost:8080/basic/items/3?status=true)
