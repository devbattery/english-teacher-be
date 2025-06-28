package com.devbattery.englishteacher.learning.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KeyExpression {

    private String expression;
    private String meaning;

}
