package com.baidu.shop.utils;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.elasticsearch.core.SearchHit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName HighLightUtil
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/3/3
 * @Version V1.0
 **/
public class HighLightUtil {

    public static HighlightBuilder getHighlightBuilder(String ...field){
        HighlightBuilder highlightBuilder = new HighlightBuilder();

        Arrays.asList(field).stream().forEach(s -> {
            highlightBuilder.field(s);
            highlightBuilder.preTags("<font style='color:red'>");
            highlightBuilder.postTags("</font>");

        });

        return highlightBuilder;
    }


    public static <T> List<T> getHighlightList(List<SearchHit<T>> searchHits){

        //修改title
        return searchHits.stream().map(entitySearchHit -> {
            T content = entitySearchHit.getContent();

            Map<String, List<String>> highlightFields = entitySearchHit.getHighlightFields();

            highlightFields.forEach((key,value)->{

                try {
                    Method method = content.getClass().getMethod("set" + firstCharUpper(key), String.class);

                    method.invoke(content,value.get(0));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            });

            return content;
        }).collect(Collectors.toList());

    }


    public static String firstCharUpper(String str){
        char[] chars = str.toCharArray();

        chars[0] -= 32;

        return String.valueOf(chars);
    }


}
