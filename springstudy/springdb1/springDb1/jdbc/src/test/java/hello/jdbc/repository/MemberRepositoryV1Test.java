package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    @BeforeEach
    void beforeEach() {
        //기본 DriverManager - 항상 새로운 커넥션 획득

//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        //커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new MemberRepositoryV1(dataSource);
    }

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