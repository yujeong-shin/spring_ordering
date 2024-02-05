package com.encore.ordering.order.repository;

import com.encore.ordering.member.domain.Member;
import com.encore.ordering.order.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Ordering, Long> {
    List<Ordering> findByMemberId(Long id);
}
