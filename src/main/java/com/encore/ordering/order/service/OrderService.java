package com.encore.ordering.order.service;

import com.encore.ordering.item.domain.Item;
import com.encore.ordering.item.repository.ItemRepository;
import com.encore.ordering.member.domain.Member;
import com.encore.ordering.member.domain.Role;
import com.encore.ordering.member.repository.MemberRepository;
import com.encore.ordering.order.domain.OrderStatus;
import com.encore.ordering.order.domain.Ordering;
import com.encore.ordering.order.dto.OrderReqDto;
import com.encore.ordering.order.dto.OrderResDto;
import com.encore.ordering.order.repository.OrderRepository;
import com.encore.ordering.order_item.domain.OrderItem;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    public OrderService(OrderRepository orderRepository, MemberRepository memberRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.itemRepository = itemRepository;
    }

    public Ordering create(OrderReqDto orderReqDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("not found email"));

        // id, orderStatus, orderItems에는 Entity에서 정의한 초기 값으로 설정, member만 넣어줌
        Ordering ordering = Ordering.builder().member(member).build();

        // Ordeing 객체가 생성될 때 OrderingItem 객체도 함께 생성 : Cascading
        // Cascading PERSIST
        for(OrderReqDto.OrderReqItemDto dto : orderReqDto.getOrderReqItemDtos()){
            Item item = itemRepository.findById(dto.getItemId()).orElseThrow(()->new EntityNotFoundException("not found item"));

            OrderItem orderItem = OrderItem.builder()
                    .item(item)
                    .quantity(dto.getCount())
                    .ordering(ordering)
                    .build();

            // 빈 List<OrderItem>에 orderItem들을 삽입 -> orderItem DB에는 자동으로 cascading되어 추가됨
            ordering.getOrderItems().add(orderItem);
            
            int newStockQuantity = item.getStockQuantity()-dto.getCount();
            if(newStockQuantity < 0){
                throw new IllegalArgumentException("not enough stock");
            }
            orderItem.getItem().updateStockQuantity(newStockQuantity); //dirty checking
        }
        return orderRepository.save(ordering);
    }

    public Ordering cancel(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        //order 구한 뒤 order member의 email과 비교
        Ordering ordering = orderRepository.findById(id).orElseThrow(()->new EntityNotFoundException("not found order"));

        //자기 자신, 관리자만 수행 가능
        if(!ordering.getMember().getEmail().equals(email) && !authentication.getAuthorities().contains((new SimpleGrantedAuthority("ROLE_ADMIN")))) {
            throw new AccessDeniedException("access denied");
            // 프로젝트 때는 ExceptionHandler로 잡아 403에러 발생시키기
        }

        // 이미 취소된 주문에 대해서는 취소 불가능
        if(ordering.getOrderStatus() == OrderStatus.CANCELED){
            throw new IllegalArgumentException("already canceled order");
        }

        ordering.cancelOrder();
        for(OrderItem orderItem : ordering.getOrderItems()){
            int newStockQuantity = orderItem.getItem().getStockQuantity() + orderItem.getQuantity();
            orderItem.getItem().updateStockQuantity(newStockQuantity); //dirty checking
        }

        return orderRepository.save(ordering);
    }

    public List<OrderResDto> findAll(){
        List<Ordering> orderings = orderRepository.findAll();
        return orderings.stream().map(o -> OrderResDto.toDto(o)).collect(Collectors.toList());
    }

    public List<OrderResDto> findByMember(Long id) {
//        Member member = memberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("member not found"));
//        List<Ordering> orderings = member.getOrderings();

        List<Ordering> orderings = orderRepository.findByMemberId(id);
        return orderings.stream().map(o -> OrderResDto.toDto(o)).collect(Collectors.toList());
    }

    public List<OrderResDto> findMyOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member not found"));

        List<Ordering> orderings = member.getOrderings();
        return orderings.stream().map(o -> OrderResDto.toDto(o)).collect(Collectors.toList());
    }
}
