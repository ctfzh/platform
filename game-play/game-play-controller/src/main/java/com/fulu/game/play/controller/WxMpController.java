package com.fulu.game.play.controller;

import com.fulu.game.common.Result;
import com.fulu.game.core.service.VirtualPayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 微信公众号Controller
 *
 * @author Gong ZeChun
 * @date 2018/9/3 15:54
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/wx-mp")
public class WxMpController extends BaseController {

    @Autowired
    private VirtualPayOrderService virtualPayOrderService;


    @PostMapping("/charge")
    public Result charge(@RequestParam String code,
                         @RequestParam BigDecimal actualMoney,
                         @RequestParam Integer virtualMoney,
                         @RequestParam String mobile) {

        virtualPayOrderService.charge(code, actualMoney, virtualMoney, mobile);

        return null;
    }

}