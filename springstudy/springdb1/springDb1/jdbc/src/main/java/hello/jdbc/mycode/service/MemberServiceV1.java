package hello.jdbc.mycode.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV1 {
    private final MemberRepositoryV1 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);

        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        log.info("my log : from member 감소 ");

        log.info("validation before");
        // -> 오류 발생 시킬 것임.
        validation(toMember);
        log.info("validation after");
        // -> toMember의 id 가 ex 이면 예외 발생됨.

        memberRepository.update(toId, toMember.getMoney() + money);
        log.info("my log : to member 증가 ");
    }

    public static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }


}
