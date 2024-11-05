package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        Member memberV0 = new Member("memberV100", 10000);
        repository.save(memberV0);

        //FindById
        Member findMember = repository.findById(memberV0.getMemberId());
        log.info("findMember = {}", findMember);

        log.info("member != findMember {}", memberV0 == findMember);
        assertThat(findMember).isEqualTo(memberV0);

        //update money 10000 -> 20000
        repository.update(memberV0.getMemberId(), 20000);
        Member updatedMember = repository.findById(memberV0.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        //delete
        repository.delete(memberV0.getMemberId());

        //등록 했다가 바로 삭제하는 것이기 때문에 볼 수 없다.
        //그럼 검증을 어떻게 해야 하는가?
        assertThatThrownBy(() -> repository.findById(memberV0.getMemberId())).isInstanceOf(NoSuchElementException.class);


    }
}