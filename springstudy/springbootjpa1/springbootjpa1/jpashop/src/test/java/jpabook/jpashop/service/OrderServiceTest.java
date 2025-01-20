package jpabook.jpashop.service;

import ch.qos.logback.classic.Logger;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@Slf4j
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ItemRepository itemRepository;


    @Test
    void 상품주문() {
        // given
        Member member = createMember("회원1");

        Book book = createBook("book1", 10000, 10);

        // when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order getOrder = orderRepository.findOne(orderId);

//        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertEquals("상품 주문 시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다.",1, getOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다.", 10000 * 2, getOrder.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야 한다.",8, book.getStockQuantity());
    }

    @Test
    void 주문취소_성공() {
        // given
        Member member = createMember("testUser2");
        Book item = createBook("book3", 20000, 10);

        Long orderId = orderService.order(member.getId(), item.getId(), 8);
        // when
        orderService.cancelOrder(orderId);

        // then
        assertThat(item.getStockQuantity()).isEqualTo(10);
    }


    @Test
    void 주문취소_실패() {
        // given
        Member member = createMember("testUser3");
        Book item = createBook("book4", 20000, 10);

        Long orderId = orderService.order(member.getId(), item.getId(), 8);
        // when
        orderRepository.findOne(orderId).getDelivery().setStatus(DeliveryStatus.COMP);



        // then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId)).isInstanceOf(IllegalStateException.class);
        assertThat(item.getStockQuantity()).isEqualTo(2);
    }


    @Test
    void 상품주문_재고수량초과() {
        // given
        Member member = createMember("testUser1");
        Item item = createBook("book2", 10000, 10);


        int orderCount = 11;
        // when & then
        assertThatThrownBy(() -> orderService.order(member.getId(), item.getId(), orderCount)).isInstanceOf(NotEnoughStockException.class);
    }


    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address("경기도", "정든로 14번길 19-1", "202호"));
        em.persist(member);
        return member;
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }


    @Test
    void 검색_memberName만_있을때() {
        // given
        OrderSearch orderSearch = new OrderSearch();
        orderSearch.setMemberName("testUser");
        orderSearch.setOrderStatus(null);

        Member member = createMember("testUser");
        Book item = createBook("bookA", 20000, 10);
        Long orderId = orderService.order(member.getId(), item.getId(), 3);


        // when
        List<Order> orders = orderService.findOrders(orderSearch);

        // then
        assertThat(orders.contains(orderRepository.findOne(orderId))).isTrue();
    }

    @Test
    void 검색_orderStatus만_있을때() {
        // given
        OrderSearch orderSearch = new OrderSearch();
        orderSearch.setOrderStatus(OrderStatus.ORDER);

        Member member = createMember("testUser");
        Book item = createBook("bookA", 20000, 10);
        Long orderId = orderService.order(member.getId(), item.getId(), 3);
        // when
        List<Order> orders = orderService.findOrders(orderSearch);

        // then
        assertThat(orders.contains(orderRepository.findOne(orderId))).isTrue();
    }

    @Test
    void 검색_memberName_orderStatus_다있을때() {
        // given


        OrderSearch orderSearch = new OrderSearch();
        orderSearch.setMemberName("testUser");
        orderSearch.setOrderStatus(OrderStatus.ORDER);

        Member member = createMember("testUser");
        Book item = createBook("bookA", 20000, 10);
        Long orderId = orderService.order(member.getId(), item.getId(), 3);
        // when
        List<Order> orders = orderService.findOrders(orderSearch);

        // then
        assertThat(orders.contains(orderRepository.findOne(orderId))).isTrue();
    }
}