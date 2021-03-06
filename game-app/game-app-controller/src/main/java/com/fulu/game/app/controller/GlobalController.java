package com.fulu.game.app.controller;


import com.fulu.game.common.Result;
import com.fulu.game.common.utils.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/api/v1/global")
@Slf4j
public class GlobalController extends BaseController {

    @Autowired
    private OssUtil ossUtil;


    @PostMapping(value = "upload")
    public Result upload(@RequestParam("file") MultipartFile file, String name) throws Exception {
        String fileName = ossUtil.uploadFile(file.getInputStream(), file.getOriginalFilename());
        return Result.success().data(fileName).msg(name);
    }


}
