package com.baidu.shop.service;


import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName BrandService
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2020/12/25
 * @Version V1.0
 **/
@Api(tags = "品牌管理接口")
public interface BrandService {
    @ApiOperation("查询品牌")
    @GetMapping("brand/list")
    Result<PageInfo<BrandEntity>> query(BrandDTO brandDTO);

    @ApiOperation("新增品牌")
    @PostMapping("brand/save")
    Result<JSONObject> save(@Validated({MingruiOperation.Add.class})@RequestBody BrandDTO brandDTO);

    @ApiOperation("修改品牌")
    @PutMapping("brand/save")
    Result<JSONObject> update(@Validated({MingruiOperation.Update.class})@RequestBody BrandDTO brandDTO);

    @ApiOperation("删除品牌")
    @DeleteMapping("brand/deleteById")
    Result<JSONObject> deleteById(Integer id);

    @ApiOperation("查询品牌")
    @GetMapping("/brand/getBrandInfoByCategoryId")
    Result<List<BrandEntity>> getBrandInfoByCategoryId(Integer cid);
}
