package com.fulu.game.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TechAuthStatusEnum implements TypeEnum<Integer>{
    NO_AUTHENTICATION(0,"未通过"),
    AUTHENTICATION_ING(1,"待认证"),
    NORMAL(2,"已认证"),
    FREEZE(3,"冻结");

    private Integer type;
    private String msg;
}