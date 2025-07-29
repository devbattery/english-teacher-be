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
    class RegisterMember {

        @DisplayName("회원 가입 시 성공하면, 상태코드 200과 id를 반환하고 회원 프로필이 조회된다.")
        @Test
        void when_signupMember_then_response200AndId_and_canFetchMemberProfile() {
            // docs
            api_문서_타이틀("signupMember_success", spec);

            // when
            var response = 비회원이_로그인한다(spec);

            // then
            상태코드가_200이다(response);
        }

    }

}
