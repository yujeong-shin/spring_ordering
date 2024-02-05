package com.encore.ordering.order.domain;

import com.encore.ordering.member.domain.Member;
import com.encore.ordering.order_item.domain.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
//@Builder
//@AllArgsConstructor
// @Builder, @AllArgsConstructor 사용 시 초기값을 설정해놔도 ex) orderStatus = OrderStatus.ORDERED; ==> Hello 객체
// @Builder.Default로 값을 넣어주지 않으면 null이 들어감 ==> myBuilder 객체에 null이 담김
// 이 클래스에서는 직접 생성자를 만드는 방식으로 구현
// id, orderStatus, orderItems에는 초기 값으로 설정,
// member만 생성해주면 -> 나머지 초기 값들 유지
@NoArgsConstructor
public class Ordering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    // @Builder.Default없이 초기 값을 세팅해봤자, builder 생성 규칙에 의해 null로 세팅됨 ⭐
//    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;

    // 세종쓰 아이디어 사용하려고 Builder, @AllArgsConstructor 주석처리
    @Builder
    public Ordering(Member member){
        this.member = member;
    }

    public void cancelOrder(){
        this.orderStatus = OrderStatus.CANCELED;
    }
}
