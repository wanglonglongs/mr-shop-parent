package com.baidu.shop.base;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName Result
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2020/12/22
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
public class Result<T> {

    private Integer code;//返回码

    private String message;//返回信息

    private T data;//返回的数据

    public Result(Integer code,String message,Object data){
        this.code = code;
        this.message = message;
        this.data = (T) data;
    }

}
