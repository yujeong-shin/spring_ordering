package com.encore.ordering.member.controller;

import com.encore.ordering.common.ResponseDto;
import com.encore.ordering.member.domain.Member;
import com.encore.ordering.member.dto.LoginReqDto;
import com.encore.ordering.member.dto.MemberCreateReqDto;
import com.encore.ordering.member.dto.MemberResponseDto;
import com.encore.ordering.member.service.MemberService;
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
    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/member/create")
    public ResponseEntity<ResponseDto> memberCreate(@Valid @RequestBody MemberCreateReqDto memberCreateReqDto){
        Member member = memberService.create(memberCreateReqDto);
        //ResponseDto 객체에 담긴 값은 header로 나가고, body에 담긴 Map은 json 형태로 나감.
        return new ResponseEntity<>(new ResponseDto(HttpStatus.CREATED, "member is successfully created", member.getId()), HttpStatus.CREATED);
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

//    @GetMapping("/member/{id}/orders")

//    @GetMapping("/member/myorders")

    @PostMapping("/doLogin")
    public ResponseEntity<ResponseDto> memberLogin(@Valid @RequestBody LoginReqDto loginReqDto){
        Member member = memberService.login(loginReqDto);
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        Map<String, Object> member_info = new HashMap<>();
        member_info.put("id", member.getId());
        member_info.put("token", jwtToken);
        return new ResponseEntity<>(new ResponseDto(HttpStatus.OK, "member is successfully logined", member_info), HttpStatus.OK);
    }
}
