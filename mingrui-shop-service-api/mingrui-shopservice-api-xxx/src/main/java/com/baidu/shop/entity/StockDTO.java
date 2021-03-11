package com.baidu.shop.entity;

import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ClassName StockDTO
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/1/7
 * @Version V1.0
 **/
@Data
@ApiModel(value = "库存")
public class StockDTO {
   @ApiModelProperty(value = "sku主键",example = "1")
   @NotNull(message = "主键不能为空",groups = {MingruiOperation.Update.class})
    private Long skuId;

   @ApiModelProperty(value = "可秒杀库存",example = "1")
    private Integer seckillStock;

   @ApiModelProperty(value = "秒杀总数量",example = "1")
    private Integer seckillTotal;

   @ApiModelProperty(value = "库存数量",example = "1")
   @NotNull(message = "库存数量不能为空",groups = {MingruiOperation.Update.class,MingruiOperation.Add.class})
    private Integer stock;
}
