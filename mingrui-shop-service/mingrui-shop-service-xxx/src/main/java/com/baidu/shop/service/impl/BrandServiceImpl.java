package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BeanCopy;
import com.baidu.shop.utils.PinyinUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName BrandServiceImpl
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2020/12/25
 * @Version V1.0
 **/
@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {
    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Override
    public Result<List<BrandEntity>> getBrandInfoByCategoryId(Integer cid) {

        List<BrandEntity> list = categoryBrandMapper.getBrandInfoByCategoryId(cid);

        return this.setResultSuccess(list);
    }

    @Override
    @Transactional
    public Result<JSONObject> deleteById(Integer id) {

        brandMapper.deleteByPrimaryKey(id);

        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        categoryBrandMapper.deleteByExample(example);

        return this.setResultSuccess();
    }

    @Override
    @Transactional
    public Result<JSONObject> update(BrandDTO brandDTO) {
        BrandEntity brandEntity = BeanCopy.copyProperties(brandDTO, BrandEntity.class);
        //因为name可能会修改，所以转换大写
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().toCharArray()[0]),false).toCharArray()[0]);
        brandMapper.updateByPrimaryKeySelective(brandEntity);

        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",brandEntity.getId());
        categoryBrandMapper.deleteByExample(example);

        this.insertCategoryBrandData(brandDTO.getCategories(),brandEntity.getId());

        return this.setResultSuccess();
    }

    @Override
    @Transactional
    public Result<JSONObject> save(BrandDTO brandDTO) {

        BrandEntity brandEntity = BeanCopy.copyProperties(brandDTO, BrandEntity.class);
        //处理品牌首字母
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().toCharArray()[0]), false).toCharArray()[0]);
        brandMapper.insertSelective(brandEntity);


        this.insertCategoryBrandData(brandDTO.getCategories(),brandEntity.getId());

        return this.setResultSuccess();
    }

    @Override
    public Result<PageInfo<BrandEntity>> query(BrandDTO brandDTO) {

        //分页
        PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());

        BrandEntity brandEntity = BeanCopy.copyProperties(brandDTO, BrandEntity.class);
        Example example = new Example(BrandEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if(!StringUtils.isEmpty(brandDTO.getSort()))
            // BrandEntity brandEntity = new BrandEntity();
//        brandDTO.setId(brandEntity.getId());
//        brandDTO.setName(brandEntity.getName());
            //排序
//        if (!StringUtil.isEmpty(brandDTO.getSort())){
//            String order = "asc";
//            if (Boolean.valueOf(brandDTO.getOrder())){
//                order = "desc";
//            }else{
//                order = "asc";
//            }
//        }
            PageHelper.orderBy(brandDTO.getSort() + " " + (Boolean.valueOf(brandDTO.getOrder()) ? "desc" : "asc"));

        criteria.andLike("name","%"+brandDTO.getName()+"%");

        List<BrandEntity> list = brandMapper.selectByExample(example);

        PageInfo<BrandEntity> pageInfo = new PageInfo<>(list);

        return this.setResultSuccess(pageInfo);
    }
    //公用批量增
    private void insertCategoryBrandData(String categories,Integer Id){


        if(StringUtils.isEmpty(categories)) throw new RuntimeException("请填写数据");//如果分类集合为空就返回


        List<CategoryBrandEntity> categoryBrandEntities = new ArrayList<>();

        //判断分类集合字符串中是否包含,
        if(categories.contains(",")){//多个分类 --> 批量新增
            String[] categoryArr = categories.split(",");

            for (String s : categoryArr) {
                CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
                categoryBrandEntity.setBrandId(Id);
                categoryBrandEntity.setCategoryId(Integer.valueOf(s));
                categoryBrandEntities.add(categoryBrandEntity);
            }
            //insertListMapper
            categoryBrandMapper.insertList(categoryBrandEntities);
        }else{//普通单个新增

            CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
            categoryBrandEntity.setBrandId(Id);
            categoryBrandEntity.setCategoryId(Integer.valueOf(categories));

            categoryBrandMapper.insertSelective(categoryBrandEntity);
        }

    }
    private void deleteCategoryBrandByBrandId(){

    }
}
