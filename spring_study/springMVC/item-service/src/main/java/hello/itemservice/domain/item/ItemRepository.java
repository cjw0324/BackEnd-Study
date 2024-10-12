package hello.itemservice.domain.item;


import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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

    public Item update(Long itemId, Item updateParam) {
        Item findItem = findById(itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setQuantity(updateParam.getQuantity());
        findItem.setPrice(updateParam.getPrice());
        return findItem;
    }
    public void clearStore(){
        store.clear();
    }
}
