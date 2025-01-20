package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager em;

    @Test
    void updateTest() {
        Book book = em.find(Book.class, 1L);
        book.setName("자동으로 쿼리를 만들어서 업데이트 해줌 -> dirty checking == 변경 감지");

    }
}
