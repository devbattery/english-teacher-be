package com.devbattery.englishteacher.acceptance.user;

import static com.devbattery.englishteacher.acceptance.auth.AuthSteps.비회원이_로그인한다;
import static com.devbattery.englishteacher.acceptance.user.UserSteps.상태코드가_200이다;

import com.devbattery.englishteacher.acceptance.AcceptanceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("회원 관련 기능 인수테스트")
class UserAcceptanceTest extends AcceptanceTest {

    @Nested
    @DisplayName("회원 가입 인수테스트")
    class SaveUser {

        @DisplayName("최초 로그인(회원 가입)이 성공하면, 상태코드 200을 반환한다.")
        @Test
        void when_first_login_then_response_200() {
            // docs
            api_문서_타이틀("firstLogin_success", spec);

            // when
            var response = 비회원이_로그인한다(spec);

            // then
            상태코드가_200이다(response);
        }

    }

}
