package com.devbattery.englishteacher.acceptance.auth;


import static com.devbattery.englishteacher.acceptance.auth.AuthSteps.로그아웃_한다;
import static com.devbattery.englishteacher.acceptance.auth.AuthSteps.비회원이_로그인한다;
import static com.devbattery.englishteacher.acceptance.auth.AuthSteps.상태코드_404와_오류코드_m001을_반환하는지_검증한다;
import static com.devbattery.englishteacher.acceptance.auth.AuthSteps.상태코드가_204임을_검증한다;
import static com.devbattery.englishteacher.acceptance.auth.AuthSteps.원준이_로그인한다;
import static com.devbattery.englishteacher.acceptance.auth.AuthSteps.토큰과_상태코드_200을_응답하는지_검증한다;

import com.devbattery.englishteacher.acceptance.AcceptanceTest;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("인증 관련 기능 인수테스트")
class AuthAcceptanceTest extends AcceptanceTest {

//    @Nested
//    @DisplayName("로그인 인수테스트")
//    class Login {
//
//        @DisplayName("로그인 요청시 성공하면 토큰을 반환한다.")
//        @Test
//        void when_login_then_return_200AndToken() {
//            // docs
//            api_문서_타이틀("login_success", spec);
//
//            // when
//            var response = 원준이_로그인한다(spec);
//
//            // then
//            토큰과_상태코드_200을_응답하는지_검증한다(response);
//        }
//
//        @DisplayName("로그인 요청시 해당하는 회원이 없으면 404와 오류코드 m001을 반환한다")
//        @Test
//        void when_loginFailedByUnregisteredEmail_then_return_400AndCodem001() {
//            // docs
//            api_문서_타이틀("login_failedByUnregisteredEmail", spec);
//
//            // when
//            var response = 비회원이_로그인한다(spec);
//
//            // then
//            상태코드_404와_오류코드_m001을_반환하는지_검증한다(response);
//        }
//
//    }
//
//    @Nested
//    @DisplayName("로그아웃 테스트")
//    class Logout {
//
//        @DisplayName("로그아웃 요청이 성공하면, 204코드를 반환한다.")
//        @Test
//        void when_logout_then_return_204() {
//            // docs
//            api_문서_타이틀("logout_success", spec);
//
//            // given
//            var 원준_로그인응답 = 원준이_로그인한다(new RequestSpecBuilder().build());
//            String 원준_액세스토큰 = 원준_로그인응답.jsonPath().getString("accessToken");
//
//            // when
//            var response = 로그아웃_한다(원준_액세스토큰, spec);
//
//            // then
//            상태코드가_204임을_검증한다(response);
//        }
//
//    }


}
