package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName CategoryService
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2020/12/22
 * @Version V1.0
 **/
@Api(tags = "商品分类接口")
public interface CategoryService {

    @ApiOperation(value = "通过品牌id查询商品分类")
    @GetMapping(value = "category/getByBrand")
    Result<List<CategoryEntity>> getByBrand(Integer brandId);

    @ApiOperation(value = "通过查询商品分类")
    @GetMapping(value = "category/list")

    Result<List<CategoryEntity>> getCategoryByPid(Integer pid);

    @ApiOperation(value = "通过查询Id删除分类")
    @DeleteMapping(value = "category/list")
    Result<JsonObject> deleteCategoryById(Integer id);

    @ApiOperation(value = "改变")
    @PutMapping(value = "category/update")
        //声明哪个组下面的参数参加校验-->当前是校验改变
    Result<JsonObject> updateCategory(@Validated({MingruiOperation.Update.class}) @RequestBody CategoryEntity entity);

    @ApiOperation(value = "新增")
    @PostMapping(value = "category/save")
        //声明哪个组下面的参数参加校验-->当前是校验新增组
    Result<JsonObject> saveCategory(@Validated({MingruiOperation.Add.class})@RequestBody CategoryEntity entity);

}
