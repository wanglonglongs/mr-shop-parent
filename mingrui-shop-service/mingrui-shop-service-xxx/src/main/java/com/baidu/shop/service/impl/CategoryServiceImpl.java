package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.service.CategoryService;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2020/12/22
 * @Version V1.0
 **/
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;


    @Override
    public Result<List<CategoryEntity>> getCateByIds(String cateIds) {

        List<Integer> cateIdsList = Arrays.asList(cateIds.split(",")).stream().map(idStr -> Integer.valueOf(idStr)).collect(Collectors.toList());
        List<CategoryEntity> list = categoryMapper.selectByIdList(cateIdsList);
        return this.setResultSuccess(list);
    }

    @Override
    public Result<List<CategoryEntity>> getByBrand(Integer brandId) {
        List<CategoryEntity> list = categoryMapper.getByBrandId(brandId);
        return this.setResultSuccess(list);
    }

    @Override
    @Transactional
    public Result<JsonObject> saveCategory(CategoryEntity entity) {
        CategoryEntity saveCategoryEntity = new CategoryEntity();
        saveCategoryEntity.setIsParent(1);
        saveCategoryEntity.setId(entity.getParentId());
        categoryMapper.updateByPrimaryKeySelective(saveCategoryEntity);


        categoryMapper.insertSelective(entity);

        return this.setResultSuccess();
    }

    @Override
    @Transactional
    public Result<JsonObject> updateCategory(CategoryEntity entity) {
        categoryMapper.updateByPrimaryKeySelective(entity);
        return this.setResultSuccess();
    }

    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setParentId(pid);
        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    @Override
    //???????????????
    @Transactional
    public Result<JsonObject> deleteCategoryById(Integer id) {

        //?????????Id????????????,???????????????????????????????????????return???
        if(ObjectUtil.isNull(id) || id < 0) return this.setResultError("id?????????");

        //??????id??????????????????????????????
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);

        //????????????????????????????????????????????? ????????????????????????
        Example example1 = new Example(CategoryBrandEntity.class);
        example1.createCriteria().andEqualTo("categoryId",id);
        List<CategoryBrandEntity> categoryBrandEntities = categoryBrandMapper.selectByExample(example1);
        if (categoryBrandEntities.size() >= 1) return this.setResultError("???????????????????????????");


        //????????????????????????????????????null ??????????????????????????????????????????Id ???????????????????????????
        if (ObjectUtil.isNull(categoryEntity)) return this.setResultError("??????????????????");

        //??????????????????????????????????????????????????????????????????????????????
        if(categoryEntity.getParentId() == 1)return this.setResultError("???????????????Id????????????");

        //????????????????????????Id???????????????????????????
        Example example = new Example(CategoryEntity.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("parentId",categoryEntity.getParentId());
        List<CategoryEntity> categoryEntitiesList = categoryMapper.selectByExample(example);

        //????????????????????????Id?????????????????????????????????????????????1?????????????????? ??????????????????????????????
        if(categoryEntitiesList.size() <= 1){
            //?????????????????????????????????parentId ????????????????????????????????????//????????????????????????????????????????????????????????????
            CategoryEntity updateCategoryEntity = new CategoryEntity();
            //isParentId??????????????????????????????
            updateCategoryEntity.setIsParent(0);
            //?????????????????????0??????????????????setId
            updateCategoryEntity.setId(categoryEntity.getParentId());
            //update
            categoryMapper.updateByPrimaryKeySelective(updateCategoryEntity);
        }

        //??????????????????
        categoryMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }
}
