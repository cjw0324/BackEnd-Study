package hello.itemservice.web.basic;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return "basic/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "basic/item";
    }

    @GetMapping("/add")
    public String addForm() {
        return "basic/addForm";
    }

//    @PostMapping("/add")
    public String addItemV1(@RequestParam String itemName,
                            @RequestParam int price,
                            @RequestParam Integer quantity,
                            Model model
                            )
    {
        Item item = new Item(itemName, price, quantity);
        itemRepository.save(item);
        model.addAttribute("item", item);
        return "basic/item";
    }

    @PostMapping("/add")
    public String addItemV4(Item item) {
        itemRepository.save(item);
        return "basic/item";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute(item);
        return "basic/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/basic/items/{itemId}";
    }

    /**
     * 테스트 용 데이터 추가
     */

    @PostConstruct
    public void init() {

        itemRepository.save(new Item("testA", 10000, 10));
        itemRepository.save(new Item("testB", 20000, 20));
    }


}
