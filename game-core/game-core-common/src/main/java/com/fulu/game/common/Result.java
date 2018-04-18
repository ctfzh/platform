package com.fulu.game.common;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 返回结果封装
 */
public class Result implements Serializable {

    private int status;

    private String msg ="";

    private Object data = null;

    public Result(int status) {
        this.status = status;
    }

    public Result() {
    }

    public Result msg(String msg) {
        this.msg = msg;
        return this;
    }


    public Result data(Object data) {
        this.data = data;
        return this;
    }

    public Result data(String key,Object data) {
        Map<String,Object> map = new HashMap<>();
        map.put(key, data);
        this.data = map;
        return this;
    }

    public static Result success() {
        Result result = new Result();
        result.setStatus(ResultStatus.SUCCESS);
        return result;
    }

    public static Result error() {
        Result result = new Result();
        result.setStatus(ResultStatus.ERROR);
        return result;
    }



    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}