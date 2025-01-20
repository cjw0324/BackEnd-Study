package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    LogRepository logRepository;

    /**
     * MemberService @Transactional:OFF
     * MemberRepository @Transactional:ON
     * LogRepository @Transactional:ON
     */

    @Test
    void outerTxOff_success() {
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());

    }
    /**
     * MemberService @Transactional:OFF
     * MemberRepository @Transactional:ON
     * LogRepository @Transactional:ON
     */

    @Test
    void outerTxOff_fail() {
        //given
        String username = "로그예외_outerTxOff_fail";

        //when
        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        //then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());

    }


    /**
     * MemberService @Transactional:ON
     * MemberRepository @Transactional:OFF
     * LogRepository @Transactional:OFF
     */

    @Test
    void singleTransaction_commit() {
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());

    }


    /**
     * MemberService @Transactional:ON
     * MemberRepository @Transactional:OFF
     * LogRepository @Transactional:OFF
     */

    @Test
    void singleTransaction_rollback() {
        //given
        String username = "로그예외_outerTxOff_success";

        //when
        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        //then
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());

    }

    /**
     * MemberService @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository @Transactional:ON
     */

    @Test
    void outerTxOn_success_commit() {
        //given
        String username = "outerTxOn_success";

        //when
        memberService.joinV1(username);

        //then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());

    }

    /**
     * MemberService @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository @Transactional:ON -> Exception
     */

    @Test
    void outerTxOn_innerFail_rollback() {
        //given
        String username = "로그예외_outerTxOn_success";

        //when
        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        //then
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());

    }


    @Test
    void recoverException_fail() {
        //given
        String username = "로그예외_recoverException_fail";

        //when
        assertThatThrownBy(() -> memberService.joinV2(username)).isInstanceOf(UnexpectedRollbackException.class);

        //then
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());


    }

    /**
     * MemberService @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository @Transactional:ON -> REQUIRES_NEW 로 별도의 물리 트랜잭션으로 분리한다.
     */

    @Test
    void recoverException_success() {
        //given
        String username = "로그예외_recoverException";

        //when
        memberService.joinV2(username);

        //then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());

    }


}