package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.TiDBDialect;
import org.hibernate.jdbc.Expectations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void join() {
        // given
        Member member = new Member();
        member.setName("cjw");


        // when
        Long saveId = memberService.join(member);

        // then
        assertEquals(member, memberRepository.findOne(saveId));
    }

    @Test
    void validation_same_member_name_v1() {
        // given
        Member member1 = new Member();
        member1.setName("cjwoo");

        Member member2 = new Member();
        member2.setName("cjwoo");

        // when & then
        memberService.join(member1);
        assertThatThrownBy(() -> memberService.join(member2)).isInstanceOf(IllegalStateException.class);

    }


    @Test
    void validation_same_member_name_v2() {
        // given
        Member member1 = new Member();
        member1.setName("cjwoo");

        Member member2 = new Member();
        member2.setName("cjwoo");

        // when & then
        memberService.join(member1);
        assertThatThrownBy(() -> memberService.join(member2)).isInstanceOf(IllegalStateException.class);

    }
}