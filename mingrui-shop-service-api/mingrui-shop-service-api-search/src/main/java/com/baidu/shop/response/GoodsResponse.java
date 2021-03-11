package com.baidu.shop.response;

import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.status.HTTPStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsResponse
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/3/6
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
public class GoodsResponse extends Result<List<GoodsDoc>> {

    private Long total;

    private Long totalPage;

    private List<CategoryEntity> categoryList;

    private List<BrandEntity> brandList;

    private Map<String, List<String>> specMap;

    public GoodsResponse(Long total,long totalPage,List<CategoryEntity> categoryList,List<BrandEntity> brandList,List<GoodsDoc> goodsDocList,Map<String, List<String>> specMap){

        super(HTTPStatus.OK,"",goodsDocList);
        this.total = total;

        this.totalPage = totalPage;

        this.categoryList = categoryList;

        this.brandList = brandList;

        this.specMap = specMap;


    }

}
