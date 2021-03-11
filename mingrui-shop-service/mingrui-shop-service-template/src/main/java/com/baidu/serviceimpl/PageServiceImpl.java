package com.baidu.serviceimpl;

import com.baidu.feign.BrandFeign;
import com.baidu.feign.CategoryFeign;
import com.baidu.feign.GoodsFeign;
import com.baidu.feign.SpecificationFeign;
import com.baidu.service.PageService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.*;
import com.baidu.shop.utils.BeanCopy;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName PageServiceImpl
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/3/8
 * @Version V1.0
 **/
@Service
public class PageServiceImpl implements PageService {

//    @Autowired
    private BrandFeign brandFeign;

//    @Autowired
    private CategoryFeign categoryFeign;

//    @Autowired
    private GoodsFeign goodsFeign;

//    @Autowired
    private SpecificationFeign specificationFeign;

//    @Override
    public Map<String, Object> getGoodsInfo(Integer spuId) {
        Map<String, Object> goodsInfoMap = new HashMap<>();
        //获取spu信息
        SpuDTO spuDTO = new SpuDTO();

        spuDTO.setId(spuId);

        Result<List<SpuDTO>> spuResult = goodsFeign.queryGoods(spuDTO);
        SpuDTO spuResultData = null;
        if (spuResult.isSuccess()){
            spuResultData  = spuResult.getData().get(0);
            goodsInfoMap.put("spuInfo",spuResultData);
        }

        //spudetai信息特有
        Result<SpuDetailEntity> spuDetailResult  = goodsFeign.goodsSpuDetail(spuId);
        if (spuDetailResult.isSuccess()){
            SpuDetailEntity spuDetailInfo = spuDetailResult.getData();
            goodsInfoMap.put("spuDetailInfo",spuDetailInfo);
        }
        //查询分类信息
        Result<List<CategoryEntity>> categoryResult = categoryFeign.getCateByIds(
                String.join(",",Arrays.asList(spuResultData.getCid1()+"",spuResultData.getCid2()+"",spuResultData.getCid3()+"")));
        if (categoryResult.isSuccess()){
            List<CategoryEntity> categoryInfo = categoryResult.getData();
            goodsInfoMap.put("categoryInfo",categoryInfo);
        }
        //查询品牌信息
        BrandDTO brandDTO = new BrandDTO();

        brandDTO.setId(spuResultData.getBrandId());

        Result<PageInfo<BrandEntity>> brandResult = brandFeign.query(brandDTO);
        if (brandResult.isSuccess()){
            goodsInfoMap.put("brandInfo",brandResult.getData().getList().get(0));
        }

        //sku
        Result<List<SkuEntity>> skusInfo = goodsFeign.stockandskuList(spuId);

        if (skusInfo.isSuccess()){
            goodsInfoMap.put("skus",skusInfo.getData());
        }

        System.out.println(goodsInfoMap);
        //规格组和规格参数
        SpecGroupDTO specGroupDTO = new SpecGroupDTO();
        specGroupDTO.setCid(spuResultData.getCid3());
        Result<List<SpecGroupEntity>> specGroupResult = specificationFeign.getSepcGroupInfo(specGroupDTO);
        if (specGroupResult.isSuccess()){
            List<SpecGroupEntity> specGroupList = specGroupResult.getData();
            List<SpecGroupDTO> specGroupAndParam  = specGroupList.stream().map(specGroup -> {
                SpecGroupDTO specGroupDTO1 = BeanCopy.copyProperties(specGroup, SpecGroupDTO.class);
                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setGroupId(specGroupDTO1.getId());
                specParamDTO.setGeneric(true);
                Result<List<SpecParamEntity>> specParamResult = specificationFeign.specparamList(specParamDTO);
                if (specParamResult.isSuccess()) {
                    specGroupDTO1.setSpecList(specParamResult.getData());
                }
                return specGroupDTO1;
            }).collect(Collectors.toList());
            goodsInfoMap.put("specGroupAndParam",specGroupAndParam);
        }
        //特有规格参数
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spuResultData.getCid3());
        specParamDTO.setGeneric(false);
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.specparamList(specParamDTO);
        if (specParamResult.isSuccess()){
            List<SpecParamEntity> specParamEntityList = specParamResult.getData();
            HashMap<Integer, String> specParamMap  = new HashMap<>();
            specParamEntityList.stream().forEach(specParam -> specParamMap.put(specParam.getId(),specParam.getName()));
            goodsInfoMap.put("specParamMap",specParamMap);

        }
        return goodsInfoMap;
    }
}
