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
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    //
    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)
    //@Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;

    // 세종쓰 아이디어 사용하려면 메서드 단 @Builder, @AllArgsConstructor 주석처리
    @Builder
    public Ordering(Member member){
        this.member = member;
    }

    public void cancelOrder(){
        this.orderStatus = OrderStatus.CANCELED;
    }
}
