package com.baidu.shop.mapper;
import com.baidu.shop.entity.BrandEntity;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ClassName BrandMapper
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2020/12/25
 * @Version V1.0
 **/
public interface BrandMapper extends Mapper<BrandEntity>, SelectByIdListMapper<BrandEntity,Integer> {
}
