package com.baidu.serviceimpl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.feign.BrandFeign;
import com.baidu.feign.CategoryFeign;
import com.baidu.feign.GoodsFeign;
import com.baidu.feign.SpecificationFeign;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.*;
import com.baidu.shop.service.TemplateService;
import com.baidu.shop.utils.BeanCopy;
import com.baidu.shop.utils.ObjectUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TemplateServiceImpl
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/3/9
 * @Version V1.0
 **/
@RestController
public class TemplateServiceImpl extends BaseApiService implements TemplateService {


    private final Integer CREATE_STATIC_HTML = 1;

    private final Integer DELETE_STATIC_HTML = 2;

    @Autowired
    private BrandFeign brandFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private TemplateEngine templateEngine;

    @Value(value = "${mrshop.static.html.path}")
    private String htmlPath;

    @Override
    public Result<JSONObject> createStaticHTMLTemplate(Integer spuId) {

        //得到要渲染的数据
        Map<String, Object> goodsInfo = this.getGoodsInfo(spuId);

        //创建上下文
        Context context = new Context();
        context.setVariables(goodsInfo);

        //创建要生成的文件
        File file = new File(htmlPath, spuId + ".html");
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(file, "utf-8");
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }finally {
            if (ObjectUtil.isNotNull(writer))writer.close();
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> initStaticHTMLTemplate() {
        this.operationStaticHTML(CREATE_STATIC_HTML);
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> clearStaticHTMLTemplate() {
        this.operationStaticHTML(DELETE_STATIC_HTML);
        return this.setResultSuccess();
    }

    private Boolean operationStaticHTML(Integer operation){

        try {
            Result<List<SpuDTO>> spuInfo = goodsFeign.queryGoods(new SpuDTO());
            if (spuInfo.isSuccess()){
                spuInfo.getData().stream().forEach(spuDTO -> {
                    if (operation == 1){
                        this.createStaticHTMLTemplate(spuDTO.getId());
                    }else {
                        this.deleteStaticHTMLTemplate(spuDTO.getId());
                    }
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public Result<JSONObject> deleteStaticHTMLTemplate(Integer spuId) {
        File file = new File(htmlPath, spuId + ".html");
        if (file.exists()){
            file.delete();
        }
        return this.setResultSuccess();
    }


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
