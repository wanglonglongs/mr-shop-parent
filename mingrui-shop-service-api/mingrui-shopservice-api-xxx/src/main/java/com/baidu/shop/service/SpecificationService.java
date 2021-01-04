package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.SpecGroupDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName SpecificationService
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/1/4
 * @Version V1.0
 **/
@Api(tags = "规格接口")
public interface SpecificationService {
    @ApiOperation(value = "通过条件查询规格组")
    @GetMapping(value = "specgroup/getSpecGroupInfo")
    Result<List<SpecGroupEntity>> getSepcGroupInfo(SpecGroupDTO specGroupDTO);
    @ApiOperation(value = "新增规格组名称")
    @PostMapping(value = "specgroup/saveOrUpdate")
    Result<JSONObject> saveSepcGroupName(@RequestBody SpecGroupDTO specGroupDTO);
    @ApiOperation(value = "修改规格组名称")
    @PutMapping(value = "specgroup/saveOrUpdate")
    Result<JSONObject> updateSepcGroupName(@RequestBody SpecGroupDTO specGroupDTO);
    @ApiOperation(value = "删除规格组")
    @DeleteMapping(value = "specgroup/deleteById")
    Result<JSONObject> deleteSepcGroup(Integer id);
}
