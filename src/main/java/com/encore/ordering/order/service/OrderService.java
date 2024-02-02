package com.encore.ordering.order.service;

import com.encore.ordering.member.domain.Member;
import com.encore.ordering.member.repository.MemberRepository;
import com.encore.ordering.order.domain.Ordering;
import com.encore.ordering.order.dto.OrderReqDto;
import com.encore.ordering.order.repository.OrderRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    public OrderService(OrderRepository orderRepository, MemberRepository memberRepository) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
    }

    public Ordering create(OrderReqDto orderReqDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("not found email"));
        return null;
    }


//    public void create(OrderReqDto orderReqDto){
//        Ordering ordering = Ordering.builder().build();
//        orderRepository.create(ordering);
//
//        for (int i = 0; i < orderReqDto.getItemIds().size(); i++) {
//            Long id = orderReqDto.getItemIds().get(i);
//            Long count = orderReqDto.getItemIds().get(i);
//            orderItemRepository.create();
//            )
//        }
//    }
//    -> 해당 방식은 create를 두 번 해줘야 함. cascade로 orderRepo에만 create시켜보자.
}
