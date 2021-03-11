package com.baidu.shop.mapper;

import com.baidu.shop.entity.SkuDTO;
import com.baidu.shop.entity.SkuEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @ClassName SkuMapper
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/1/7
 * @Version V1.0
 **/
public interface SkuMapper extends Mapper<SkuEntity>,DeleteByIdListMapper<SkuEntity,Long> {
    @Select(value = "SELECT k.*,t.`stock` FROM `tb_stock` t,`tb_sku` k WHERE k.`id` = t.`sku_id` AND k.`spu_id` = #{spuId}")
    List<SkuDTO> stockAndSkuList(Integer spuId);
}
