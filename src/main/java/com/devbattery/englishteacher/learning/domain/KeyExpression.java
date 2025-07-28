package com.devbattery.englishteacher.learning.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JsonTypeHandler 이용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class KeyExpression {

    private String expression;
    private String meaning;

}
