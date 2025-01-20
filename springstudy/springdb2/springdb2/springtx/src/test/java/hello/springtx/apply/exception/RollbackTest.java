package hello.springtx.apply.exception;


import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import static hello.springtx.apply.exception.RollbackTest.RollbackService.*;
import static org.assertj.core.api.Assertions.*;
@Slf4j
@SpringBootTest
public class RollbackTest {

    @Autowired
    RollbackService rollbackService;

    @TestConfiguration
    static class RollbackTestConfig {
        @Bean
        RollbackService rollbackService() {
            return new RollbackService();
        }
    }

    @Test
    void runtimeException() {
        assertThatThrownBy(() -> rollbackService.runtimeException()).isInstanceOf(RuntimeException.class);
    }

    @Test
    void checkedException() {
        assertThatThrownBy(() -> rollbackService.checkedException()).isInstanceOf(MyException.class);
    }

    @Test
    void rollbackFor() {
        assertThatThrownBy(() -> rollbackService.rollbackFor()).isInstanceOf(MyException.class);
    }



    @Slf4j
    static class RollbackService {
        //런타임 예외  - 롤백

        @Transactional
        public void runtimeException() {
            log.info("call runtime exception");
            throw new RuntimeException();
        }

        //체크 예외 발생 - 커밋
        @Transactional
        public void checkedException() throws MyException {
            log.info("call checked exception");
            throw new MyException();
        }


        //체크 예외 rollbackFor  지정 - 강제로 롤백
        @Transactional(rollbackFor = MyException.class)
        public void rollbackFor() throws MyException {
            log.info("call checked exception use rollbackFor");
            throw new MyException();
        }



        static class MyException extends Exception {
        }
    }
}
