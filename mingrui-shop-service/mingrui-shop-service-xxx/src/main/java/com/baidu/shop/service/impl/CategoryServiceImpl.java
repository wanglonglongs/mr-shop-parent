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

import java.util.List;

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
    //事务的特性
    @Transactional
    public Result<JsonObject> deleteCategoryById(Integer id) {

        //先判断Id是否合法,合法执行下面代码不合法直接return；
        if(ObjectUtil.isNull(id) || id < 0) return this.setResultError("id不合法");

        //通过id查询出当前节点的状态
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);

        //查看当前节点是否有绑定分类信息 如果有则不能删除
        Example example1 = new Example(CategoryBrandEntity.class);
        example1.createCriteria().andEqualTo("categoryId",id);
        List<CategoryBrandEntity> categoryBrandEntities = categoryBrandMapper.selectByExample(example1);
        if (categoryBrandEntities.size() >= 1) return this.setResultError("有绑定信息不能删除");


        //如果查询不到数据也会执行null 例如传一个大于数据库里数据的Id 这时候就有次问题了
        if (ObjectUtil.isNull(categoryEntity)) return this.setResultError("没有这条数据");

        //查看当前节点是否为父级节点，如果是父级节点则不能删除
        if(categoryEntity.getParentId() == 1)return this.setResultError("当前为父级Id不能删除");

        //查看当前节点的父Id下是否有其他子节点
        Example example = new Example(CategoryEntity.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("parentId",categoryEntity.getParentId());
        List<CategoryEntity> categoryEntitiesList = categoryMapper.selectByExample(example);

        //如果当前节点的父Id下有其他子节点的个数小于或等于1个的时候执行 大于一个节点这不执行
        if(categoryEntitiesList.size() <= 1){
            //当前节点删除后，父节点parentId 变成字节点（即叶子节点）//原因！！因为变成叶子节点后才可以被删除点
            CategoryEntity updateCategoryEntity = new CategoryEntity();
            //isParentId为零则就不是父节点了
            updateCategoryEntity.setIsParent(0);
            //再把已经修改为0的节点赋值到setId
            updateCategoryEntity.setId(categoryEntity.getParentId());
            //update
            categoryMapper.updateByPrimaryKeySelective(updateCategoryEntity);
        }

        //执行删除操作
        categoryMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }
}
