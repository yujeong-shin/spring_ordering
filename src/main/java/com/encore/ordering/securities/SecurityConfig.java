package com.encore.ordering.securities;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
//pre : 사전, post : 사후, 사전/사후에 인증/권한 검사 어노테이션 사용 가능 ex.@PreAuthorize("hasRole('ADMIN')")
public class SecurityConfig {
    private final JwtAuthFilter authFilter;

    public SecurityConfig(JwtAuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    //여러 filter들을 걸어주는 것
    //custom한 authFilter를 전체 filterChain에 넣어줘야 함
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                //xss, csrf 차이점 정리 필요.
                .csrf().disable()
                .cors().and() //CORS 활성화
                .httpBasic().disable() //기본 Http Configuarer
                .authorizeRequests()
                //인증 미적용 url 패턴
                .antMatchers("/member/create", "/doLogin", "/items", "/item/*/image")
                    .permitAll()
                .anyRequest().authenticated()
                .and()
                //세션을 사용하지 않겠다
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //custom filter는 인증 미적용 url 이더라도 무조건 실행된다

                //UsernamePasswordAuthenticationFilter전에 authFilter를 실행
                //UsernamePasswordAuthenticationFilter : form 로그인에서 기본적으로 사용하는 필터
                //RestAPI패턴에서는 form 형식이 아니라? 구현하진 않지만(MVC는 사용)
                //내부적으로 무의미하게 실행된다. 그 전에 내 필터를 걸어주면 좋음.
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
