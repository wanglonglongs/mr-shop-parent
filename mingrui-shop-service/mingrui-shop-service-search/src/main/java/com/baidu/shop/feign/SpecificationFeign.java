package com.baidu.shop.feign;

import com.baidu.shop.service.SpecificationService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName specificationFeign
 * @Description: TODO
 * @Author wanglonglong
 * @Date 2021/3/4
 * @Version V1.0
 **/
@FeignClient(value = "xxx-server",contextId = "specificationFeign")
public interface SpecificationFeign extends SpecificationService {
}
