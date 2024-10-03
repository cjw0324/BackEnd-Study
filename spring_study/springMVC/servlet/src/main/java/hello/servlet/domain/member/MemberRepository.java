package hello.servlet.domain.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberRepository {
    private static Map<Long, Member> store = new HashMap<>();
    private static long sequence = 0L;
    //static 으로 했기 때문에, MemberRepository 가 계속 생성되더라도 (new 로), static 인 것들은 딱 한번만 생성, 사용된다.

    private static final MemberRepository instance = new MemberRepository(); //싱글톤으로 만든다.
    //singleton으로 할 때는 생성자를 private으로 해야 함.

    public static MemberRepository getInstance() {
        return instance;
    }

    private MemberRepository() {

    }

    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;
    }

    public Member findById(long id) {
        return store.get(id);
    }

    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    public void clearStore() {
        store.clear();
    }

}
