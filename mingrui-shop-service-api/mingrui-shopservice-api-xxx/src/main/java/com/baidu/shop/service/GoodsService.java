package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.*;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName GoodsService
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/1/5
 * @Version V1.0
 **/
@Api(tags = "商品接口")
public interface GoodsService {
    @ApiOperation(value = "查询")
    @GetMapping(value = "goods/getSpuInfo")
    Result<List<SpuDTO>> queryGoods(@SpringQueryMap SpuDTO spuDTO);

    @ApiOperation(value = "新增")
    @PostMapping(value = "goods/getSpuSave")
    Result<JSONObject> saveGoods(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "修改")
    @PutMapping(value = "goods/getSpuSave")
    Result<JSONObject> updateGoods(@RequestBody SpuDTO spuDTO);


    @ApiOperation(value = "查询品牌")
    @GetMapping(value = "goods/spu/detail")
    Result<SpuDetailEntity> goodsSpuDetail(@RequestParam Integer spuId);

    @ApiOperation(value = "查询库存sku")
    @GetMapping(value = "goods/stockandsku/")
    Result<List<SkuEntity>> stockandskuList(@RequestParam Integer spuId);

    @ApiOperation(value = "删除")
    @DeleteMapping(value = "GoodsDeletes")
    Result<JSONObject> GoodsDeletes(Integer pId);

    @ApiOperation(value = "上下架")
    @PutMapping(value = "goods/updateSaleable")
    Result<JSONObject> updateSaleable(@RequestBody SpuDTO spuDTO);

}
