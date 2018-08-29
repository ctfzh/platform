package com.fulu.game.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlatformEcoEnum implements TypeEnum<Integer> {


    PLAY(1, "开黑陪玩"),
    POINT(2, "开黑上分"),

    APP(4, "APP");


    private Integer type;
    private String msg;

}