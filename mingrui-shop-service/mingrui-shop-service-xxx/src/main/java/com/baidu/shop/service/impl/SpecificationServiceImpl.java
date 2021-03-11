package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.SpecGroupDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamDTO;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.mapper.SpecParamMapper;
import com.baidu.shop.service.SpecificationService;
import com.baidu.shop.utils.BeanCopy;
import com.baidu.shop.utils.ObjectUtil;
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

    @Autowired
    private SpecParamMapper specParamMapper;


    @Override
    @Transactional
    public Result<JSONObject> specparamUpdate(SpecParamDTO specParamDTO) {
        specParamMapper.updateByPrimaryKeySelective(BeanCopy.copyProperties(specParamDTO,SpecParamEntity.class));
        return this.setResultSuccess();
    }

    @Override
    @Transactional
    public Result<JSONObject> specparamDelete(Integer id) {
        Example example = new Example(SpecParamEntity.class);
        example.createCriteria().andEqualTo("groupId",id);
        List<SpecParamEntity> list = specParamMapper.selectByExample(example);
        if (list.size() >= 1) return this.setResultError("有绑定不能删除");
        specParamMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }

    @Override
    @Transactional
    public Result<JSONObject> specparamSave(SpecParamDTO specParamDTO) {
        specParamMapper.insertSelective(BeanCopy.copyProperties(specParamDTO,SpecParamEntity.class));
        return this.setResultSuccess();
    }

    @Override
    public Result<List<SpecParamEntity>> specparamList(SpecParamDTO specParamDTO) {
        SpecParamEntity specParamEntity = BeanCopy.copyProperties(specParamDTO, SpecParamEntity.class);

        Example example = new Example(SpecParamEntity.class);
        Example.Criteria criteria = example.createCriteria();

        if(ObjectUtil.isNotNull(specParamDTO.getGroupId()));
        criteria.andEqualTo("groupId",(specParamEntity.getGroupId()));


        if(ObjectUtil.isNotNull(specParamDTO.getCid()));
        criteria.andEqualTo("cid",specParamEntity.getCid());

        if(ObjectUtil.isNotNull(specParamDTO.getGeneric())){
            criteria.andEqualTo("generic",specParamEntity.getGeneric());
        }

        List<SpecParamEntity> list = specParamMapper.selectByExample(example);
        return setResultSuccess(list);
    }

    //--------------------------------------------------------------

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
