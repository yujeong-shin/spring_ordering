package com.encore.ordering.securities;

import com.encore.ordering.common.ErrorResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter extends GenericFilter {
    @Value("${jwt.secretKey}")
    private String secretKey;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try{
            String bearerToken = ((HttpServletRequest)request).getHeader("Authorization");
            if(bearerToken != null){
                //Bearer 토큰에서 토큰 값만 추출
                if(!bearerToken.substring(0, 7).equals("Bearer ")){
                    throw new AuthenticationServiceException("token의 형식이 맞지 않습니다.");
                }
                String token = bearerToken.substring(7);

                //추출된 토큰 검증 및 claims 추출(userInfo)
                //Body = claims = Payload 비슷한 개념
                //Body를 꺼내는 과정에서 재암호화한 값과 사용자가 들고 들어온 token 값이 일치하는지 검증이 자동으로 됨
                //검증 코드는 없지만, 내부적으로 에러나면 에러 터짐.
                Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

                //Authentication 객체를 생성하기 위한 UserDetails 생성⭐⭐
                //사용자 정보를 Authentication 객체 안에 삽입 -> order, ... 등 다른 부분에서 사용.
                //email은 Jwts.claims().setSubject(email);로 삽입
                //role은 claims.put("role", role);로 삽입. 꺼낼 시에도 형식 맞춰 꺼내야 함
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_"+claims.get("role")));
                UserDetails userDetails = new User(claims.getSubject(), "", authorities);

                //토큰은 authentication에 자동 등록 과정이 없기 떄문에, 검증 시에 Authentication 객체 생성 후 등록
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // filterChain에서 그 다음 filtering으로 넘어가도록 하는 메서드
            chain.doFilter(request, response);
        }catch(AuthenticationServiceException e){
            // Controller가 아니기 때문에 직접 만들어줘야 함.
            // 이때, errorMessage 메서드를 ErrorResponseDto.java에 makeMessage메서드로 빼면서 공통화 했음
            HttpServletResponse httpServletResponse = (HttpServletResponse)response;
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write(ErrorResponseDto.makeMessage(HttpStatus.UNAUTHORIZED, e.getMessage()).toString());
        }
    }
}
