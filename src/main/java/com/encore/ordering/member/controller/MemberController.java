package com.encore.ordering.member.controller;

import com.encore.ordering.common.CommonResponse;
import com.encore.ordering.member.domain.Member;
import com.encore.ordering.member.dto.LoginReqDto;
import com.encore.ordering.member.dto.MemberCreateReqDto;
import com.encore.ordering.member.dto.MemberResponseDto;
import com.encore.ordering.member.service.MemberService;
import com.encore.ordering.order.dto.OrderResDto;
import com.encore.ordering.order.service.OrderService;
import com.encore.ordering.securities.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final OrderService orderService;

    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider, OrderService orderService) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.orderService = orderService;
    }

    @PostMapping("/member/create")
    public ResponseEntity<CommonResponse> memberCreate(@Valid @RequestBody MemberCreateReqDto memberCreateReqDto){
        Member member = memberService.create(memberCreateReqDto);
        //ResponseDto 객체에 담긴 값은 header로 나가고, body에 담긴 Map은 json 형태로 나감.
        return new ResponseEntity<>(new CommonResponse(HttpStatus.CREATED, "member is successfully created", member.getId()), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/members")
    public List<MemberResponseDto> members(){
        return memberService.findAll();
    }

    @GetMapping("/member/myInfo")
    public MemberResponseDto findMyInfo(){
        return memberService.findMyInfo();
    }

    // 관리자가 회원목록조회 -> 주문횟수 -> 해당 회원의 주문 목록
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member/{id}/orders")
    public List<OrderResDto> findMemberOrders(@PathVariable Long id){
        return orderService.findByMember(id);
    }

    @GetMapping("/member/myorders")
    public List<OrderResDto> findMyOrders(){
        return orderService.findMyOrders();
    }

    @PostMapping("/doLogin")
    public ResponseEntity<CommonResponse> memberLogin(@Valid @RequestBody LoginReqDto loginReqDto){
        Member member = memberService.login(loginReqDto);
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        Map<String, Object> member_info = new HashMap<>();
        member_info.put("id", member.getId());
        member_info.put("token", jwtToken);
        return new ResponseEntity<>(new CommonResponse(HttpStatus.OK, "member is successfully logined", member_info), HttpStatus.OK);
    }
}
