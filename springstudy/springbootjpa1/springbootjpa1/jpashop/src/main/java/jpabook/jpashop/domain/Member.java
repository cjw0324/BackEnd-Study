package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.*;

@Entity
@Getter @Setter
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;


    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member", fetch = LAZY)
    private List<Order> orders = new ArrayList<>();
}
