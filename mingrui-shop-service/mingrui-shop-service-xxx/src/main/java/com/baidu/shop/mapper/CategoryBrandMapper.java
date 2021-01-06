package com.baidu.shop.mapper;

import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

import java.util.List;

/**
* @ClassName CategoryBrandMapper
* @Description: TODO
* @Author wanglonglong
* @Date 2020/12/28
* @Version V1.0
**/
public interface CategoryBrandMapper extends Mapper<CategoryBrandEntity>, InsertListMapper<CategoryBrandEntity> {
    @Select(value = "SELECT * FROM tb_brand b WHERE b.id IN(SELECT cb.brand_id  FROM tb_category_brand cb WHERE cb.category_id=#{cid})")
    List<BrandEntity> getBrandInfoByCategoryId(Integer cid);
}
