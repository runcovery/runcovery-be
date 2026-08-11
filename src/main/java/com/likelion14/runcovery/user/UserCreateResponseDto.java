package com.likelion14.runcovery.user;

import lombok.Getter;

@Getter
public class UserCreateResponseDto {

    private Long userId;
    private String nickname;

    public UserCreateResponseDto(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
    }
}
