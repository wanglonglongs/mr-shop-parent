package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName StockEntity
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/1/7
 * @Version V1.0
 **/
@Data
@Table(name = "tb_stock")
public class StockEntity {
    @Id
    private Long skuId;

    private Integer seckillStock;

    private Integer seckillTotal;

    private Integer stock;
}
