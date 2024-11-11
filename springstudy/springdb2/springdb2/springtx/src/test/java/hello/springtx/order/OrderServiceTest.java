package hello.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;


    @Test
    void complete() throws NotEnoughMoneyException {
        //given
        Order order = new Order();
        order.setUsername("choi jaewoo");
        order.setMoney(20000);

        //when
        assertThatCode(() -> orderService.order(order)).doesNotThrowAnyException();

        //then
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("결제 완료");
    }

    @Test
    void runtimeException() {
        //given
        Order order = new Order();
        order.setUsername("예외");
        order.setMoney(20000);

        //when
        assertThatThrownBy(() -> orderService.order(order)).isInstanceOf(RuntimeException.class);

        //then : 롤백 되어 저장한 데이터가 없어야 한다.
        Optional<Order> orderItem = orderRepository.findById(order.getId());
        assertThat(orderItem.isEmpty()).isTrue();
    }

    @Test
    void bizException() {
        //given
        Order order = new Order();
        order.setUsername("cjw");
        order.setMoney(5000);

        //when
        assertThatThrownBy(() -> orderService.order(order)).isInstanceOf(NotEnoughMoneyException.class);

        //then : 커밋 되고, "결제 대기" 로 상태를 저장해야 함
        Order saveItem = orderRepository.findById(order.getId()).get();
        assertThat(saveItem.getPayStatus()).isEqualTo("결제 대기");
    }
}