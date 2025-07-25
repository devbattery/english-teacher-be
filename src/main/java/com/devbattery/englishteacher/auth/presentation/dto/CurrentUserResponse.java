package com.devbattery.englishteacher.auth.presentation.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record CurrentUserResponse(Long id, String email, String name,
                                  String picture, List<String> authorities) {

}
