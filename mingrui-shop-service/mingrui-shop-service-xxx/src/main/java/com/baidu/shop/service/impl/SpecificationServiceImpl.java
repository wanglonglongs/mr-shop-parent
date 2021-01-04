package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.SpecGroupDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.service.SpecificationService;
import com.baidu.shop.utils.BeanCopy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @ClassName SpecificationServiceImpl
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/1/4
 * @Version V1.0
 **/
@RestController
public class SpecificationServiceImpl extends BaseApiService implements SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Override
    @Transactional
    public Result<JSONObject> deleteSepcGroup(Integer id) {
        specGroupMapper.deleteByPrimaryKey(id);
        return setResultSuccess();
    }

    @Override
    @Transactional
    public Result<JSONObject> updateSepcGroupName(SpecGroupDTO specGroupDTO) {
        specGroupMapper.updateByPrimaryKeySelective(BeanCopy.copyProperties(specGroupDTO,SpecGroupEntity.class));
        return setResultSuccess();
    }

    @Override
    @Transactional
    public Result<JSONObject> saveSepcGroupName(SpecGroupDTO specGroupDTO) {
        specGroupMapper.insertSelective(BeanCopy.copyProperties(specGroupDTO,SpecGroupEntity.class));
        return setResultSuccess();
    }

    @Override
    public Result<List<SpecGroupEntity>> getSepcGroupInfo(SpecGroupDTO specGroupDTO) {
        Example example = new Example(SpecGroupEntity.class);
        example.createCriteria().andEqualTo("cid",specGroupDTO.getCid());
        List<SpecGroupEntity> list = specGroupMapper.selectByExample(example);

        return setResultSuccess(list);
    }
}
