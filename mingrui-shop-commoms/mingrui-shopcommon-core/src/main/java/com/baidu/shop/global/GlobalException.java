package com.baidu.shop.global;

import com.baidu.shop.base.Result;
import com.baidu.shop.status.HTTPStatus;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName GlobalException
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2020/12/24
 * @Version V1.0
 **/
@RestControllerAdvice
@Slf4j
public class GlobalException {

    @ExceptionHandler(RuntimeException.class)
    public Result<JsonObject> RuntimeException(RuntimeException e){
        Result<JsonObject> result = new Result<>();
        result.setCode(HTTPStatus.PARAMS_VALIDATE_ERROR);
        result.setMessage(e.getMessage());
        log.error(e.getMessage());
        return result;
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String,Object> MethodArgumentNotValidException(MethodArgumentNotValidException e){
        Map<String, Object> map = new HashMap<>();
        e.getBindingResult().getAllErrors();
        log.error(e.getMessage());
        map.put("code",HTTPStatus.PARAMS_VALIDATE_ERROR);
        return map;
    }

//    @ExceptionHandler(value= MethodArgumentNotValidException.class)
//    public Map<String,Object> methodArgumentNotValidHandler(MethodArgumentNotValidException exception) throws Exception{
//        // == ===区别???
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("code",HTTPStatus.PARAMS_VALIDATE_ERROR);
//
//        /*String message = "";
//
//        //按需重新封装需要返回的错误信息
//        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
//            message += "Field --> " + error.getField() + " : " + error.getDefaultMessage() + ",";
//            log.error("Field --> " + error.getField() + " : " + error.getDefaultMessage());
//        }
//        map.put("massage",message.substring(0,message.lastIndexOf(",")));
//        */
//
//        List<String> msgList = new ArrayList<>();
//
//        /*for (FieldError error : exception.getBindingResult().getFieldErrors()) {
//            msgList.add("Field --> " + error.getField() + " : " + error.getDefaultMessage());
//            log.error("Field --> " + error.getField() + " : " + error.getDefaultMessage());
//        }*/
//        exception.getBindingResult().getFieldErrors().stream().forEach(error -> {
//            msgList.add("Field --> " + error.getField() + " : " + error.getDefaultMessage());
//            log.error("Field --> " + error.getField() + " : " + error.getDefaultMessage());
//        });
//
//        //ArrayList 是线程不安全的 -->
//        //hadoop --> HDFS(存储数据\文件) mapreduce(计算)
//        //reverse   //gc --> gc垃圾回收器 ps + po
//        String message = msgList.parallelStream().collect(Collectors.joining(","));
//
//        map.put("massage",message);
//        return map;
//    }

}
