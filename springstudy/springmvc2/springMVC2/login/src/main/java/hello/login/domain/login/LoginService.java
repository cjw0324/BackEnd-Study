package hello.login.domain.login;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final MemberRepository memberRepository;

    /**
     * return 이 null 이면 로그인 실패
     */
    public Member login(String loginId, String password) {
//        Optional<Member> findMemberOptional = memberRepository.findByLoginId(loginId);
//        Member member = findMemberOptional.get();
//        if (member.getPassword().equals(password)) {
//            return member;
//        } else {
//            return null;
//        }
//        Optional<Member> findMemberLoginId = memberRepository.findByLoginId(loginId);
//        return findMemberLoginId.filter(member -> member.getPassword().equals(password)).orElse(null);
        return memberRepository.findByLoginId(loginId).filter(member -> member.getPassword().equals(password)).orElse(null);
    }
}
