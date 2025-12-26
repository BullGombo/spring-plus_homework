package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Aspect // 이 클래스가 (공통 관심사) 임을 선언
@Component // AOP는 반드시 Bean이어야 동작
@RequiredArgsConstructor // final 필드를 파라미터로 받는 생성자 자동 생성
public class AdminAccessLoggingAspect {

    private final HttpServletRequest request; // 현재 요청 정보 접근 : 유저 아이디, 요청 URL 등
    // ############################################## 1 - 5 ##############################################
    // 메서드명으로 부터 추측 : 일단 이 AOP 메서드의 역할은 UserRole 변경 후 로깅이라고 추측
//@After("execution(* org.example.expert.domain.user.controller.UserController.getUser(..))") // <- 근데 매핑이 일반유저, 실행 시점이 @After
    // 마침 UserAdminController에 역할 변경 메서드가 존재해서 이거에 매핑, 요구사항대로 역할변경 메서드 실행 전 동작하도록 @Before
    @Before("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public void logAfterChangeUserRole(JoinPoint joinPoint) { // JoinPoint : AOP가 끼어든 현재 실행 지점 정보 - 메서드명/클래스명/파라미터 접근 가능
        String userId = String.valueOf(request.getAttribute("userId"));
        String requestUrl = request.getRequestURI();
        LocalDateTime requestTime = LocalDateTime.now();

        log.info("Admin Access Log - User ID: {}, Request Time: {}, Request URL: {}, Method: {}",
                userId, requestTime, requestUrl, joinPoint.getSignature().getName());
//        Admin Access Log -
//                User ID: 1,
//                Request Time: 2025-12-26T12:00:00,
//                Request URL: /admin/users/1,
//                Method: changeUserRole

        // 전체 실행 흐름 정리...
//        PATCH /admin/users/{userId}
//                      ↓
//        JwtFilter → request.setAttribute("userId")
//                      ↓
//        AdminAccessLoggingAspect (@Before)
//                      ↓
//        UserAdminController.changeUserRole()
//                      ↓
//        UserAdminService.changeUserRole()
    }
}
