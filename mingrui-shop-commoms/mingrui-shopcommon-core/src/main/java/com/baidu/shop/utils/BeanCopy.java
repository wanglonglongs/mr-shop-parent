package com.baidu.shop.utils;

import org.springframework.beans.BeanUtils;

/**
 * @ClassName BeanCopy
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2020/12/25
 * @Version V1.0
 **/
public class BeanCopy {
    public static <T> T copyProperties(Object source,Class<T> clazz){
        try {
            T t = clazz.newInstance();
            BeanUtils.copyProperties(source,t);
            return t;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
