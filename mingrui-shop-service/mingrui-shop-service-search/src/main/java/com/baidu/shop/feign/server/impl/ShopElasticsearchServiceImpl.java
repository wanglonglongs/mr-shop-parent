package com.baidu.shop.feign.server.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.server.ShopElasticsearchService;
import com.baidu.shop.utils.HighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ShopElasticsearchServiceImpl
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/3/4
 * @Version V1.0
 **/
@RestController
@Slf4j
public class ShopElasticsearchServiceImpl extends BaseApiService implements ShopElasticsearchService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private BrandFeign brandFeign;

    @Autowired
    private CategoryFeign categoryFeign;


    @Override
    public Result<JSONObject> saveData(Integer spuId) {
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        List<GoodsDoc> goodsDocs = this.esGoodsInfo(spuDTO);

        GoodsDoc goodsDoc = goodsDocs.get(0);

        elasticsearchRestTemplate.save(goodsDoc);

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> delData(Integer pId) {
        GoodsDoc goodsDoc = new GoodsDoc();
        goodsDoc.setId(pId.longValue());
        elasticsearchRestTemplate.delete(goodsDoc);
        return this.setResultSuccess();
    }

    //搜索
    @Override
    public GoodsResponse search(String search, Integer page,String filter) {

        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(this.getNativeSearchQueryBuilder(search,page,filter).build(), GoodsDoc.class);

        List<GoodsDoc> goodsDocs = HighLightUtil.getHighlightList(searchHits.getSearchHits());

        long total = searchHits.getTotalHits();
        long totalPage  = total/10;
        if (totalPage % 10 > 0){
            totalPage++;
        }

//        Map<Integer, List<CategoryEntity>>  map = this.getCategoryListByBucket(searchHits.getAggregations());
//
//        Integer hotCid = 0;
//        List<CategoryEntity> categoryList = null;
//        for (Map.Entry<Integer,List<CategoryEntity>> entry : map.entrySet()){
//            Integer key = entry.getKey();
//            List<CategoryEntity> value = entry.getValue();
//        }

        Map<Integer, List<CategoryEntity>> map = this.getCategoryListByBucket(searchHits.getAggregations());

        Integer hotCid = 0;
        List<CategoryEntity> categoryList = null;
        for(Map.Entry<Integer, List<CategoryEntity>> entry : map.entrySet()){
            hotCid = entry.getKey();
            categoryList = entry.getValue();
        }

        //获取聚合信息

        Aggregations aggregations = searchHits.getAggregations();

//        Map<String, Long> msgMap = new HashMap<>();
//
//        msgMap.put("total",total);
//        msgMap.put("totalPage",totalPage);

        return new GoodsResponse(total,totalPage,categoryList,this.getBrandListByBucket(aggregations),goodsDocs,this.getSpecMap(hotCid,search));
    }


    //参数
    private Map<String, List<String>> getSpecMap(Integer hotCid,String search){
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(hotCid);
        specParamDTO.setSearching(true);
        Result<List<SpecParamEntity>> specParamInfo = specificationFeign.specparamList(specParamDTO);
        Map<String, List<String>> specMap = new HashMap<>();
        if (specParamInfo.isSuccess()) {

            List<SpecParamEntity> specParamList = specParamInfo.getData();

            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            nativeSearchQueryBuilder.withQuery(
                    QueryBuilders.multiMatchQuery(search,"title","brandName","categoryName")
            );
            nativeSearchQueryBuilder.withPageable(PageRequest.of(0,1));
            specParamList.stream().forEach(specParam -> {
                nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(specParam.getName())
                        .field("specs." + specParam.getName() + ".keyword"));
            });

            SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), GoodsDoc.class);
            Aggregations aggregations = searchHits.getAggregations();

            specParamList.stream().forEach(specParam -> {

                Terms aggregation = aggregations.get(specParam.getName());
                List<? extends Terms.Bucket> buckets = aggregation.getBuckets();
                List<String> valueList = buckets.stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());

                specMap.put(specParam.getName(),valueList);
            });
        }

        return specMap;
    }

    //获取native信息
    private NativeSearchQueryBuilder getNativeSearchQueryBuilder(String search,Integer page,String filter) {

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //多字段同时查询

        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(search, "title", "brandName", "categoryName"));

        //过滤查询
        if (!StringUtils.isEmpty(filter) && filter.length() >2){

            Map<String, String> filterMap = JSONUtil.toMapValueString(filter);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();


            //判断key是否为cid3和brandId
            filterMap.forEach((key,value)->{
                MatchQueryBuilder matchQueryBuilder = null;
                if (key.equals("cid3") || key.equals("brandId")){
                    matchQueryBuilder = QueryBuilders.matchQuery(key,value);
                }else {
                    matchQueryBuilder = QueryBuilders.matchQuery("specs." + key + ".keyword", value);
                }
                boolQueryBuilder.must(matchQueryBuilder);
            });
                nativeSearchQueryBuilder.withFilter(boolQueryBuilder);

//            nativeSearchQueryBuilder.withFilter(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("spec.分辨率.keyword","分辨率"))
//                    .must(QueryBuilders.matchQuery("brandId","8557"))
//            );







        }


        //一点点小瑕疵
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "title", "skus"}, null));

        //设置分页
        nativeSearchQueryBuilder.withPageable(PageRequest.of(page - 1, 10));


        //聚合为桶

        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("agg_category").field("cid3"));

        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("agg_brand").field("brandId"));


        //设置高亮字段
        nativeSearchQueryBuilder.withHighlightBuilder(HighLightUtil.getHighlightBuilder("title"));

        return nativeSearchQueryBuilder;

    }
    //分类list
    private Map<Integer,List<CategoryEntity>> getCategoryListByBucket(Aggregations aggregations){

        Terms agg_category = aggregations.get("agg_category");


        List<? extends Terms.Bucket> categoryBuckets = agg_category.getBuckets();


        //        List<Integer> integers = Arrays.asList(0);
//        ArrayList<Long> docCount = new ArrayList<>();
//        docCount.add(0L);
//
//        ArrayList<Integer> hotCid  = new ArrayList<>();
//        hotCid.add(0);
        List<Long> docCount = Arrays.asList(0L);

        List<Integer> hotCid = Arrays.asList(0);

        List<String> categoryIdList = categoryBuckets.stream().map(categoryBucket -> {

            if (categoryBucket.getDocCount() > docCount.get(0)) {
                docCount.set(0, categoryBucket.getDocCount());
                hotCid.set(0, categoryBucket.getKeyAsNumber().intValue());
            }

            return categoryBucket.getKeyAsNumber().longValue() + "";

        }).collect(Collectors.toList());

        //要将List<Long>转成String类型的字符串并且用,拼接
        Result<List<CategoryEntity>> cateResult = categoryFeign.getCateByIds(String.join(",", categoryIdList));

        List<CategoryEntity> cateList = null;

        if (cateResult.isSuccess()){
            cateList =  cateResult.getData();
        }
        Map<Integer,List<CategoryEntity>> map = new HashMap<>();

        map.put(hotCid.get(0),cateList);

        return map;
    }

    //通过聚合得到品牌List
    private List<BrandEntity> getBrandListByBucket(Aggregations aggregations){

        Terms agg_brand = aggregations.get("agg_brand");

        List<? extends Terms.Bucket> brandBuckets = agg_brand.getBuckets();


        List<String> brandIdList = brandBuckets.stream().map(brandBucket -> brandBucket.getKeyAsNumber().longValue()+"").collect(Collectors.toList());


        Result<List<BrandEntity>> brandResult = brandFeign.getBrandByIds(String.join(",", brandIdList));

        List<BrandEntity> brandList = brandResult.getData();

        return brandList;
    }

    //删除索引
    @Override
    public Result<JSONObject> clearGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);

        indexOperations.delete();

        System.out.println("删除索引成功");


        return this.setResultSuccess();
    }

//    @Override
    public List<GoodsDoc> esGoodsInfo(SpuDTO spuDTO) {
//        SpuDTO spuDTO = new SpuDTO();

        spuDTO.setRows(0);
        spuDTO.setPage(10);

        Result<List<SpuDTO>> spuInfo  = goodsFeign.queryGoods(spuDTO);
        log.info("goodsFeign.getSpuInfo ----->{}",spuInfo);

        if (spuInfo.isSuccess()){
            List<SpuDTO> spuList  = spuInfo.getData();

            List<GoodsDoc> goodsDocs = spuList.stream().map(spu -> {
                //spu
                GoodsDoc goodsDoc = new GoodsDoc();
                goodsDoc.setId(spu.getId().longValue());
                goodsDoc.setSubTitle(spu.getSubTitle());
                goodsDoc.setTitle(spu.getTitle());
                goodsDoc.setBrandId(spu.getBrandId().longValue());
                goodsDoc.setBrandName(spu.getBrandName());
                goodsDoc.setCategoryName(spu.getCategoryName());
                goodsDoc.setCid1(spu.getCid1().longValue());
                goodsDoc.setCid2(spu.getCid2().longValue());
                goodsDoc.setCid3(spu.getCid3().longValue());
                goodsDoc.setCreateTime(spu.getCreateTime());



                //sku
                Result<List<SkuEntity>> skuInfo = goodsFeign.stockandskuList(spu.getId());

                if (skuInfo.isSuccess()) {

                    List<SkuEntity> data = skuInfo.getData();

                    ArrayList<Long> priceList = new ArrayList<>();

                    List<Map<String, Object>> skuMapLis = data.stream().map(sku -> {
                        Map<String, Object> map = new HashMap<>();

                        map.put("id",sku.getId());
                        map.put("price",sku.getPrice());
                        map.put("images",sku.getImages());
                        map.put("title",sku.getTitle());

                        priceList.add(sku.getPrice().longValue());

                        return map;
                    }).collect(Collectors.toList());

                    goodsDoc.setPrice(priceList);
                    goodsDoc.setSkus(JSONUtil.toJsonString(skuMapLis));

                }


                //通过cid3查询规格参数,searching为true
                SpecParamDTO specParamDTO = new SpecParamDTO();

                //参考数据库字段
                specParamDTO.setCid(spu.getCid3());
                specParamDTO.setSearching(true);

                Result<List<SpecParamEntity>> specParamInfo = specificationFeign.specparamList(specParamDTO);
                if (specParamInfo.isSuccess()) {

                    List<SpecParamEntity> specParamList = specParamInfo.getData();

                    Result<SpuDetailEntity> spuDetailInfo = goodsFeign.goodsSpuDetail(spu.getId());

                    if (spuDetailInfo.isSuccess()) {

                        SpuDetailEntity spuDetailEntity = spuDetailInfo.getData();

                        //将json字符串转换成map集合
                        Map<String, String> genericSpec = JSONUtil.toMapValueString(spuDetailEntity.getGenericSpec());

                        Map<String, List<String>> specialSpec = JSONUtil.toMapValueStrList(spuDetailEntity.getSpecialSpec());

                        //需要查询两张表的数据 spec_param(规格参数名) spu_detail(规格参数值) --> 规格参数名 : 规格参数值

                        Map<String, Object> specMap = new HashMap<>();

                        specParamList.stream().forEach(specParam -> {

                            if (specParam.getGeneric()) {
                                if (specParam.getNumeric() && !StringUtils.isEmpty(specParam.getSearching())) {
                                    specMap.put(specParam.getName(), chooseSegment(genericSpec.get(specParam.getId() + ""), specParam.getSegments(), specParam.getUnit()));
                                } else {
                                    specMap.put(specParam.getName(), genericSpec.get(specParam.getId() + ""));
                                }

                            } else {
                                specMap.put(specParam.getName(), specialSpec.get(specParam.getId() + ""));
                            }

                            goodsDoc.setSpecs(specMap);

                        });
                        return goodsDoc;
                    }

                }

                return goodsDoc;

            }).collect(Collectors.toList());

            return goodsDocs;
        }

        return null;
    }



    private String chooseSegment(String value, String segments, String unit) {//800 -> 5000-1000
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + unit + "以上";
                }else if(begin == 0){
                    result = segs[1] + unit + "以下";
                }else{
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }


    //创建索引
    @Override
    public Result<JSONObject> initGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if (!indexOperations.exists()){

            indexOperations.create();

            indexOperations.createMapping();
        }

        //查询mysql中的数据
        List<GoodsDoc> goodsDocs = this.esGoodsInfo(new SpuDTO());


        elasticsearchRestTemplate.save(goodsDocs);

        return this.setResultSuccess();
    }

}
