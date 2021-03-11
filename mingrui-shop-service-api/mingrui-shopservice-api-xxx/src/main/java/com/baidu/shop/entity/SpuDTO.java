package com.baidu.shop.entity;

import com.baidu.shop.base.BaseDTO;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @ClassName SpuDTO
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/1/5
 * @Version V1.0
 **/
@ApiModel(value = "spu数据传输DTO")
@Data
public class SpuDTO extends BaseDTO {
    @NotNull(message = "主键不能为空",groups = {MingruiOperation.Update.class})
    @ApiModelProperty(value = "主键",example = "1")
    private Integer id;

    @ApiModelProperty(value = "标题")
    @NotEmpty(message = "标题不能为空",groups = {MingruiOperation.Add.class})
    private String title;

    @ApiModelProperty(value = "子标题")
    private String subTitle;



    @ApiModelProperty(value = "一级类目",example = "1")
    @NotNull(message = "一级类目Id不能为空",groups = {MingruiOperation.Add.class})
    private Integer cid1;

    @ApiModelProperty(value = "二级类目",example = "1")
    @NotNull(message = "二级类目Id不能为空",groups = {MingruiOperation.Add.class})
    private Integer cid2;

    @ApiModelProperty(value = "三级类目",example = "1")
    @NotNull(message = "三级类目Id不能为空",groups = {MingruiOperation.Add.class})
    private Integer cid3;

    @ApiModelProperty(value = "商品所属品牌Id",example = "1")
    @NotNull(message = "商品所属品牌Id不能为空",groups = {MingruiOperation.Add.class})
    private Integer brandId;

    @ApiModelProperty(value = "是否上架，0下架，1上架",example = "1")
    @NotNull(message = "上架信息不能为空",groups = {MingruiOperation.Add.class,MingruiOperation.Update.class})
    private Integer saleable;

    @ApiModelProperty(value = "是否有效，0已删除，1有效", example = "1")
    @NotNull(message = "有效不能为空",groups = {MingruiOperation.Add.class,MingruiOperation.Update.class})
    private Integer valid;

    @ApiModelProperty(value = "添加时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date lastUpdateTime;

    private String categoryName;

    private String brandName;

    private SpuDetailDTO spuDetail;

    private List<SkuDTO> skus;

}
