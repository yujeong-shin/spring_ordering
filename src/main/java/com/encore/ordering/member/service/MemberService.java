package com.encore.ordering.member.service;

import com.encore.ordering.member.domain.Address;
import com.encore.ordering.member.domain.Member;
import com.encore.ordering.member.domain.Role;
import com.encore.ordering.member.dto.LoginReqDto;
import com.encore.ordering.member.dto.MemberCreateReqDto;
import com.encore.ordering.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.NoSuchElementException;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    //Bean으로 만들어 놓은 PasswordEncoder 주입
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {

        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;

    }

    public Member create(MemberCreateReqDto memberCreateReqDto){
        // Address 조립, 필수 입력이 아니라 null이 들어있을 수도 있음
        Address address = new Address(memberCreateReqDto.getCity(), memberCreateReqDto.getStreet(), memberCreateReqDto.getZipcode());

        Member member = Member.builder()
              .name(memberCreateReqDto.getName())
              .email(memberCreateReqDto.getEmail())
              .password(passwordEncoder.encode(memberCreateReqDto.getPassword()))
              .address(address)
              .role(Role.USER)
              .build();
        return memberRepository.save(member);
    }

    public Member login(LoginReqDto loginReqDto) throws IllegalArgumentException{
        //email 존재여부
        Member member = memberRepository.findByEmail(loginReqDto.getEmail()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 이메일입니다."));

        //password 일치여부
        if(!passwordEncoder.matches(loginReqDto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("비밀번호 불일치");
        }
        return member;
    }
}
