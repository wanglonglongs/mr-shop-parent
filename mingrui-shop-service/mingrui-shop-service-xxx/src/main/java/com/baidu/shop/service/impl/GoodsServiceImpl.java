package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.component.MrRabbitMQ;
import com.baidu.shop.constant.MqMessageConstant;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BeanCopy;
import com.baidu.shop.utils.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsServiceImpl
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/1/5
 * @Version V1.0
 **/
@RestController
@Slf4j
public class GoodsServiceImpl extends BaseApiService implements GoodsService {
    @Resource
    private SpuMapper spuMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Resource
    private StockMapper stockMapper;

    @Resource
    private SkuMapper skuMapper;

    @Autowired
    private MrRabbitMQ mrRabbitMQ;


    @Override
    @Transactional
    public Result<JSONObject> updateSaleable(SpuDTO spuDTO) {

        SpuEntity spuEntity = BeanCopy.copyProperties(spuDTO, SpuEntity.class);

        if (spuEntity.getSaleable()==1){
            spuEntity.setSaleable(0);
        }else{
            spuEntity.setSaleable(1);
        }

        spuMapper.updateByPrimaryKeySelective(spuEntity);

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> GoodsDeletes(Integer pId) {

        this.GoodsDelete(pId);

        mrRabbitMQ.send(pId + "", MqMessageConstant.SPU_ROUT_KEY_DELETE);

        return this.setResultSuccess();
    }
    @Transactional
    public void GoodsDelete(Integer pId){

        spuMapper.deleteByPrimaryKey(pId);

        spuDetailMapper.deleteByPrimaryKey(pId);

        //sku
        Example example = new Example(SpuEntity.class);
        example.createCriteria().andEqualTo("id",pId);
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
        List<Long> skuId = skuEntities.stream().map(skuEntity -> skuEntity.getId()).collect(Collectors.toList());
        skuMapper.deleteByIdList(skuId);
        stockMapper.deleteByIdList(skuId);
    }

    @Override
//    @Transactional
    public Result<JSONObject> updateGoods(SpuDTO spuDTO) {
        this.updateGood(spuDTO);
        mrRabbitMQ.send(spuDTO.getId()+"", MqMessageConstant.SPU_ROUT_KEY_UPDATE);
        return this.setResultSuccess();
    }

    @Transactional
    public void updateGood(SpuDTO spuDTO){
        //spu
        final Date date = new Date();
        SpuEntity spuEntity = BeanCopy.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spuEntity);
        //spuDetail
        SpuDetailEntity spuDetailEntity = BeanCopy.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailMapper.updateByPrimaryKeySelective(spuDetailEntity);
        //sku
        Example example = new Example(spuEntity.getClass());
        example.createCriteria().andEqualTo("id",spuEntity.getId());
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
        List<Long> skuId = skuEntities.stream().map(skuEntity -> skuEntity.getId()).collect(Collectors.toList());
        skuMapper.deleteByIdList(skuId);
        stockMapper.deleteByIdList(skuId);

        //新增sku
        List<SkuDTO> skus = spuDTO.getSkus();

        skus.stream().forEach(skuDTO -> {

            SkuEntity skuEntity = BeanCopy.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuEntity.getId());
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);

            //新增stock
            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            stockMapper.insertSelective(stockEntity);
        });
    }





    @Override
    @Transactional
    public Result<List<SkuEntity>> stockandskuList(Integer spuId) {
        List<SkuDTO> list = skuMapper.stockAndSkuList(spuId);

        return this.setResultSuccess(list);
    }

    @Override
    public Result<SpuDetailEntity> goodsSpuDetail(Integer spuId) {
        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);
        return this.setResultSuccess(spuDetailEntity);
    }

    @Override
//    @Transactional
    public Result<JSONObject> saveGoods(SpuDTO spuDTO) {

        Integer spuId  = this.saveGood(spuDTO);

        mrRabbitMQ.send(spuId+"", MqMessageConstant.SPU_ROUT_KEY_SAVE);

        return this.setResultSuccess();
    }
    @Transactional
    public Integer saveGood(SpuDTO spuDTO){
        final Date date = new Date();
        SpuEntity spuEntity = BeanCopy.copyProperties(spuDTO, SpuEntity.class);
        //新增spu,新增返回主键, 给必要字段赋默认值
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        spuMapper.insertSelective(spuEntity);

        //新增spuDetail 根据Id查询SpuId
        SpuDetailDTO spuDetail = spuDTO.getSpuDetail();
        SpuDetailEntity spuDetailEntity = BeanCopy.copyProperties(spuDetail,SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuEntity.getId());
        spuDetailMapper.insertSelective(spuDetailEntity);

        //新增sku
        List<SkuDTO> skus = spuDTO.getSkus();

        skus.stream().forEach(skuDTO -> {

            SkuEntity skuEntity = BeanCopy.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuEntity.getId());
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);

            //新增stock
            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            stockMapper.insertSelective(stockEntity);

        });
        return spuEntity.getId();
    }





    @Override
    public Result<List<SpuDTO>> queryGoods(SpuDTO spuDTO) {
//        if (ObjectUtil.isNotNull(spuDTO.getPage()) && ObjectUtil.isNotNull(spuDTO.getRows()));
//        PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());



        Example example = new Example(SpuEntity.class);


        Example.Criteria criteria = example.createCriteria();
        if (ObjectUtil.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() < 2){
            criteria.andEqualTo("saleable",spuDTO.getSaleable());
        }
        if(ObjectUtil.isNotNull(spuDTO.getId())){
            criteria.andEqualTo("id",spuDTO.getId());

        }
        if(!StringUtils.isEmpty(spuDTO.getSort()) && !StringUtils.isEmpty(spuDTO.getOrder()));
        PageHelper.orderBy(spuDTO.getOrderBy());

        if(!StringUtils.isEmpty(spuDTO.getTitle())) example.createCriteria().andLike("title","%"+spuDTO.getTitle()+"%");

        List<SpuEntity> spuEntities = spuMapper.selectByExample(example);

        List<SpuDTO> collect = spuEntities.stream().map(spuEntity -> {
            SpuDTO spuDTO1 = BeanCopy.copyProperties(spuEntity, SpuDTO.class);


            CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(spuDTO1.getCid1());
            CategoryEntity categoryEntity2 = categoryMapper.selectByPrimaryKey(spuDTO1.getCid2());
            CategoryEntity categoryEntity3 = categoryMapper.selectByPrimaryKey(spuDTO1.getCid3());
            spuDTO1.setCategoryName(categoryEntity.getName()+"/"+categoryEntity2.getName()+"/"+categoryEntity3.getName());

//            List<CategoryEntity> categoryEntities = categoryMapper.selectByIdList(Arrays.asList(spuEntity.getCid1(), spuEntity.getCid2(), spuEntity.getCid3()));
//          categoryEntities.stream().map()


            BrandEntity brandEntity = brandMapper.selectByPrimaryKey(spuDTO1.getBrandId());
            spuDTO1.setBrandName(brandEntity.getName());

            return spuDTO1;
        }).collect(Collectors.toList());


        PageInfo<SpuEntity> pageInfo = new PageInfo<>(spuEntities);

        return this.setResult(HTTPStatus.OK,pageInfo.getTotal()+"",collect);
    }
}
