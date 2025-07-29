package com.devbattery.englishteacher.acceptance.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.devbattery.englishteacher.acceptance.util.AuthFixture;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class AuthSteps {

    public static ExtractableResponse<Response> 비회원이_로그인한다(RequestSpecification spec) {
        return 로그인한다(AuthFixture.비회원_로그인_요청(), spec);
    }

    public static ExtractableResponse<Response> 원준이_로그인한다(RequestSpecification spec) {
        return 로그인한다(AuthFixture.사용자_원준_로그인_요청(), spec);
    }

    public static ExtractableResponse<Response> 로그인한다(Map<String, Object> loginRequest, RequestSpecification spec) {
        return RestAssured
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .spec(spec)
                .log().all()
                .body(loginRequest)
                .when().post("/api/test/login")
                .then().log().all()
                .extract();
    }

    public static void 상태코드_404와_오류코드_m001을_반환하는지_검증한다(ExtractableResponse<Response> response) {
        Assertions.assertAll(
                () -> 상태코드를_검증한다(response, HttpStatus.NOT_FOUND),
                () -> 오류코드를_검증한다(response, "m001")
        );
    }

    public static void 상태코드_401과_오류코드_a005를_반환하는지_검증한다(ExtractableResponse<Response> response) {
        Assertions.assertAll(
                () -> 상태코드를_검증한다(response, HttpStatus.UNAUTHORIZED),
                () -> 오류코드를_검증한다(response, "a005")
        );
    }

    public static void 토큰과_상태코드_200을_응답하는지_검증한다(ExtractableResponse<Response> response) {
        Assertions.assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(200),
                () -> assertThat(response.jsonPath().getString("accessToken")).isNotNull(),
                () -> assertThat(response.jsonPath().getString("refreshToken")).isNotNull()
        );
    }

    public static void 상태코드가_200이고_새로운_토큰이_발급됐음을_검증한다(
            ExtractableResponse<Response> reIssueResponse, ExtractableResponse<Response> loginResponse
    ) {

        String loginAccessToken = loginResponse.jsonPath().getString("accessToken");
        String loginRefreshToken = loginResponse.jsonPath().getString("refreshToken");

        String reIssuedAccessToken = reIssueResponse.jsonPath().getString("accessToken");
        String reIssuedRefreshToken = reIssueResponse.jsonPath().getString("refreshToken");

        Assertions.assertAll(
                () -> 상태코드를_검증한다(reIssueResponse, HttpStatus.OK),
                () -> assertThat(reIssuedAccessToken).isNotEqualTo(loginAccessToken),
                () -> assertThat(reIssuedRefreshToken).isNotEqualTo(loginRefreshToken)
        );
    }

    public static void 상태코드가_400이고_오류코드가_a002임을_검증한다(ExtractableResponse<Response> response) {
        Assertions.assertAll(
                () -> 상태코드를_검증한다(response, HttpStatus.BAD_REQUEST),
                () -> 오류코드를_검증한다(response, "a002")
        );
    }

    public static void 상태코드가_204임을_검증한다(ExtractableResponse<Response> response) {
        상태코드를_검증한다(response, HttpStatus.NO_CONTENT);
    }

    public static ExtractableResponse<Response> 로그아웃_한다(String accessToken, RequestSpecification spec) {
        return RestAssured
                .given().log().all()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .spec(spec)
                .auth().oauth2(accessToken)
                .when().get("/api/members/logout")
                .then().log().all()
                .extract();
    }

    private static AbstractStringAssert<?> 오류코드를_검증한다(ExtractableResponse<Response> response, String code) {
        return assertThat(response.jsonPath().getString("code")).isEqualTo(code);
    }

    private static AbstractIntegerAssert<?> 상태코드를_검증한다(
            ExtractableResponse<Response> response,
            HttpStatus expectedHttpStatus
    ) {
        return assertThat(response.statusCode()).isEqualTo(expectedHttpStatus.value());
    }

}
