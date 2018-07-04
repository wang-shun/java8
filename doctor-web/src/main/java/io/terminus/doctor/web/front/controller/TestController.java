package io.terminus.doctor.web.front.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.service.TestService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Description TODO
 * @Author Danny
 * @Date 2018/7/4 10:50
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @RpcConsumer
    private TestService testService;

    // 查询
    @RequestMapping(value = "/select/{rid}",method = RequestMethod.GET)
    public Response select(@PathVariable("rid") Integer id){
       return testService.select(id);
    }

     // 添加
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public Response add(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle){
        return testService.add(doctorWarehouseMaterialHandle);
    }

    // 修改
    @RequestMapping(value = "/update",method = RequestMethod.PUT)
    public Response update(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle){
        return testService.update(doctorWarehouseMaterialHandle);

    }

    // 删除
    @RequestMapping(value = "/delete/{rid}",method = RequestMethod.DELETE)
    public Response delete(@PathVariable("rid") Long id){
        return testService.delete(id);
    }


}
